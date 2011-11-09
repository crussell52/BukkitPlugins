package crussell52.poi.actions;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import crussell52.poi.Config;

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

}
