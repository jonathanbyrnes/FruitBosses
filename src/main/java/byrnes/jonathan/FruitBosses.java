package byrnes.jonathan;

import byrnes.jonathan.commands.BossCommand;
import byrnes.jonathan.config.ConfigHelper;
import byrnes.jonathan.events.BossDeathListener;
import byrnes.jonathan.events.DamageListener;
import byrnes.jonathan.manager.BossManager;
import byrnes.jonathan.manager.RewardManager;
import byrnes.jonathan.placeholder.Expansion;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.LegacyPaperCommandManager;

public final class FruitBosses extends JavaPlugin {

    private ConfigHelper configHelper;
    private BossManager bossManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // Core components
        configHelper = new ConfigHelper(this);
        BossLoader bossLoader = new BossLoader(configHelper);
        DamageListener damageListener = new DamageListener();
        bossManager = new BossManager(configHelper, bossLoader);
        RewardManager rewardManager = new RewardManager(damageListener, bossLoader, configHelper);

        // Register events
        getServer().getPluginManager().registerEvents(damageListener, this);
        getServer().getPluginManager().registerEvents(new BossDeathListener(damageListener, rewardManager), this);

        // Register commands via Cloud
        LegacyPaperCommandManager<CommandSender> commandManager =
                LegacyPaperCommandManager.createNative(this, ExecutionCoordinator.simpleCoordinator());
        AnnotationParser<CommandSender> parser = new AnnotationParser<>(commandManager, CommandSender.class);
        parser.parse(new BossCommand(bossManager, bossLoader, configHelper));

        // Register PAPI placeholders
        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new Expansion(bossManager).register();
        } else {
            getLogger().warning("PlaceholderAPI not found. Placeholders will not be available.");
        }

        // Start boss rotation
        bossManager.startRotationTimer();
    }

    @Override
    public void onDisable() {
        // Save rotation index and time
        if (configHelper != null && bossManager != null) {
            configHelper.config().set("settings.current_rotation_index", bossManager.getNextBossId());
            configHelper.config().set("settings.next_spawn_time", bossManager.getNextSpawnTime().toEpochMilli());
            saveConfig();
        }
    }
}
