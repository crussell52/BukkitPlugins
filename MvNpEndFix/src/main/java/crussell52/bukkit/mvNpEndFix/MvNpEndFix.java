package crussell52.bukkit.mvNpEndFix;


import com.onarandombox.MultiverseNetherPortals.MultiverseNetherPortals;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

import static org.bukkit.World.Environment.THE_END;

public final class MvNpEndFix extends JavaPlugin implements Listener
{
    /**
     * {@inheritDoc}
     */
    @Override
    public void onEnable()
    {
        this.getServer().getPluginManager().registerEvents(this, this);
    }

    // MVNP listens with a normal priority. Ours must occur afterwards, so set at high priority.
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerPortal(PlayerPortalEvent event) {
        if (event.isCancelled()) {
            return;
        }

        this.getLogger().log(Level.INFO, "Detected portal event.");

        MultiverseNetherPortals mvnp = (MultiverseNetherPortals) this.getServer().getPluginManager().getPlugin("Multiverse-NetherPortals");

        if (mvnp == null) {
            this.getLogger().log(Level.INFO, "Multiverse-NetherPortals not found.");
            return;
        }

        if (event.getTo() == null) {
            this.getLogger().log(Level.INFO, "No Destination... Don't know what to do.");
            return;
        }

        Location originalTo = event.getTo().clone();
        Location currentLocation = event.getFrom().clone();

        // Make sure this is an event that is (was) handled by Multiverse-NetherPortals
        if (!mvnp.isHandledByNetherPortals(currentLocation)) {
            this.getLogger().log(Level.INFO, "Multiverse-NetherPortals Not handling this PortalEvent.");
            return;
        }

        // Make sure this portal event involves a "The end" world.
        World.Environment fromEnv = currentLocation.getWorld().getEnvironment();
        World.Environment toEnv = originalTo.getWorld().getEnvironment();
        if ( fromEnv != THE_END && toEnv != THE_END) {
            this.getLogger().log(Level.INFO, "PortalEvent does not involve The End.");
            return;
        }

        // Disallow nether portal usage in the The End.
        if (fromEnv == THE_END &&  event.getFrom().getBlock().getType() == Material.PORTAL) {
            this.getLogger().log(Level.INFO, "Blocked nether portal usage in The End.");
            event.setCancelled(true);
            return;
        }

        // Always send the player to the world spawn point in the case of the The End.
        if (toEnv == THE_END) {
            this.getLogger().log(Level.INFO, "Player traveling to The End - Sending to spawn point.");
            event.setTo(originalTo.getWorld().getSpawnLocation());
        }
    }
}
