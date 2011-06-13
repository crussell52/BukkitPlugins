package crussell52.poi.actions;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import crussell52.poi.Config;
import crussell52.poi.Poi;
import crussell52.poi.PoiManager;

public class SummaryAction extends ActionHandler {

	/**
	 * {@inheritDoc}
	 * 
	 * @param poiManager
	 */
	public SummaryAction(PoiManager poiManager) {
		super(poiManager);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleAction(CommandSender sender, String action, String[] args) {
		// you have to a player (for now) to do this.
		if (!this._playerCheck(sender)) {
			return;
		}
		
		// see if we have any arguments
		if (args.length > 0) {
			// we should only have one argument
			if (args.length != 1) {
				this._actionUsageError(sender, "Too much information. Only ID is supported as an argument to this action.", action);
				return;
			}
		
			// attempt to select with the provided id.
			if (!this._selectPOI(args, 0, (Player)sender, action)) {
				// failed to select, do not proceed.
				return;
			}
		}
		
		// attempt to get the player's selected poi
		Poi poi = this._poiManager.getSelectedPoi((Player)sender);
		if (poi == null) {
			this._actionUsageError(sender, ChatColor.RED + "You must have a POI selected or specify an ID.", action);
			return;
		}
		
		// get a summary report and send it to the user with a nice header.
		ArrayList<String> summaryReport = 
			poi.getSummary(((Player)sender).getLocation(), Config.getDistanceThreshold(), ChatColor.WHITE);
		sender.sendMessage("");
		sender.sendMessage(ChatColor.YELLOW + "---- POI Summary ----");
		for (String message : summaryReport) {
			sender.sendMessage(message);
		}
	}

}
