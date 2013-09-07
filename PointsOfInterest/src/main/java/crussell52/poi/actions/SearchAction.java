package crussell52.poi.actions;

import java.util.ArrayList;
import java.util.List;

import crussell52.poi.*;
import crussell52.poi.commands.PoiCommand;
import org.bukkit.ChatColor;
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

    public static List<String> getHelp(boolean isShort) {
        ArrayList<String> messages = new ArrayList<String>();
        String basic = HelpAction.action(PoiCommand.ACTION_SEARCH);
        if (isShort) {
            basic += HelpAction.shortDescription("Find nearby POIs");
            messages.add(basic);
            return messages;
        }

        messages.add(basic);
        messages.add(ChatColor.GREEN + "------------");
        messages.add(ChatColor.YELLOW + "Use this action to see the " + Config.getMaxSearchResults() + " closest Points of Interest");
        if (Config.getDistanceThreshold() >= 0) {
            messages.add(ChatColor.YELLOW + "within a " + Config.getDistanceThreshold() + " meter (block) radius. ");
        }
        else {
            messages.add(ChatColor.YELLOW + "within your current world.");
        }

        messages.add("");

        messages.add(ChatColor.YELLOW + "The first page of results will be displayed and");
        messages.add(ChatColor.YELLOW + HelpAction.actionXRef(PoiCommand.ACTION_PAGE) + " can be used to view the rest. The results ");
        messages.add(ChatColor.YELLOW + "will contain an id for each POI which can be used to");
        messages.add(ChatColor.YELLOW + "further interact with it. You can also cycle through nearby");
        messages.add(ChatColor.YELLOW + "POIs by double-clicking the \"use\" button while holding your");
        messages.add(ChatColor.YELLOW + "compass and not looking at a block. Use " + HelpAction.actionXRef("help compass"));
        messages.add(ChatColor.YELLOW + "for more details about your compass.");

        return messages;
    }
}
