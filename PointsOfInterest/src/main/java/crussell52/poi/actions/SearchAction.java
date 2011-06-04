package crussell52.poi.actions;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import crussell52.poi.POIException;
import crussell52.poi.POIManager;
import crussell52.poi.PagedPoiList;
import crussell52.poi.commands.POICommand;

public class SearchAction extends ActionHandler {
	
	private static final int MAX_PER_PAGE = 3;
	
	public SearchAction(POIManager poiManager) {
		super(poiManager);
		this._isOwnerOnly = false;
	}
	
	
	@Override
	public void handleAction(CommandSender sender, String action, String[] args) {
		// we need a player in order to handle this action.
		if (!this._playerCheck(sender)) {
			return;
		}
		
		// now that we know we have a player, we can safely cast
		Player player = (Player)sender;
		
		// handle the search action
		if (action.equalsIgnoreCase(POICommand.ACTION_SEARCH)) {
			Location playerLoc = (player.getLocation());
			
			// attempt to get a list of nearby POIs
			try {
				PagedPoiList results = new PagedPoiList(MAX_PER_PAGE, this._poiManager.getNearby(playerLoc, 2000, 5));
				this._poiManager.setRecentResults(player, results);
				ArrayList<String> messages = results.getPageReport(1, playerLoc, 2000);
				for (String message : messages) {
					player.sendMessage(message);
				}
			}
			catch (POIException poiEx) {
				ActionHandler._log.severe(poiEx.toString());
				sender.sendMessage("There was a system error while looking for nearby POIs");
			}
		}
		else if (action.equalsIgnoreCase(POICommand.ACTION_LAST)) {
			// TODO: implement
		}
		else {
			sender.sendMessage("Unable to execute action.");
			ActionHandler._log.severe("How did we end up here?... SearchAction can't handle the action: " + action);
		}
	}
	
	
	
	

}
