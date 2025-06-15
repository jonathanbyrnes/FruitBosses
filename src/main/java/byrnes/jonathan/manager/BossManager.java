package byrnes.jonathan.manager;

import byrnes.jonathan.BossLoader;
import byrnes.jonathan.config.ConfigHelper;
import byrnes.jonathan.events.DamageListener;
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
import org.bukkit.entity.LivingEntity;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class BossManager {

    private final ConfigHelper configHelper;
    private final BossLoader bossLoader;
    private final DamageListener damageListener;

    private final Map<UUID, Double> damageMap = new HashMap<>();
    private Boss currentBoss;
    private Instant nextSpawnTime;
    private String nextBossId;

    public BossManager(ConfigHelper configHelper, BossLoader bossLoader, DamageListener damageListener) {
        this.configHelper = configHelper;
        this.bossLoader = bossLoader;
        this.damageListener = damageListener;
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

        LivingEntity entity = (LivingEntity) result.getEntity().getBukkitEntity();
        boolean entityNull =  entity == null;

        this.damageListener.setActiveBoss(entity, this.currentBoss);
        Bukkit.getLogger().info("[FruitBosses] is this living entity variable null? " + entityNull);

        scheduleNextBoss();
        // Feedback
        send(sender, configHelper.getMessage("messages.boss_spawned", "%boss%", bossId));
        if (configHelper.config().getBoolean("settings.announce_boss_spawn", true)) {
            Bukkit.broadcast(configHelper.getMessage("messages.boss_spawned", "%boss%", bossId));
        }
        return true;
    }

    public void startRotationTimer() {
        long savedTimestamp = configHelper.getNextSpawnTimestamp();
        this.nextBossId = configHelper.getCurrentRotationIndex();

        // Use saved time if valid
        if (savedTimestamp > System.currentTimeMillis()) {
            this.nextSpawnTime = Instant.ofEpochMilli(savedTimestamp);
        } else {
            // Otherwise schedule the next boss immediately
            scheduleNextBoss(); // This sets nextBossId and nextSpawnTime
        }

        Bukkit.getLogger().info("[FruitBosses] Rotation timer started. Next boss: " + nextBossId + " at " + nextSpawnTime);

        // Timer that checks every 60 seconds
        Bukkit.getScheduler().runTaskTimerAsynchronously(configHelper.getPlugin(), () -> {
            if (Instant.now().isAfter(nextSpawnTime)) {
                Bukkit.getScheduler().runTask(configHelper.getPlugin(), () -> {
                    boolean success = spawnBoss(nextBossId, Bukkit.getConsoleSender());

                    if (success) {
                        // Only schedule the next boss if spawn succeeded
                        scheduleNextBoss();
                        Bukkit.getLogger().info("[FruitBosses] Boss '" + nextBossId + "' spawned. Next boss in rotation scheduled.");
                    } else {
                        Bukkit.getLogger().warning("[FruitBosses] Failed to spawn boss '" + nextBossId + "'. Will retry in 60 seconds.");
                    }
                });
            }
        }, 20L, 20L * 60); // check every 60 seconds
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
        Bukkit.getLogger().info("[FruitBosses] Next boss set to: " + nextBossId + " at " + nextSpawnTime);
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
