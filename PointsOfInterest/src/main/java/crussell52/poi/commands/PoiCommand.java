/**
 * 
 */
package crussell52.poi.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import crussell52.poi.Config;
import crussell52.poi.PoiManager;
import crussell52.poi.actions.ActionHandler;
import crussell52.poi.actions.AddAction;
import crussell52.poi.actions.ConfigReload;
import crussell52.poi.actions.HelpAction;
import crussell52.poi.actions.OwnerListAction;
import crussell52.poi.actions.PageReportAction;
import crussell52.poi.actions.RemoveAction;
import crussell52.poi.actions.SearchAction;
import crussell52.poi.actions.SelectAction;
import crussell52.poi.actions.SummaryAction;

import java.util.HashMap;
import java.util.Map;


/**
 * Delegates available POI actions off to appropriate ActionHandler subclasses.
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
	public static final String ACTION_PAGE = "page";
	public static final String ACTION_LIST = "list";
	public static final String ACTION_HELP = "help";
	public static final String ACTION_RELOAD_CONFIG = "config";
	
	/**
	 * Performs the heavy lifting of POI interactions.
	 */
	private PoiManager _poiManager;
	
	/**
	 * Map between action strings and the ActionHandler subclass that should handle each.
	 */
	private final Map<String, ActionHandler> actionHandlers = new HashMap<String, ActionHandler>();

	/**
	 * Creates a new instance, receiving in the <code>PoiManager</code> to use for all POI interactions.
	 * 
	 * @param poiManager
	 */
    public PoiCommand(PoiManager poiManager) {
    	// record a handle to the poi manager
    	this._poiManager = poiManager;
    	
    	// set up action handlers for all available actions
    	actionHandlers.put(ACTION_ADD, new AddAction(this._poiManager));
    	actionHandlers.put(ACTION_SEARCH, new SearchAction(this._poiManager));
    	actionHandlers.put(ACTION_SELECT, new SelectAction(this._poiManager));
    	actionHandlers.put(ACTION_PAGE, new PageReportAction(this._poiManager));
    	actionHandlers.put(ACTION_SUMMARY, new SummaryAction(this._poiManager));
    	actionHandlers.put(ACTION_REMOVE, new RemoveAction(this._poiManager));
    	actionHandlers.put(ACTION_HELP, new HelpAction(this._poiManager));
    	actionHandlers.put(ACTION_LIST, new OwnerListAction(this._poiManager));
    	actionHandlers.put(ACTION_RELOAD_CONFIG, new ConfigReload());
    }
    
    /**
     * {@inheritDoc}
     */
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		// see if the command came from a black listed world
		if ((sender instanceof Player) && !Config.isWorldSupported(((Player)sender).getWorld().getName())) {
			sender.sendMessage("PointsOfInterest is not supported in this world.");
			return true;
		}
		
		// if there wasn't an action provided, assume they want a summary
		String action = (args.length == 0 ?  ACTION_SUMMARY : args[0]);
		
		// get the appropriate action handler
		ActionHandler actionHandler = actionHandlers.get(action.toLowerCase());
		if (actionHandler == null) {
			// no action handler for this action
			sender.sendMessage("Unrecognized action");
			return false;
		}
		
		// strip the action off the front of the arguments so that we can pass on the 
		// rest as action arguments.
		String[] otherArgs = this._removeActionArg(args);
		
		// let the action handler... well.. handle the action
		actionHandler.handleAction(sender, action, otherArgs);
		
		// we've done our job... return success.
		return true;
	}
	
	/**
	 * Assumes the first argument is the action argument and removes
	 * it from the array, returning a new array.
	 * 
	 * @param args
	 * @return
	 */
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
