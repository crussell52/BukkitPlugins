package crussell52.bukkit.melopiaTweaks.deathXP;

import org.bukkit.Location;
import org.bukkit.entity.Animals;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.HashMap;
import java.util.Map;

public class DeathXPListener implements Listener {

    private Map<Location, Long> _killZones = new HashMap<Location, Long>();
    private Map<Location, Integer> _killZoneCounts = new HashMap<Location, Integer>();


    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        int loss = Math.round(event.getEntity().getTotalExperience() / 2.0f);
        event.setNewExp(event.getEntity().getTotalExperience() - loss);
        event.setDroppedExp(Math.round(loss * .25f));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMobDeath(EntityDeathEvent event) {
        if ((event.getEntity() instanceof Monster) || (event.getEntity() instanceof Animals)) {
            Location killZone = event.getEntity().getLocation().getBlock().getLocation();
            Long now = System.currentTimeMillis();
            if (!_killZones.containsKey(killZone) || (now - _killZones.get(killZone)) > 3000) {
                _killZoneCounts.put(killZone, 0);
            }

            _killZones.put(killZone, now);
            _killZoneCounts.put(killZone, _killZoneCounts.get(killZone) + 1);
            if (_killZoneCounts.get(killZone) > 2) {
                event.setDroppedExp(Math.round(event.getDroppedExp() * .80f));
            }
        }
    }
}
