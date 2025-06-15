package byrnes.jonathan.events;

import byrnes.jonathan.manager.RewardManager;
import byrnes.jonathan.events.DamageListener;
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class BossDeathListener implements Listener {

    private final DamageListener damageListener;
    private final RewardManager rewardManager;

    public BossDeathListener(DamageListener damageListener, RewardManager rewardManager) {
        this.damageListener = damageListener;
        this.rewardManager = rewardManager;
    }

    @EventHandler
    public void onBossDeath(MythicMobDeathEvent event) {
        if (damageListener.getActiveBoss() == null) return;

        String deadMobId = event.getMob().getType().getInternalName();
        String expectedMobId = damageListener.getActiveBoss().getMythicId();

        if (!deadMobId.equalsIgnoreCase(expectedMobId)) {
            Bukkit.getLogger().info("[FruitBosses] MythicMob ID mismatch: died=" + deadMobId + ", expected=" + expectedMobId);
            return;
        }

        Bukkit.getLogger().info("[FruitBosses] Boss match confirmed: " + deadMobId + ". Distributing rewards.");
        rewardManager.distributeRewards();
        damageListener.clearActiveBoss();
    }
}
