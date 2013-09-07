package crussell52.poi.actions;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import crussell52.poi.Config;
import crussell52.poi.PoiManager;
import crussell52.poi.commands.PoiCommand;

import javax.swing.border.TitledBorder;

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
        String targetAction;
        if (args.length == 1) {
            targetAction = args[0];
        }
        else if (args.length == 2 && args[0].equalsIgnoreCase("actions")) {
            targetAction = args[1];
        }
        else {
            targetAction = PoiCommand.ACTION_HELP;
        }

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
        else if (targetAction.equalsIgnoreCase("actions")) {
            messages = _actionsHelp();
        }
        else if (targetAction.equalsIgnoreCase("compass")) {
            messages = _compassHelp();
        }
        else if (targetAction.equalsIgnoreCase("signs")) {
            messages = _signHelp();
        }
        else {
            // specified action does not have specific help
            // or is unrecognized, output general help.
            messages = this._generalHelp();
        }

        // output a blank line to improve readability and then output the messages.
        sender.sendMessage("");
        for (String message : messages) {
            sender.sendMessage(message);
        }
    }

    private ArrayList<String> _signHelp() {

        // This looks messy, but it works on on-screen. Mostly :)
        ArrayList<String> messages = new ArrayList<String>();
        messages.add(ChatColor.YELLOW + "You can create a Point of Interest with a sign like this:");
        messages.add(ChatColor.GOLD + "-----------                                   -------------");
        messages.add(ChatColor.GOLD + "|     [POI]     |                                  |   Title Line 1   |");
        messages.add(ChatColor.GOLD + "| Title Line 1 |  Which will change to -->   |   Title Line 2   |");
        messages.add(ChatColor.GOLD + "| Title Line 2 |                                  |    POI[7] by:    |");
        messages.add(ChatColor.GOLD + "|                |                                  | YourNameHere |");
        messages.add(ChatColor.GOLD + "-----------                                   -------------");
        messages.add(ChatColor.YELLOW + "You can remove it by simply destroying the sign. You can");
        messages.add(ChatColor.YELLOW + "also \"use\" a POI sign for more information about the POI.");

        return messages;
    }

    private ArrayList<String> _compassHelp() {

        ArrayList<String> messages = new ArrayList<String>();
        messages.add(ChatColor.YELLOW + "When you select a Point of Interest with " + actionXRef(PoiCommand.ACTION_SELECT) + ", your");
        messages.add(ChatColor.YELLOW + "compass will automatically target it. With a compass in your");
        messages.add(ChatColor.YELLOW + "hand, you can click the \"use\" button without targeting a");
        messages.add(ChatColor.YELLOW + "block to perform POI actions:");
        messages.add("");
        messages.add(ChatColor.GOLD + "Double-click to cycle through nearby Points of Interest. Once");
        messages.add(ChatColor.GOLD + "you cycle through all nearby POIs, your compass will go back");
        messages.add(ChatColor.GOLD + "to targeting your respawn location.");
        messages.add("");
        messages.add(ChatColor.GOLD + "Single-click to get a summary of your currently focused POI.");

        return messages;
    }

    private ArrayList<String> _generalHelp() {

        ArrayList<String> messages = new ArrayList<String>();

        messages.add(action(PoiCommand.ACTION_HELP) + " actions" + shortDescription("General help on available actions."));
        messages.add(action(PoiCommand.ACTION_HELP) + " actions" + required("action") + shortDescription("More help on a specific action"));
        messages.add(action(PoiCommand.ACTION_HELP) + " signs" + shortDescription("Help on using signs to manage POIs"));
        messages.add(action(PoiCommand.ACTION_HELP) + " compass" + shortDescription("Help on using your compass"));

        return messages;
    }

    /**
     * Returns a list of messages which form the general help.
     * @return
     */
    private ArrayList<String> _actionsHelp() {
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
