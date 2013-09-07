package crussell52.poi.actions;

import crussell52.poi.commands.PoiCommand;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import crussell52.poi.Config;
import crussell52.poi.PoiException;
import crussell52.poi.PoiManager;

import java.util.ArrayList;
import java.util.List;

public class AddAction extends ActionHandler {

	/**
	 * {@inheritDoc}
	 *
	 * @param poiManager
	 */
	public AddAction(PoiManager poiManager) {
		super(poiManager);

		this._relatedPermission = "poi.action.add";
	}

	/**
	 * {@inheritDoc}
	 */
	public void handleAction(CommandSender sender, String action, String[] args) {
		// make this command can be executed by the sender
		if (!this._canExecute(sender)) {
			return;
		}

		// we need a name to be specified.
		if (args.length < 1) {
			this._actionUsageError(sender, "A name must be provided.", action);
			return;
		}

        // Create a single name value from the args.
        String name = StringUtils.join(args, " ");

        // Make sure the name is not too long.
        if (name.length() > PoiManager.MAX_NAME_LENGTH) {
            sender.sendMessage("The name can not exceed " + PoiManager.MAX_NAME_LENGTH + " characters.");
        }

		try {
			this._poiManager.add(name, (Player)sender, Config.getMinPoiGap(), Config.getMaxPoiPerWorld((Player)sender));
			sender.sendMessage("POI " + name + " Created!");
		}
		catch (PoiException poiEx) {
			if (poiEx.getErrorCode() == PoiException.TOO_CLOSE_TO_ANOTHER_POI) {
				sender.sendMessage("You are too close to another POI.");
			}
			else if (poiEx.getErrorCode() == PoiException.MAX_PLAYER_POI_EXCEEDED) {
				sender.sendMessage("You have reached your maximum allowed POIs for this world.");
			}
			else {
				_log.severe("There was an unexpected error while trying to add a location: " + name + "|" + sender + "|" + Config.getMinPoiGap());
				poiEx.printStackTrace();
				sender.sendMessage("There was a system error setting your POI.");
			}
		}
	}

    public static List<String> getHelp(boolean isShort) {
        ArrayList<String> messages = new ArrayList<String>();
        String basic = HelpAction.action(PoiCommand.ACTION_ADD) + HelpAction.required("name");
        if (isShort) {
            basic += HelpAction.shortDescription("Create a POI");
            messages.add(basic);
            return messages;
        }

        messages.add(basic);
        messages.add(ChatColor.GREEN + "------------");
        messages.add(ChatColor.YELLOW + "Use this action to create a Point of Interest in the current");
        messages.add(ChatColor.YELLOW + "world. You must provide a name which is no more than 31");
        messages.add(ChatColor.YELLOW + "characters long.  Names do not need to be unique, but");
        messages.add(ChatColor.YELLOW + "consider using " + HelpAction.actionXRef(PoiCommand.ACTION_SEARCH) + "to look at nearby POIs before");
        messages.add(ChatColor.YELLOW + "choosing a name. You may also add a POI by placing a sign with");
        messages.add(ChatColor.YELLOW + "special text. Use " + HelpAction.actionXRef("help signs") + " for more details about");
        messages.add(ChatColor.YELLOW + "signs.");

        return messages;
    }

}
