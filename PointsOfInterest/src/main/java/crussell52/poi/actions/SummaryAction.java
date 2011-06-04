package crussell52.poi.actions;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import crussell52.poi.Poi;
import crussell52.poi.PoiManager;

public class SummaryAction extends ActionHandler {

	public SummaryAction(PoiManager poiManager) {
		super(poiManager);
	}

	@Override
	public void handleAction(CommandSender sender, String action, String[] args) {
		// you have to a player (for now) to do this.
		if (!this._playerCheck(sender)) {
			return;
		}
				
		if (args.length > 0) {
			if (args.length != 1) {
				sender.sendMessage(ChatColor.RED + "Too much information. Only ID is supported as an argument to this action.");
				return;
			}
		
			// attempt to select with the provided id.
			if (!this._selectPOI(args, 0, (Player)sender)) {
				// failed to select, do not proceed.
				return;
			}
		}
		
		Poi poi = this._poiManager.getSelectedPOI((Player)sender);
		if (poi == null) {
			sender.sendMessage(ChatColor.RED + "You must have a POI selected or specify an ID.");
			return;
		}
		
		ArrayList<String> summaryReport = (poi.getSummary(((Player)sender).getLocation(), 2000, ""));
		
		sender.sendMessage("");
		sender.sendMessage(ChatColor.YELLOW + "---- POI Summary ----");
		for (String message : summaryReport) {
			sender.sendMessage(message);
		}
	}

}
