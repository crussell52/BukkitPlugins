package crussell52.poi.actions;

import java.util.ArrayList;
import java.util.List;

import crussell52.poi.commands.PoiCommand;
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

        // send a summary report to the user with a nice header.
        sender.sendMessage("");
        sender.sendMessage(ChatColor.YELLOW + "---- POI Summary ----");
        sendSummary((Player) sender, poi);
	}

    public static void sendSummary(Player recipient, Poi poi)
    {
        ArrayList<String> summaryReport =
                poi.getSummary(recipient.getLocation(), ChatColor.WHITE);
        for (String message : summaryReport) {
            recipient.sendMessage(message);
        }
    }

    public static List<String> getHelp(boolean isShort) {
        ArrayList<String> messages = new ArrayList<String>();
        String basic = HelpAction.action(null) + HelpAction.optional(PoiCommand.ACTION_SUMMARY) + HelpAction.optional("id");
        if (isShort) {
            basic += HelpAction.shortDescription("Get summary of selected POI");
            messages.add(basic);
            return messages;
        }

        messages.add(basic);
        messages.add(ChatColor.GREEN + "------------");
        messages.add(ChatColor.YELLOW + "Use this action to get a summary and directions of a Point");
        messages.add(ChatColor.YELLOW + "of Interest. If you provide an id, the related POI will");
        messages.add(ChatColor.YELLOW + "become your selected POI. If you do not provide an id, then");
        messages.add(ChatColor.YELLOW + "you will see a summary of your selected POI. You can also");
        messages.add(ChatColor.YELLOW + "get a summary of your current POI by clicking the \"use\"");
        messages.add(ChatColor.YELLOW + "button while holding a compass and not looking at a block.");
        messages.add(ChatColor.YELLOW + "Use" + HelpAction.actionXRef("help compass") + " for more details about your compass.");

        return messages;
    }

}
