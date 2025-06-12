package byrnes.jonathan.config;

import byrnes.jonathan.util.Text;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class ConfigHelper {
    private final JavaPlugin plugin;

    public ConfigHelper(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public FileConfiguration config() {
        return plugin.getConfig();
    }

    public void reload() {
        plugin.reloadConfig();
    }

    public Component getMessage(String path) {
        String raw = config().getString(path);
        return Text.translate(raw);
    }

    public Component getMessage(String path, String... replacements) {
        String raw = config().getString(path, "<red>Missing message: " + path);
        for (int i = 0; i < replacements.length - 1; i += 2) {
            raw = raw.replace(replacements[i], replacements[i + 1]);
        }
        return Text.translate(raw);
    }

    public List<String> getList(String path) {
        return config().getStringList(path);
    }

}
