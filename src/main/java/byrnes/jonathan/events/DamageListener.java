package byrnes.jonathan.events;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.*;

public class DamageListener implements Listener {

    private LivingEntity activeBoss;
    private String currentBossId;

    private final Map<UUID, Double> damageMap = new HashMap<>();

    public void setActiveBoss(LivingEntity boss) {
        this.activeBoss = boss;
        this.damageMap.clear();
    }

    public LivingEntity getActiveBoss() {
        return activeBoss;
    }

    public void setCurrentBossId(String id) {
        this.currentBossId = id;
    }

    public String getCurrentBossId() {
        return currentBossId;
    }

    public Map<UUID, Double> getDamageMap() {
        return Collections.unmodifiableMap(damageMap);
    }

    public void clearActiveBoss() {
        this.activeBoss = null;
        this.currentBossId = null;
        this.damageMap.clear();
    }

    @EventHandler
    public void onBossDamaged(EntityDamageByEntityEvent event) {
        if (activeBoss == null) return;
        if (!event.getEntity().equals(activeBoss)) return;
        if (!(event.getDamager() instanceof Player player)) return;

        damageMap.merge(player.getUniqueId(), event.getFinalDamage(), Double::sum);
    }

    public boolean isActiveBoss(Entity entity) {
        return activeBoss != null && activeBoss.equals(entity);
    }

}
