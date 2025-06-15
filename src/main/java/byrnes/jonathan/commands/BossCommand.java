package byrnes.jonathan.commands;

import byrnes.jonathan.BossLoader;
import byrnes.jonathan.config.ConfigHelper;
import byrnes.jonathan.manager.BossManager;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.annotations.suggestion.Suggestions;
import org.incendo.cloud.context.CommandContext;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Command("boss")
public class BossCommand {

    private final BossManager bossManager;
    private final BossLoader bossLoader;
    private final ConfigHelper config;

    public BossCommand(BossManager bossManager, BossLoader bossLoader, ConfigHelper config) {
        this.bossManager = bossManager;
        this.bossLoader = bossLoader;
        this.config = config;
    }

    @Command("reload")
    @Permission("fruitbosses.reload")
    public void reload(CommandSender sender) {
        config.reload();
        bossLoader.reload();
        sender.sendMessage(config.getMessage("messages.reload_success"));
    }

    @Command("spawn <id>")
    @Permission("fruitbosses.spawn")
    public void spawn(
            CommandContext<CommandSender> context,
            @Argument(value = "id", suggestions = "boss-ids") final String id
    ) {
        boolean success = bossManager.spawnBoss(id, context.sender());
        if (success) {
            context.sender().sendMessage(config.getMessage("messages.boss_spawned", "%boss%", capitalize(id)));
        } else {
            context.sender().sendMessage(config.getMessage("messages.boss_spawn_failed", "%boss%", id));
        }
    }

    @Command("next")
    @Permission("fruitbosses.next")
    public void next(CommandSender sender) {
        boolean enabled = config.config().getBoolean("settings.show_next_boss_command", true);
        if (!enabled) {
            sender.sendMessage(config.getMessage("messages.command_disabled"));
            return;
        }

        String id = bossManager.getNextBossId();
        Instant time = bossManager.getNextSpawnTime();
        Duration remaining = Duration.between(Instant.now(), time);

        String formatted = formatDuration(remaining);
        sender.sendMessage(config.getMessage("messages.next_boss", "%boss%", capitalize(id)));
        sender.sendMessage(config.getMessage("messages.next_boss_timer", "%time%", formatted));
    }

    @Suggestions("boss-ids")
    public List<String> suggestBossIds(CommandContext<CommandSender> context, String input) {
        return bossLoader.getAllBossIds().stream()
                .filter(id -> input == null || id.toLowerCase().startsWith(input.toLowerCase()))
                .toList();
    }

    private String capitalize(String id) {
        if (id == null || id.isEmpty()) return id;
        return id.substring(0, 1).toUpperCase() + id.substring(1).toLowerCase();
    }

    private String formatDuration(Duration duration) {
        long minutes = duration.toMinutes();
        long hours = minutes / 60;
        long mins = minutes % 60;
        return (hours > 0 ? hours + "h " : "") + mins + "m";
    }
}
