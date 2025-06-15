package byrnes.jonathan.manager;

import byrnes.jonathan.BossLoader;
import byrnes.jonathan.config.ConfigHelper;
import byrnes.jonathan.model.Boss;
import byrnes.jonathan.util.Text;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class BossManager {

    private final ConfigHelper configHelper;
    private final BossLoader bossLoader;

    private final Map<UUID, Double> damageMap = new HashMap<>();
    private Boss currentBoss;
    private Instant nextSpawnTime;
    private String nextBossId;

    public BossManager(ConfigHelper configHelper, BossLoader bossLoader) {
        this.configHelper = configHelper;
        this.bossLoader = bossLoader;
        loadState();
    }

    public boolean spawnBoss(String bossId, CommandSender sender) {
        Boss boss = bossLoader.getBoss(bossId);
        if (boss == null) {
            send(sender, Component.text("No boss definition found for ID: " + bossId));
            return false;
        }

        Location location = boss.getLocation();
        World world = location.getWorld();
        if (world == null) {
            send(sender, configHelper.getMessage("messages.spawn_world_missing", "%world%", location.getWorld() != null ? location.getWorld().getName() : "null"));
            return false;
        }

        Optional<MythicMob> optionalMob = MythicBukkit.inst().getMobManager().getMythicMob(boss.getMythicId());
        if (optionalMob.isEmpty()) {
            send(sender, configHelper.getMessage("messages.boss_not_found", "%boss%", boss.getMythicId()));
            return false;
        }

        MythicMob mythicMob = optionalMob.get();
        ActiveMob result = mythicMob.spawn(BukkitAdapter.adapt(location), 1);

        if (result.isDead()) {
            send(sender, configHelper.getMessage("messages.boss_spawn_failed", "%boss%", bossId));
            return false;
        }

        // Store boss and reset damage tracking
        this.currentBoss = boss;
        this.damageMap.clear();

        // Feedback
        send(sender, configHelper.getMessage("messages.boss_spawned", "%boss%", bossId));
        if (configHelper.config().getBoolean("settings.announce_boss_spawn", true)) {
            Bukkit.broadcast(configHelper.getMessage("messages.boss_spawned", "%boss%", bossId));
        }
        return true;
    }

    public void startRotationTimer() {
        long savedTimestamp = configHelper.getNextSpawnTimestamp();
        nextBossId = configHelper.getCurrentRotationIndex();

        if (savedTimestamp > System.currentTimeMillis()) {
            nextSpawnTime = Instant.ofEpochMilli(savedTimestamp);
        } else {
            scheduleNextBoss();
        }

        Bukkit.getScheduler().runTaskTimerAsynchronously(configHelper.getPlugin(), () -> {
            if (Instant.now().isAfter(nextSpawnTime)) {
                Bukkit.getScheduler().runTask(configHelper.getPlugin(), () -> spawnBoss(nextBossId, Bukkit.getConsoleSender()));
                scheduleNextBoss();
            }
        }, 20L, 20L * 60); // every 60s
    }

    private void scheduleNextBoss() {
        List<String> rotation = configHelper.config().getStringList("rotation_order");
        int currentIndex = rotation.indexOf(nextBossId);
        int nextIndex = (currentIndex + 1) % rotation.size();
        nextBossId = rotation.get(nextIndex);

        int intervalHours = configHelper.getRotationIntervalHours();
        nextSpawnTime = Instant.now().plus(Duration.ofHours(intervalHours));

        configHelper.setCurrentRotationIndex(nextBossId);
        configHelper.setNextSpawnTimestamp(nextSpawnTime.toEpochMilli());
        configHelper.save();
    }

    public Boss getCurrentBoss() {
        return currentBoss;
    }

    public Map<UUID, Double> getDamageMap() {
        return damageMap;
    }

    public Instant getNextSpawnTime() {
        return nextSpawnTime;
    }

    public String getNextBossId() {
        return nextBossId;
    }

    private void loadState() {
        this.nextBossId = configHelper.getCurrentRotationIndex();
        this.nextSpawnTime = Instant.ofEpochMilli(configHelper.getNextSpawnTimestamp());
    }

    private void send(CommandSender sender, Component message) {
        sender.sendMessage(message);
    }
}
