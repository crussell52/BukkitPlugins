package crussell52.poi.actions;

import crussell52.poi.commands.PoiCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import crussell52.poi.Config;

import java.util.ArrayList;
import java.util.List;

public class ConfigReload extends ActionHandler {

    /**
     * {@inheritDoc}
     *
     * @param poiManager
     */
    public ConfigReload() {
        super(null);

        this._relatedPermission = "poi.action.config.reload";
        this._fromConsole = true;
        this._fromInGame  = true;
        this._lockdownOverride = true;
    }

    @Override
    public void handleAction(CommandSender sender, String action, String[] args) {
        if (!this._canExecute(sender)){
            return;
        }

        // make a record of whether lockdown is active before the reload.
        boolean lockDownActive = Config.isLocked();

        // attempt to reload.
        if (Config.reload()) {
            sender.sendMessage(ChatColor.GREEN + "Config successfully reloaded.");

            // see if a lock was just released
            if (lockDownActive && !Config.isLocked()) {
                // announce that the the lock down has been released
                Bukkit.getServer().broadcastMessage(ChatColor.YELLOW + "Points Of Interest is no longer in lock-down! Have Fun!");
            }
        }
        else {
            sender.sendMessage(ChatColor.RED + "Error reloading config - details in Log");
        }
    }

    public static List<String> getHelp(boolean isShort) {
        ArrayList<String> messages = new ArrayList<String>();
        String basic = HelpAction.action(PoiCommand.ACTION_RELOAD_CONFIG);
        if (isShort) {
            basic += HelpAction.shortDescription("Reload the configuration file.");
            messages.add(basic);
            return messages;
        }

        messages.add(basic);
        messages.add(ChatColor.GREEN + "------------");
        messages.add(ChatColor.YELLOW + "Use this action to reload the POI configuration from the");
        messages.add(ChatColor.YELLOW + "file without restarting your server. You need special");
        messages.add(ChatColor.YELLOW + "permissions to use this action.");

        return messages;
    }
}
