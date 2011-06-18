package crussell52.poi.actions;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import crussell52.poi.Config;

public class ConfigReload extends ActionHandler {
	
	/**
	 * {@inheritDoc}
	 * 
	 * @param poiManager
	 */
	public ConfigReload() {
		super(null);
	}

	@Override
	public void handleAction(CommandSender sender, String action, String[] args) {
		// this can only be performed by the server
		if (sender instanceof Player) {
			sender.sendMessage(ChatColor.RED + "That can only be performed from the console.");
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
