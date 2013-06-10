package crussell52.bukkit.melopiaTweaks.deathXP;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathXPListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        int loss = Math.round(event.getEntity().getTotalExperience() / 2.0f);
        event.setNewExp(event.getEntity().getTotalExperience() - loss);
        event.setDroppedExp(Math.round(loss * .25f));
    }
}
