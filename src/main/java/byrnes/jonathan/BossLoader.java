package byrnes.jonathan;

import byrnes.jonathan.config.ConfigHelper;
import byrnes.jonathan.model.Boss;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

public class BossLoader {

    private final ConfigHelper config;
    private final Map<String, Boss> bosses = new HashMap<>();
    private final List<String> rotationOrder = new ArrayList<>();

    public BossLoader(ConfigHelper config) {
        this.config = config;
        load();
    }

    public void load() {
        bosses.clear();
        rotationOrder.clear();
        Bukkit.getLogger().info("[BossLoader] Starting boss loading...");

        // Load rotation order
        List<String> order = config.config().getStringList("rotation_order");
        rotationOrder.addAll(order);
        Bukkit.getLogger().info("[BossLoader] Loaded rotation order: " + rotationOrder);

        // Load boss definitions
        ConfigurationSection section = config.config().getConfigurationSection("bosses");
        if (section == null) {
            Bukkit.getLogger().warning("[BossLoader] No 'bosses' section found in config.yml.");
            return;
        }

        for (String id : section.getKeys(false)) {
            ConfigurationSection bossSection = section.getConfigurationSection(id);
            if (bossSection == null) {
                Bukkit.getLogger().warning("[BossLoader] Missing section for boss ID: " + id);
                continue;
            }

            String mythicId = bossSection.getString("mythic_id");
            ConfigurationSection spawn = bossSection.getConfigurationSection("spawn");

            if (mythicId == null || spawn == null) {
                Bukkit.getLogger().warning("[BossLoader] Skipping boss '" + id + "' — missing 'mythic_id' or 'spawn' section.");
                continue;
            }

            String worldName = spawn.getString("world");
            double x = spawn.getDouble("x");
            double y = spawn.getDouble("y");
            double z = spawn.getDouble("z");

            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                Bukkit.getLogger().warning("[BossLoader] World '" + worldName + "' not found for boss: " + id);
                continue;
            }

            Location location = new Location(world, x, y, z);
            Map<String, List<String>> rewards = loadRewards(bossSection.getConfigurationSection("rewards"));

            String normalizedId = id.trim().toLowerCase();
            Boss boss = new Boss(normalizedId, mythicId, location, rewards);
            bosses.put(normalizedId, boss);
            Bukkit.getLogger().info("[BossLoader] Loaded boss: '" + normalizedId + "' → Mythic ID: " + mythicId);
        }

        Bukkit.getLogger().info("[BossLoader] Boss loading complete. Total bosses loaded: " + bosses.size());
    }

    private Map<String, List<String>> loadRewards(ConfigurationSection section) {
        Map<String, List<String>> rewards = new HashMap<>();
        if (section == null) return rewards;

        for (String key : section.getKeys(false)) {
            List<String> cmds = section.getStringList(key);
            rewards.put(key.toLowerCase(), cmds);
        }

        return rewards;
    }

    public void reload() {
        Bukkit.getLogger().info("[BossLoader] Reloading configuration...");
        config.reload();
        load();
    }

    public Boss getBoss(String id) {
        if (id == null) return null;
        String key = id.trim().toLowerCase();
        Bukkit.getLogger().info("[BossLoader] BossMap keys: " + bosses.keySet());
        Bukkit.getLogger().info("[BossLoader] Looking for bossId: '" + key + "'");
        return bosses.get(key);
    }

    public List<String> getRotationOrder() {
        return Collections.unmodifiableList(rotationOrder);
    }

    public int getRotationHours() {
        return config.getRotationIntervalHours();
    }

    public Set<String> getAllBossIds() {
        return Collections.unmodifiableSet(bosses.keySet());
    }
}
