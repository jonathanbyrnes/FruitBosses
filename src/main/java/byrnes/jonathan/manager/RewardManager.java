package byrnes.jonathan.manager;

import byrnes.jonathan.BossLoader;
import byrnes.jonathan.config.ConfigHelper;
import byrnes.jonathan.events.DamageListener;
import byrnes.jonathan.model.Boss;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class RewardManager {

    private final DamageListener damageListener;
    private final BossLoader bossLoader;
    private final ConfigHelper config;

    public RewardManager(DamageListener damageListener, BossLoader bossLoader, ConfigHelper config) {
        this.damageListener = damageListener;
        this.bossLoader = bossLoader;
        this.config = config;
    }

    public void distributeRewards() {
        String bossId = damageListener.getCurrentBossId();
        if (bossId == null) return;

        Boss boss = bossLoader.getBoss(bossId);
        if (boss == null) return;

        Map<UUID, Double> damageMap = damageListener.getDamageMap();
        if (damageMap.isEmpty()) return;

        List<Map.Entry<UUID, Double>> sorted = new ArrayList<>(damageMap.entrySet());
        sorted.sort(Map.Entry.<UUID, Double>comparingByValue().reversed());

        ConsoleCommandSender console = Bukkit.getConsoleSender();
        Map<String, List<String>> rewards = boss.getRewards();

        for (int i = 0; i < sorted.size(); i++) {
            UUID uuid = sorted.get(i).getKey();
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) continue;

            int position = i + 1;
            String tier = switch (position) {
                case 1 -> "position_1";
                case 2 -> "position_2";
                case 3 -> "position_3";
                default -> null;
            };

            if (tier != null && rewards.containsKey(tier)) {
                executeCommands(rewards.get(tier), player.getName(), console);
            }

            if (position <= 5 && rewards.containsKey("top_5")) {
                executeCommands(rewards.get("top_5"), player.getName(), console);
            }

            if (rewards.containsKey("participant")) {
                executeCommands(rewards.get("participant"), player.getName(), console);
            }
        }

        boolean announce = config.config().getBoolean("settings.announce_boss_rewards", true);
        if (announce) {
            Bukkit.broadcast(config.getMessage("messages.rewards_announced", "%boss%", capitalize(bossId)));
        }

        damageListener.clearActiveBoss();
    }

    private void executeCommands(List<String> commands, String playerName, ConsoleCommandSender console) {
        for (String cmd : commands) {
            Bukkit.dispatchCommand(console, cmd.replace("%player%", playerName));
        }
    }

    private String capitalize(String id) {
        return id.substring(0, 1).toUpperCase() + id.substring(1).toLowerCase();
    }
}
