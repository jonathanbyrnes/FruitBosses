package byrnes.jonathan.model;

import org.bukkit.Location;

import java.util.List;
import java.util.Map;

public class Boss {

    private final String id;
    private final String mythicId;
    private final Location location;
    private final Map<String, List<String>> rewards;

    public Boss(String id, String mythicId, Location location, Map<String, List<String>> rewards) {
        this.id = id;
        this.mythicId = mythicId;
        this.location = location;
        this.rewards = rewards;
    }

    public String getId() {
        return this.id;
    }

    public String getMythicId() {
        return this.mythicId;
    }

    public Location getLocation() {
        return this.location;
    }

    public Map<String, List<String>> getRewards() {
        return this.rewards;
    }
}
