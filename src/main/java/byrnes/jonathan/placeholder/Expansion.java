package byrnes.jonathan.placeholder;

import byrnes.jonathan.manager.BossManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;

public class Expansion extends PlaceholderExpansion {

    private final BossManager bossManager;

    public Expansion(BossManager bossManager) {
        this.bossManager = bossManager;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "fruitbosses";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Jonathan Byrnes";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String identifier) {
        switch (identifier.toLowerCase()) {
            case "next_spawn" -> {
                Instant now = Instant.now();
                Instant next = bossManager.getNextSpawnTime();

                if (now.isAfter(next)) {
                    return "Spawning soon";
                }

                Duration duration = Duration.between(now, next);
                long hours = duration.toHours();
                long minutes = duration.toMinutesPart();
                return (hours > 0 ? hours + "h " : "") + minutes + "m";
            }

            case "next_boss" -> {
                String id = bossManager.getNextBossId();
                if (id == null || id.isEmpty()) return "Unknown";
                return capitalize(id);
            }

            default -> {
                return null;
            }
        }
    }

    private String capitalize(String id) {
        return id.substring(0, 1).toUpperCase() + id.substring(1).toLowerCase();
    }
}
