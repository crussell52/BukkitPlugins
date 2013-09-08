package crussell52.poi.actions;

import java.util.ArrayList;
import java.util.List;

import crussell52.poi.commands.PoiCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


import crussell52.poi.Config;
import crussell52.poi.PagedPoiList;
import crussell52.poi.PoiManager;

public class PageReportAction extends ActionHandler {

    /**
     * {@inheritDoc}
     *
     * @param poiManager
     */
    public PageReportAction(PoiManager poiManager) {
        super(poiManager);

        this._relatedPermission = "crussell52.poi.view";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleAction(CommandSender sender, String action, String[] args) {
        if (!this._canExecute(sender)){
            return;
        }

        // make sure we don't have extra arguments
        if (args.length > 1) {
            this._actionUsageError(sender, "Too much info! This action only accepts a page number.", action);
            return;
        }

        // make sure this player has a result set in the current world.
        PagedPoiList results = this._poiManager.getPagedResults((Player)sender);
        if (results == null) {
            sender.sendMessage("You do not have any recent results in this World.");
            return;
        }

        // try to handle the first argument as a page number
        try {
            int pageNum = Integer.parseInt(args[0]);

            // set the new page
            if (!results.setPage(pageNum)) {
                sender.sendMessage("\u00a74Can't display page \u00a7e" + pageNum + "\u00a74...");
                sender.sendMessage("\u00a74There are only \u00a7e" + results.getNumPages() + "\u00a74 page(s) available.");
                return;
            }
        }
        catch (IndexOutOfBoundsException ex) {
            // let this condition slide, we'll use the current page.
        }
        catch (NumberFormatException ex) {
            // didn't get a number... check for special navigation values.
            if (args[0].equals(">")) {
                results.nextPage();
            }
            else if (args[0].equals(">>")) {
                results.lastPage();
            }
            else if (args[0].equals("<")) {
                results.previousPage();
            }
            else if (args[0].equals("<<")) {
                results.firstPage();
            }
            else {
                sender.sendMessage("Invalid page indicator, expecting: a number, >, >>, <, or <<");
                return;
            }
        }

        // if we made it this far, we can show the report.
        ArrayList<String> report;
        if (results.getListType() == PagedPoiList.TYPE_AREA_SEARCH) {
            report = results.getPageReport(((Player)sender).getLocation());
        }
        else {
            report = results.getPageReport();
        }

        // send the report to the command sender
        sender.sendMessage("");
        for (String message : report) {
            sender.sendMessage(message);
        }
    }

    public static List<String> getHelp(boolean isShort) {
        ArrayList<String> messages = new ArrayList<String>();

        // set up different possibilities for the argument
        ArrayList<String> alternation = new ArrayList<String>();
        alternation.add("number");
        alternation.add("<<");
        alternation.add("<");
        alternation.add(">");
        alternation.add(">>");

        String basic = HelpAction.action(PoiCommand.ACTION_PAGE) + HelpAction.alternation(alternation, true);
        if (isShort) {
            basic += HelpAction.shortDescription("Page through last results");
            messages.add(basic);
            return messages;
        }

        messages.add(basic);
        messages.add(ChatColor.GREEN + "------------");
        messages.add(ChatColor.YELLOW + "Use this action to page through your last list of Points of");
        messages.add(ChatColor.YELLOW + "Interest within the current world. If you provide a number,");
        messages.add(ChatColor.YELLOW + "that page will be displayed. You can also use <<, <, >, or >>");
        messages.add(ChatColor.YELLOW + "to view the first, previous, next, or last page. If you do not");
        messages.add(ChatColor.YELLOW + "provide anything, the current page will be redisplayed.");

        return messages;
    }

}
