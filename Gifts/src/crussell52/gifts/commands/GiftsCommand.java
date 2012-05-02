/**
 * 
 */
package crussell52.gifts.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import crussell52.gifts.GiftManager;
import crussell52.gifts.actions.ActionHandler;
import crussell52.gifts.actions.HelpAction;
import crussell52.gifts.actions.ListAction;
import crussell52.gifts.actions.XPCollectAction;

import java.util.HashMap;
import java.util.Map;


/**
 * Delegates available POI actions off to appropriate ActionHandler subclasses.
 */
public class GiftsCommand implements CommandExecutor 
{
    // TODO: look into creating an enumeration for this.
    public static final String ACTION_LIST = "list";
    public static final String ACTION_XP_COLLECT = "xp";
    public static final String ACTION_HELP = "help";
	
	/**
	 * Performs the heavy lifting of POI interactions.
	 */
	private GiftManager _giftManager;
	
	/**
	 * Map between action strings and the ActionHandler subclass that should handle each.
	 */
	private final Map<String, ActionHandler> actionHandlers = new HashMap<String, ActionHandler>();

	/**
	 * Creates a new instance, receiving in the <code>PoiManager</code> to use for all POI interactions.
	 * 
	 * @param poiManager
	 */
    public GiftsCommand(GiftManager giftManager) 
    {
    	// record a handle to the poi manager
    	this._giftManager = giftManager;
    	
    	// set up action handlers for all available actions
    	actionHandlers.put(ACTION_LIST, new ListAction(this._giftManager));
        actionHandlers.put(ACTION_XP_COLLECT, new XPCollectAction(this._giftManager));
        actionHandlers.put(ACTION_HELP, new HelpAction(this._giftManager));
    }
    
    /**
     * {@inheritDoc}
     */
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) 
	{
		// if there wasn't an action provided, assume they want a list of gifts
		String action = (args.length == 0 ?  ACTION_LIST : args[0]);
		
		// get the appropriate action handler
		ActionHandler actionHandler = actionHandlers.get(action.toLowerCase());
		if (actionHandler == null) {
			// no action handler for this action
			sender.sendMessage("Unrecognized action.");
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
	private String[] _removeActionArg(String[] args) 
	{
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
