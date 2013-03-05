package crussell52.bukkit.escape;

import crussell52.bukkit.common.CustomConfig;
import crussell52.bukkit.common.TeleportUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;

import java.util.logging.Level;

public class EscapeArtist
{
    /**
     * Used to assist with teleportations.
     */
    private TeleportUtil _teleportUtil = new TeleportUtil();

    /**
     * Local handle to the main plugin.
     */
    private Plugin _plugin = null;

    /**
     * Handle to "config" file used to persist information about where people have escaped from.
     */
    private CustomConfig _escapedFromData = null;

    /**
     * Creates a new instance associated with the given plugin.
     *
     * Upon instantiation, persisted escape data will be loaded if it possible.
     *
     * @param plugin A handle to the main plugin.
     */
    public EscapeArtist(Plugin plugin)
    {
        _plugin = plugin;
        _escapedFromData = new CustomConfig(_plugin, "escape_data.yml");
        _escapedFromData.getConfig().getValues(false);
    }

    /**
     * Attempts to send the player to their default escape location.
     *
     * @param player The player who is trying to escape.
     *
     * @throws EscapeException This Exception represents a known error condition and will provide a
     *                         user-friendly message.
     */
    public void escape(Player player) throws EscapeException
    {
        // Take a snapshot of the player's current location. We'll use this to track where they
        // came from in the case of a successful teleport.
        Location originalLocation = player.getLocation().clone();

        // Attempt to teleport the player. Leave exceptions unhandled so that they bubble up.
        _teleportPlayer(player, player.getWorld().getSpawnLocation());

        // If we're here, then the player teleported without an Exception begin thrown. Keep track
        // of where they came from.
        _setEscapedFromData(player, new EscapeDetails(originalLocation, System.currentTimeMillis()));
    }

    /**
     * Attempts to send the player back to the last location they teleported from.
     *
     * @param player The player who is trying to go back.
     *
     * @throws EscapeException This Exception represents a known error condition and will provide a
     *                         user-friendly messsage.
     */
    public void goBack(Player player) throws EscapeException
    {
        // Find out where the player last escaped from.
        EscapeDetails target = (EscapeDetails) _escapedFromData.getConfig().get(player.getName());
        if (target == null)
        {
            // The user has no where to go back to. The exception gets a user-friendly message to
            // let them know what happened.
            throw new EscapeException("You didn't escape from anywhere!");
        }

        // Declare a target location. before trying to
        Location targetLocation;

        // Attempt to translate the target EscapeLocation into a regular Location with the help
        // of the server (via the plugin instance).
        try
        {
            targetLocation = target.toLocation(_plugin.getServer());
        }
        catch (EscapeException escapeException)
        {
            // Only one possible failure case.  The recorded world doesn't exist.  This is an odd
            // case. Log it and give the user a dramatic message.
            _plugin.getLogger().log(Level.SEVERE, "The 'escaped from' world does not exist.", escapeException);
            throw new EscapeException("The world you escaped from no longer exists!");
        }

        // We know where to send the player, so try to send them there.
        _teleportPlayer(player, targetLocation);

        // The teleport did not throw an exception so everything went as planned. Clear out the
        // player's escaped-from data since they can only return to it once.
        _setEscapedFromData(player, null);
    }

    /**
     * Helper function for teleporting the player to a safe location and identifying failures.
     *
     * @param player The player to be teleported
     * @param target The Location to use as a target. If this location is not safe (e.g. over lava),
     *               then it will be used as a basis for finding a safe landing spot.
     *
     * @throws EscapeException If something (such as another plugin) prevents the teleportation from
     *                         occurring, then this exception will be thrown with a user-friendly
     *                         message.
     */
    private void _teleportPlayer(Player player, Location target) throws EscapeException
    {
        // Attempt to teleport
        if (!player.teleport(_teleportUtil.findSafeLanding(target), PlayerTeleportEvent.TeleportCause.PLUGIN))
        {
            // Something prevented the teleport.  Provide a dramatic, yet identifiable message for the user.
            throw new EscapeException("An unknown force has prevented you from traveling through the escape tunnel.");
        }
    }

    /**
     * Helper function which stores information about a player's escape.
     *
     * The data is stored locally and in a data file.
     *
     * @param player The player who escaped.
     * @param location EscapeLocation instance which contains the details of the player's escape.
     */
    private void _setEscapedFromData(Player player, EscapeDetails location)
    {
        // Store it locally then persist the data.
        _escapedFromData.getConfig().set(player.getName(), location);
        _escapedFromData.saveConfig();
    }
}
