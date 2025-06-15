package byrnes.jonathan.events;

import byrnes.jonathan.model.Boss;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DamageListener implements Listener {

    private Boss activeBossData;
    private LivingEntity activeEntity;
    private final Map<UUID, Double> damageMap = new HashMap<>();

    // Called when a boss is spawned
    public void setActiveBoss(LivingEntity entity, Boss bossData) {
        this.activeEntity = entity;
        this.activeBossData = bossData;
        this.damageMap.clear();
        Bukkit.getLogger().info("[FruitBosses] setActiveBoss called for: " + bossData.getId() + " / " + bossData.getMythicId());

    }

    // Called when boss dies or fight ends
    public void clearActiveBoss() {
        this.activeEntity = null;
        this.activeBossData = null;
        this.damageMap.clear();
    }

    // Handle player damage to boss
    @EventHandler
    public void onBossDamaged(EntityDamageByEntityEvent event) {
        if (activeEntity == null || activeBossData == null) return;
        if (!event.getEntity().equals(activeEntity)) return;
        if (!(event.getDamager() instanceof Player player)) return;

        double damage = event.getFinalDamage();
        damageMap.merge(player.getUniqueId(), damage, Double::sum);
    }

    // === GETTERS ===

    public Boss getActiveBoss() {
        return activeBossData;
    }

    public LivingEntity getActiveEntity() {
        return activeEntity;
    }

    public Map<UUID, Double> getDamageMap() {
        return Collections.unmodifiableMap(damageMap);
    }

    // Used by BossDeathListener to confirm identity
    public boolean isActiveBoss(String mythicMobId) {
        return activeBossData != null && mythicMobId.equalsIgnoreCase(activeBossData.getMythicId());
    }

    // Optional: if you want to check by Bukkit entity
    public boolean isActiveBoss(Entity entity) {
        return activeEntity != null && activeEntity.equals(entity);
    }
}
