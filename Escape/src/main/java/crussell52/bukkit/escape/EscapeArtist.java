package crussell52.bukkit.escape;

import crussell52.bukkit.common.CustomConfig;
import crussell52.bukkit.common.TeleportUtil;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;

import java.util.Date;
import java.util.logging.Level;

/**
 * Created with IntelliJ IDEA.
 * User: crussell
 * Date: 2/15/13
 * Time: 5:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class EscapeArtist
{
    static
    {
        ConfigurationSerialization.registerClass(EscapeLocation.class, "escape");
    }

    private TeleportUtil _teleportUtil = new TeleportUtil();

    private Plugin _plugin = null;
    private CustomConfig _escapedFromData = null;

    public EscapeArtist(Plugin plugin)
    {
        _plugin = plugin;
        _escapedFromData = new CustomConfig(_plugin, "escape_data.yml");
        _escapedFromData.getConfig().getValues(false);
    }

    public void escape(Player player) throws EscapeException
    {
        Location originalLocation = player.getLocation().clone();
        _teleportPlayer(player, player.getWorld().getSpawnLocation());
        _setEscapedFromData(player, new EscapeLocation(originalLocation, System.currentTimeMillis()));
    }

    public void goBack(Player player) throws EscapeException
    {
        EscapeLocation target = (EscapeLocation) _escapedFromData.getConfig().get(player.getName());
        if (target == null)
        {
            throw new EscapeException("You didn't escape from anywhere!");
        }

        Location targetLocation;

        try
        {
            targetLocation = target.toLocation(_plugin.getServer());
        }
        catch (EscapeException escapeException)
        {
            _plugin.getLogger().log(Level.SEVERE, "The 'escaped from' world does not exist.", escapeException);
            throw new EscapeException("The world you escaped from no longer exists!");
        }

        _teleportPlayer(player, targetLocation);
        _setEscapedFromData(player, null);

    }

    private void _teleportPlayer(Player player, Location target) throws EscapeException
    {
        if (!player.teleport(_teleportUtil.findSafeLanding(target), PlayerTeleportEvent.TeleportCause.PLUGIN))
        {
            throw new EscapeException("An unknown force has prevented your teleportation.");
        }
    }

    private void _setEscapedFromData(Player player, EscapeLocation location)
    {
        _escapedFromData.getConfig().set(player.getName(), location);
        _escapedFromData.saveConfig();
    }
}
