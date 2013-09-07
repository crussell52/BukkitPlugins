package crussell52.poi.actions;

import crussell52.poi.commands.PoiCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import crussell52.poi.Poi;
import crussell52.poi.PoiManager;

import java.util.ArrayList;
import java.util.List;

public class SelectAction extends ActionHandler {

	/**
	 * {@inheritDoc}
	 *
	 * @param poiManager
	 */
	public SelectAction(PoiManager poiManager) {
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

		// attempt to select the POI
		if (this._selectPOI(args, 0, (Player)sender, action)) {
			Poi poi = this._poiManager.getSelectedPoi((Player)sender);
			sender.sendMessage("POI selected:");
			sender.sendMessage(poi.getShortSummary(ChatColor.WHITE));
		}
	}

    public static List<String> getHelp(boolean isShort) {
        ArrayList<String> messages = new ArrayList<String>();
        String basic = HelpAction.action(PoiCommand.ACTION_SELECT) + HelpAction.required("id");
        if (isShort) {
            basic += HelpAction.shortDescription("Select a POI");
            messages.add(basic);
            return messages;
        }

        messages.add(basic);
        messages.add(ChatColor.GREEN + "------------");
        messages.add(ChatColor.YELLOW + "Use this action to select a Point of Interest using its id.");
        messages.add(ChatColor.YELLOW + "Once you have selected a POI you can use " + HelpAction.actionXRef(PoiCommand.ACTION_SUMMARY) + " to");
        messages.add(ChatColor.YELLOW + "view a summary and directions. Your compass will automatically");
        messages.add(ChatColor.YELLOW + "point towards your selected POI. Use " + HelpAction.actionXRef("help compass") + " for");
        messages.add(ChatColor.YELLOW + "more details about your compass.");

        return messages;
    }

}
