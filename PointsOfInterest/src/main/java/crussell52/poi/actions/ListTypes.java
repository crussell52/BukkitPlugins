package crussell52.poi.actions;

import java.util.ArrayList;
import java.util.List;

import crussell52.poi.commands.PoiCommand;
import crussell52.poi.config.Config;
import crussell52.poi.config.PoiType;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import crussell52.poi.Poi;
import crussell52.poi.PoiManager;

public class ListTypes extends ActionHandler {

    /**
     * {@inheritDoc}
     *
     * @param poiManager
     */
    public ListTypes(PoiManager poiManager) {
        super(poiManager);

        // Informational only, safe to use during lock-down.
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

        // see if we have any arguments
        if (args.length > 0) {
            this._actionUsageError(sender, "This action does not support any arguments.", action);
            return;
        }

        ChatColor color = ChatColor.GREEN;
        boolean first = true;
        StringBuilder output = new StringBuilder();
        for (PoiType poiType : Config.getPoiTypes()) {
            if (sender.hasPermission(Config.getPoiTypePerm(poiType.getID()))) {
                if (first) {
                    // Only first once.
                    first = false;
                }
                else {
                    output.append(ChatColor.RESET).append(", ");
                }

                output.append(color).append(poiType.getID());

                // Alternate colors.
                color = (color == ChatColor.GREEN ? ChatColor.DARK_GREEN : ChatColor.GREEN);
            }
        }

        sender.sendMessage("");
        if (output.length() > 0) {
            sender.sendMessage(ChatColor.YELLOW + "---- Available Types ----");
            sender.sendMessage(output.toString());
        }
        else {
            sender.sendMessage("There are no specific types available.");
        }
    }

    public static List<String> getHelp(boolean isShort) {
        ArrayList<String> messages = new ArrayList<String>();
        String basic = HelpAction.action(PoiCommand.ACTION_LIST_TYPES);
        if (isShort) {
            basic += HelpAction.shortDescription("See all available POI types.");
            messages.add(basic);
            return messages;
        }

        messages.add(basic);
        messages.add(ChatColor.GREEN + "------------");
        messages.add(ChatColor.YELLOW + "Use this action to get a list of all Point of Interest types");
        messages.add(ChatColor.YELLOW + "that are currently available to you. You can set the POI type");
        messages.add(ChatColor.YELLOW + "of a POI when it is created. Use " + HelpAction.action(PoiCommand.ACTION_HELP + " signs") + ChatColor.RESET + " for more");
        messages.add(ChatColor.YELLOW + "details about setting the POI type.");

        return messages;
    }

}
