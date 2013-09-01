package crussell52.poi.actions;

import java.util.ArrayList;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import crussell52.poi.PagedPoiList;
import crussell52.poi.PoiException;
import crussell52.poi.PoiManager;

public class OwnerListAction extends ActionHandler {

	/**
	 * Maximum number of items per page on the result list.
	 */
	private static final int MAX_PER_PAGE = 6;

	/**
	 * {@inheritDoc}
	 *
	 * @param poiManager
	 */
	public OwnerListAction(PoiManager poiManager) {
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

		if (args.length > 1) {
			this._actionUsageError(sender, "This action only expects one argument (for player name).", action);
		}

		String owner = (args.length == 0) ? sender.getName() : args[0];
		Player player = (Player)sender;

		// attempt to get a list of nearby POIs
		try {
			PagedPoiList results =
				new PagedPoiList(MAX_PER_PAGE, this._poiManager.getOwnedBy(player.getWorld(), owner), PagedPoiList.TYPE_OWNER_LIST);

			this._poiManager.setPagedResults(player, results);

			ArrayList<String> messages = results.getPageReport();
			sender.sendMessage("");
			for (String message : messages) {
				player.sendMessage(message);
			}
		}
		catch (PoiException poiEx) {
			ActionHandler._log.severe("Error getting POI by owner: " + poiEx.toString() + " - trace to follow.");
			poiEx.printStackTrace();
			sender.sendMessage("There was a system error while looking up POIs.");
		}
	}

}
