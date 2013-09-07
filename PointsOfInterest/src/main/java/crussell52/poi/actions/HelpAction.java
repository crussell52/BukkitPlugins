package crussell52.poi.actions;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import crussell52.poi.Config;
import crussell52.poi.PoiManager;
import crussell52.poi.commands.PoiCommand;

public class HelpAction extends ActionHandler {

	/**
	 * {@inheritDoc}
	 *
	 * @param poiManager
	 */
	public HelpAction(PoiManager poiManager) {
		super(poiManager);
		this._lockdownOverride = true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleAction(CommandSender sender, String action, String[] args) {
		if (!this._canExecute(sender)){
			return;
		}

		// create a list to hold the messages that will be sent to the sender.
		List<String> messages;

		// if there is no action specified, then just treat it as if the help action was specified.
		// (which will output general help)
		String targetAction = (args.length > 0) ? args[0] : PoiCommand.ACTION_HELP;

		// try to output help for a specific action.
		if (targetAction.equalsIgnoreCase(PoiCommand.ACTION_LIST)) {
			messages = OwnerListAction.getHelp(false);
		}
        else if (targetAction.equalsIgnoreCase(PoiCommand.ACTION_RELOAD_CONFIG)) {
            messages = ConfigReload.getHelp(false);
        }
		else if (targetAction.equalsIgnoreCase(PoiCommand.ACTION_PAGE)) {
			messages = PageReportAction.getHelp(false);
		}
		else if (targetAction.equalsIgnoreCase(PoiCommand.ACTION_REMOVE)) {
			messages = RemoveAction.getHelp(false);
		}
		else if (targetAction.equalsIgnoreCase(PoiCommand.ACTION_SEARCH)) {
			messages = SearchAction.getHelp(false);
		}
		else if (targetAction.equalsIgnoreCase(PoiCommand.ACTION_SELECT)) {
			messages = SelectAction.getHelp(false);
		}
		else if (targetAction.equalsIgnoreCase(PoiCommand.ACTION_SUMMARY)) {
			messages = SummaryAction.getHelp(false);
		}
		else {
			// specified action does not have specific help
			// or is unrecognized, output general help.
			messages = this._generalHelp(sender.isOp());
		}

		// output a blank line to improve readability and then output the messages.
		sender.sendMessage("");
		for (String message : messages) {
			sender.sendMessage(message);
		}
	}

	/**
	 * Returns a list of messages which form the general help.
	 * @return
	 */
	private ArrayList<String> _generalHelp(boolean isOp) {
		ArrayList<String> messages = new ArrayList<String>();

		// set up alternation options for the legend
		ArrayList<String> alternation = new ArrayList<String>();
		alternation.add("this");
		alternation.add("that");
		alternation.add("other");

		messages.add(ChatColor.AQUA + "LEGEND: " + ChatColor.WHITE + "/poi action " +
				required("required") +  optional("optional")  + alternation(alternation, true));
		messages.add("------------------------------");

		// provide short usage for every action.
		messages.add(SelectAction.getHelp(true).get(0));
		messages.add(SummaryAction.getHelp(true).get(0));
		messages.add(RemoveAction.getHelp(true).get(0));
		messages.add(SearchAction.getHelp(true).get(0));
		messages.add(OwnerListAction.getHelp(true).get(0));
		messages.add(PageReportAction.getHelp(true).get(0));
        messages.add(ConfigReload.getHelp(true).get(0));
		messages.add(action(PoiCommand.ACTION_HELP) + required("action") + shortDescription("More help on a specific action"));
		messages.add(action(PoiCommand.ACTION_HELP) + required("signs") + shortDescription("Help on using signs to manage POIs"));
		messages.add(action(PoiCommand.ACTION_HELP) + required("compass") + shortDescription("Help on using your compass"));

		return messages;
	}

	/**
	 * Formats an action for use as a cross-referenced action in help text.
	 *
	 * @param action
	 * @return
	 */
	public static String actionXRef(String action) {
		return ChatColor.YELLOW + "\"" + ChatColor.GOLD + "/poi " + action + ChatColor.YELLOW + "\"";
	}

	/**
	 * Formats a token for use as an action in usage example text.
	 * @param token
	 * @return
	 */
	public static String action(String token) {
		return ChatColor.WHITE + "/poi" + (token != null ? " " + token : "");
	}

	/**
	 * Formats a token for use as short description of an action.
	 *
	 * @param token
	 * @return
	 */
	public static String shortDescription(String token) {
		return ChatColor.GRAY + " (" + token + ")";
	}


	/**
	 * Formats a token for use as a required argument of an action.
	 *
	 * @param token
	 * @return
	 */
	public static String required(String token) {
		return ChatColor.GOLD + " <" + ChatColor.WHITE + token + ChatColor.GOLD + ">";
	}

	/**
	 * Formats a token for use as an optional argument of an action.
	 *
	 * @param token
	 * @return
	 */
	public static String optional(String token) {
		return ChatColor.GREEN + " [" + ChatColor.WHITE + token + ChatColor.GREEN + "]";
	}

	/**
	 * Formats a list of tokens as a list of available values for an optional or required
	 * argument of an action.
	 *
	 * @param tokens
	 * @param isOptional
	 * @return
	 */
	public static String alternation(ArrayList<String> tokens, boolean isOptional) {
		String alternation = "";
		int numTokens = tokens.size();

		for (int i = 0; i < numTokens - 1; i++) {
			alternation += ChatColor.DARK_AQUA + tokens.get(i) + ChatColor.AQUA + " | ";
		}

		alternation += ChatColor.DARK_AQUA + tokens.get(numTokens - 1);

		return isOptional ? optional(alternation) : required(alternation);
	}

}
