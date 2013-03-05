package crussell52.bukkit.escape;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class Escape extends JavaPlugin
{
    static
    {
        // Make sure EscapeLocations can be aliased. This will (perhaps) make it less scary when
        // view/edit the .yml files.
        ConfigurationSerialization.registerClass(EscapeLocation.class, "escape");
    }

    /**
     * Local handle to the EscapeArtist. Most of the "heavy lifting" is offloaded to this instance.
     */
    private EscapeArtist _escapeArtist;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onEnable()
    {
        // Create the escape artist and keep a local handle to it.
        _escapeArtist = new EscapeArtist(this);
    }

    /**
     * {@inheritDoc}
     */
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        // Console commands not currently supported.
        if (!(sender instanceof Player))
        {
            sender.sendMessage("You must be in the game to execute that command.");
        }

        // Use try/catch to handle any problems should we execute a command.
        try
        {
            // Cast into a player for easier use.
            @SuppressWarnings("ConstantConditions")
            Player player = (Player)sender;

            // Look for the escape command.
            if (cmd.getName().equalsIgnoreCase("escape"))
            {
                // Let the escape artist handle the actual escape.
                _escapeArtist.escape(player);
            }
            else if (cmd.getName().equalsIgnoreCase("goback"))
            {
                // Let the escape artist handle sending the player back to the last place they
                // escaped from.
                _escapeArtist.goBack(player);
            }
            else
            {
                // Unrecognized command. Return failure.
                return false;
            }
        }
        catch (EscapeException escapeException)
        {
            // We hit a known error condition when executing the command. EscapeException always
            // provides a user-friendly message associated with them so send that to the user.
            sender.sendMessage(escapeException.getMessage());
        }
        catch (Exception ex)
        {
            // Something unexpected happened. Log the details and let the user know.
            getLogger().log(Level.SEVERE, "Unexpected error during command.", ex);
            sender.sendMessage("An unknown force prevented the escape tunnel from opening. [System Error]");
        }

        // The command was recognized. If there were errors they have been handled.
        // Return success.
        return true;
    }
}
