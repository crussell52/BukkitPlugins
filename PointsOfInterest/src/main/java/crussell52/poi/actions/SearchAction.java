package crussell52.poi.actions;

import java.util.ArrayList;

import crussell52.poi.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SearchAction extends ActionHandler {

	/**
	 * Maximum number of POIs per results page.
	 */
	private static final int MAX_PER_PAGE = 3;

	/**
	 * {@inheritDoc}
	 *
	 * @param poiManager
	 */
	public SearchAction(PoiManager poiManager) {
		super(poiManager);

		this._relatedPermission = "poi.action.view";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleAction(CommandSender sender, String action, String[] args) {
		if (!this._canExecute(sender)){
			return;
		}

		// now that we know we have a player, we can safely cast
		Player player = (Player)sender;

		// attempt to get a list of nearby POIs
		try {
            PoiResults results = this._poiManager.getNearby(player);
            PagedPoiList pagedResults = new PagedPoiList(MAX_PER_PAGE, results, PagedPoiList.TYPE_AREA_SEARCH);

            this._poiManager.setPagedResults(player, pagedResults);

            ArrayList<String> messages = pagedResults.getPageReport(player.getLocation());
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
