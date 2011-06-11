package crussell52.poi.actions;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import crussell52.poi.PoiException;
import crussell52.poi.PoiManager;
import crussell52.poi.PagedPoiList;

public class SearchAction extends ActionHandler {
	
	private static final int MAX_PER_PAGE = 3;
	
	public SearchAction(PoiManager poiManager) {
		super(poiManager);
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
		Location playerLoc = (player.getLocation());
		
		// attempt to get a list of nearby POIs
		try {
			PagedPoiList results = 
				new PagedPoiList(MAX_PER_PAGE, this._poiManager.getNearby(playerLoc, 2000, 10), PagedPoiList.TYPE_AREA_SEARCH);
			this._poiManager.setRecentResults(player, results);
			ArrayList<String> messages = results.getPageReport(playerLoc, 2000);
			sender.sendMessage("");
			for (String message : messages) {
				player.sendMessage(message);
			}
		}
		catch (PoiException poiEx) {
			ActionHandler._log.severe(poiEx.toString());
			sender.sendMessage("There was a system error while looking for nearby POIs");
		}
	}
}
