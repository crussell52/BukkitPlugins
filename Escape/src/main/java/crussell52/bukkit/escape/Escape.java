package crussell52.bukkit.escape;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

/**
 * Created with IntelliJ IDEA.
 * User: crussell
 * Date: 2/15/13
 * Time: 5:13 PM
 * To change this template use File | Settings | File Templates.
 */
public final class Escape extends JavaPlugin
{
    private EscapeArtist _escapeArtist;

    @Override
    public void onEnable()
    {
        _escapeArtist = new EscapeArtist(this);
    }

    @Override
    public void onDisable()
    {
        // TODO Insert logic to be performed when the plugin is disabled
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (!(sender instanceof Player))
        {
            sender.sendMessage("You must be in the game to execute that command.");
        }

        try
        {
            if (cmd.getName().equalsIgnoreCase("escape"))
            {
                //noinspection ConstantConditions
                _escapeArtist.escape((Player)sender);
                return true;
            }
            else if (cmd.getName().equalsIgnoreCase("goback"))
            {
                //noinspection ConstantConditions
                _escapeArtist.goBack((Player)sender);
                return true;
            }
        }
        catch (EscapeException escapeException)
        {
            sender.sendMessage(escapeException.getMessage());
            return true;
        }
        catch (Exception ex)
        {
            getLogger().log(Level.SEVERE, "Unexpected error during command.", ex);
            return true;
        }

        return false;
    }
}
