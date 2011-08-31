package crussell52.poi.actions;

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
	}

	@Override
	public void handleAction(CommandSender sender, String action, String[] args) {
		if (!this._canExecute(sender)){
			return;
		}

		// attempt to reload.
		if (Config.reload()) {
			sender.sendMessage(ChatColor.GREEN + "Config successfully reloaded.");
		}
		else {
			sender.sendMessage(ChatColor.RED + "Error reloading config - details in Log");
		}
	}

}
