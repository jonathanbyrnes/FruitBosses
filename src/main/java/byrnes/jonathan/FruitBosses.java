package byrnes.jonathan;

import byrnes.jonathan.config.ConfigHelper;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.LegacyPaperCommandManager;

public final class FruitBosses extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        LegacyPaperCommandManager<CommandSender> commandManager = LegacyPaperCommandManager.createNative(this, ExecutionCoordinator.simpleCoordinator());
        AnnotationParser<CommandSender> parser = new AnnotationParser<>(commandManager, CommandSender.class);

        ConfigHelper configHelper = new ConfigHelper(this);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
