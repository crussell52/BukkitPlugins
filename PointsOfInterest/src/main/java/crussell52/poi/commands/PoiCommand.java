/**
 * 
 */
package crussell52.poi.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import crussell52.poi.PoiManager;
import crussell52.poi.actions.ActionHandler;
import crussell52.poi.actions.AddAction;
import crussell52.poi.actions.PageReportAction;
import crussell52.poi.actions.SearchAction;
import crussell52.poi.actions.SelectAction;

import java.util.HashMap;
import java.util.Map;


/**
 * @author crussell
 *
 */
public class PoiCommand implements CommandExecutor {
	
	// TODO: look into creating an enumeration for this.
	public static final String ACTION_RENAME = "rename";
	public static final String ACTION_REMOVE = "remove";
	public static final String ACTION_SET_DETAIL = "setdetail";
	public static final String ACTION_SUMMARY = "summary";
	public static final String ACTION_ADD = "add";
	public static final String ACTION_SEARCH = "search";
	public static final String ACTION_SELECT = "select";
	public static final String ACTION_LAST = "last";
	public static final String ACTION_LIST = "list";
	
	private PoiManager _poiManager;
	
	private final Map<String, ActionHandler> actionHandlers = new HashMap<String, ActionHandler>();

    public PoiCommand(PoiManager poiManager) {
    	this._poiManager = poiManager;
    	
    	actionHandlers.put(ACTION_ADD, new AddAction(this._poiManager));
    	actionHandlers.put(ACTION_SEARCH, new SearchAction(this._poiManager));
    	actionHandlers.put(ACTION_SELECT, new SelectAction(this._poiManager));
    	actionHandlers.put(ACTION_LAST, new PageReportAction(this._poiManager));
    }
    
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		// make sure it was a player that issued the command
		if (!(sender instanceof Player)) {
			sender.sendMessage("This must be a Player in the game to use this command.");
			return true;
		}
		
		String action = (args.length == 0 ?  ACTION_SUMMARY : args[0]);
		String[] otherArgs = this._removeActionArg(args);
		
		ActionHandler actionHandler = actionHandlers.get(action);
		if (actionHandler == null) {
			sender.sendMessage("Unrecognized action");
			return false;
		}
		
		actionHandler.handleAction(sender, action, otherArgs);
		
		return true;
	}
	
	private String[] _removeActionArg(String[] args) {
		// handle simple case of no arguments
		if (args.length == 0) {
			return new String[0];
		}
		
		// in all other cases remove the first argument
		// and return the rest as a new String[]
		String[] remaining = new String[args.length - 1];
		
		for (int i = 1; i < args.length; i++) {
			remaining[i - 1] = args[i];
		}
		
		return remaining;
	}

}
