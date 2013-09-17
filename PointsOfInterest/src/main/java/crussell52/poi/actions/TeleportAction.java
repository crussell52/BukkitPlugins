package crussell52.poi.actions;

import crussell52.bukkit.common.TeleportUtil;
import crussell52.poi.Poi;
import crussell52.poi.PoiManager;
import crussell52.poi.commands.PoiCommand;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class TeleportAction extends ActionHandler {
    /**
     * Creates a new instance.
     *
     * @param poiManager Used for all POI interactions.
     */
    public TeleportAction(PoiManager poiManager) {
        super(poiManager);

        this._relatedPermission = "crussell52.poi.action.teleport";
    }

    @Override
    public void handleAction(CommandSender sender, String action, String[] args) {
        if (!this._canExecute(sender)){
            return;
        }

        if (args.length > 1) {
            _actionUsageError(sender, "Only one argument expected.", PoiCommand.ACTION_TELEPORT);
            return;
        }

        int id;
        try {
            id = Integer.parseInt(args[0]);
        }
        catch (Exception ex) {
            _actionUsageError(sender, "The id must be a number.", PoiCommand.ACTION_TELEPORT);
            return;
        }

        Poi poi = _poiManager.getPoi(id);
        if (poi == null) {
            _actionUsageError(sender, "Could not find a POI with that id.", PoiCommand.ACTION_TELEPORT);
            return;
        }

        Location poiLocation = poi.toLocation(sender.getServer());
        if (poiLocation == null) {
            sender.sendMessage(ChatColor.RED + "Unable to get the location of that POI.");
            return;
        }

        if (!((Player)sender).teleport((new TeleportUtil()).findSafeLanding(poiLocation))) {
            sender.sendMessage(ChatColor.RED + "An unknown force prevented you from teleporting!");
        }
    }

    public static List<String> getHelp(boolean isShort) {
        ArrayList<String> messages = new ArrayList<String>();
        String basic = HelpAction.action(PoiCommand.ACTION_TELEPORT) + HelpAction.required("id");
        if (isShort) {
            basic += HelpAction.shortDescription("Teleport directly to a POI.");
            messages.add(basic);
            return messages;
        }

        messages.add(basic);
        messages.add(ChatColor.GREEN + "------------");
        messages.add(ChatColor.YELLOW + "Use this action to teleport directly to a Point of Interest.");
        messages.add(ChatColor.YELLOW + "If the Point of Interest has become unsafe, you will be");
        messages.add(ChatColor.YELLOW + "teleported to a safe spot in the area.");

        return messages;
    }
}
