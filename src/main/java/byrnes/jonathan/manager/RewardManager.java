package byrnes.jonathan.manager;

import byrnes.jonathan.BossLoader;
import byrnes.jonathan.config.ConfigHelper;
import byrnes.jonathan.events.DamageListener;
import byrnes.jonathan.model.Boss;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.ConsoleCommandSender;

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
        if (damageListener.getActiveBoss() == null) return;
        String bossId = damageListener.getActiveBoss().getId();
        if (bossId == null) return;

        Boss boss = bossLoader.getBoss(bossId);
        if (boss == null) return;

        Map<UUID, Double> damageMap = damageListener.getDamageMap();
        if (damageMap.isEmpty()) return;

        List<Map.Entry<UUID, Double>> sorted = damageMap.entrySet().stream()
                .sorted(Map.Entry.<UUID, Double>comparingByValue().reversed())
                .toList();

        ConsoleCommandSender console = Bukkit.getConsoleSender();
        Map<String, List<String>> rewards = boss.getRewards();

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("%boss%", bossId.toUpperCase());

        // === Top 1–3 rewards and placeholder mapping ===
        for (int i = 0; i < sorted.size() && i < 5; i++) {
            UUID uuid = sorted.get(i).getKey();
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            String playerName = offlinePlayer.getName() != null ? offlinePlayer.getName() : "Unknown";
            double damage = sorted.get(i).getValue();

            int position = i + 1;
            placeholders.put("%player_position_" + position + "%", playerName);
            placeholders.put("%damage_position_" + position + "%", String.valueOf((int) damage));

            String tierKey = "position_" + position;
            if (rewards.containsKey(tierKey) && offlinePlayer.isOnline()) {
                executeCommands(rewards.get(tierKey), playerName, console);
            }
        }

        // === Top 5 leaderboard broadcast ===
        if (rewards.containsKey("top_5")) {
            for (String line : rewards.get("top_5")) {
                String parsed = replacePlaceholders(line, placeholders);
                Bukkit.dispatchCommand(console, parsed);
            }
        }

        // === Participant rewards ===
        if (rewards.containsKey("participant")) {
            for (UUID uuid : damageMap.keySet()) {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                if (!offlinePlayer.isOnline()) continue;

                executeCommands(rewards.get("participant"), offlinePlayer.getName(), console);
            }
        }

        // === Final broadcast ===
        if (config.config().getBoolean("settings.announce_boss_rewards", true)) {
            Bukkit.broadcast(config.getMessage("messages.rewards_announced", "%boss%", capitalize(bossId)));
        }

        damageListener.clearActiveBoss();
    }

    private void executeCommands(List<String> commands, String playerName, ConsoleCommandSender console) {
        for (String cmd : commands) {
            String replaced = cmd.replace("%player%", playerName);
            Bukkit.dispatchCommand(console, replaced);
        }
    }

    private String replacePlaceholders(String input, Map<String, String> replacements) {
        String original = input; // save for comparison

        // Replace known placeholders
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            input = input.replace(entry.getKey(), entry.getValue());
        }

        // If original line had player/damage placeholders, and they're still present → skip
        if ((original.contains("%player_position_") && input.contains("%player_position_")) ||
                (original.contains("%damage_position_") && input.contains("%damage_position_"))) {
            return null;
        }

        return input;
    }

    private String capitalize(String id) {
        return id.substring(0, 1).toUpperCase() + id.substring(1).toLowerCase();
    }
}
