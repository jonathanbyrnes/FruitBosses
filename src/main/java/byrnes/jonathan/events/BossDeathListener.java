package byrnes.jonathan.events;

import byrnes.jonathan.manager.RewardManager;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class BossDeathListener implements Listener {

    private final DamageListener damageListener;
    private final RewardManager rewardManager;

    public BossDeathListener(DamageListener damageListener, RewardManager rewardManager) {
        this.damageListener = damageListener;
        this.rewardManager = rewardManager;
    }

    @EventHandler
    public void onBossDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();

        // Only proceed if this is the tracked boss
        if (!damageListener.isActiveBoss(entity)) return;

        rewardManager.distributeRewards();
    }
}
