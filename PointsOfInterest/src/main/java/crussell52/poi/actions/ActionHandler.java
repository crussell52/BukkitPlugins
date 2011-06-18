package crussell52.poi.actions;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import crussell52.poi.PoiException;
import crussell52.poi.PoiManager;

/**
 * Subclasses are responsible for handling specific actions.
 */
public abstract class ActionHandler {
	
	/**
	 * Used for logging as necessary throughout this class.
	 * 
	 * Exception stack traces are still output to the standard error out.
	 */
	protected static Logger _log = Logger.getLogger("Minecraft");
	
	/**
	 * Handles all of the "heavy lifting" for POI interactions.
	 */
	protected PoiManager _poiManager;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param poiManager Used for all POI interactions.
	 */
	public ActionHandler(PoiManager poiManager) {
		this._poiManager = poiManager;
	}
	
	/**
	 * Performs necessary tasks related to the action.
	 * 
	 * @param sender Who sent the action
	 * @param action the action which was requested by the sender
	 * @param args the arguments for the action execution.
	 */
	public abstract void handleAction(CommandSender sender, String action, String[] args);
	
	/**
	 * Used for actions that can only be performed by in-game players, this 
	 * verifies that the sender is a player.
	 * 
	 * If the sender is not a player, a message is sent to the sender letting them know
	 * why they can't execute the action.
	 * 
	 * @param sender
	 * @return
	 */
	protected boolean _playerCheck(CommandSender sender)
	{
		if (!(sender instanceof Player)) {
			sender.sendMessage("This action can only be performed by an in-game Player.");
			return false;
		}
		
		return true;
	}
	
	/**
	 * Sends the recipient a multi-line message letting them know that they have used the action
	 * incorrectly and provides them with instructions on getting help.
	 * 
	 * @param recipient
	 * @param messages
	 * @param action
	 */
	protected void _actionUsageError(CommandSender recipient, String message, String action) {
		recipient.sendMessage(ChatColor.RED + message);
		recipient.sendMessage(ChatColor.RED + "Use " + ChatColor.YELLOW + "\"/poi help\" " + ChatColor.RED + "for guidance."); 
	}
	
	/**
	 * Helper method for selecting a POI using information within the action arguments.
	 * 
	 * @param args
	 * @param expectedIndex
	 * @param player
	 * @param action
	 * @return
	 */
	protected boolean _selectPOI(String[] args, int expectedIndex, Player player, String action)
	{
		// make an attempt to get the expected index as an integer and select the POI
		try {
			int	id = Integer.parseInt(args[0]);
			this._poiManager.selectPOI(id, player);
			return true;
		}
		catch (IndexOutOfBoundsException ex) {
			// expected argument was missing
			this._actionUsageError(player, "ID must be specified.", action);
			return false;
		}
		catch (NumberFormatException ex) {
			// expected argument was not an integer
			this._actionUsageError(player, "ID must be a number.", action);
			return false;
		}
		catch (PoiException ex) {
			// there was a problem getting the POI
			switch (ex.getErrorCode()) {
				case PoiException.NO_POI_AT_ID:
					// Incorrect id provided
					player.sendMessage("No POI found with the specified id.");
					break;
				case PoiException.POI_OUT_OF_WORLD:
					// POI not in player's world.
					player.sendMessage("Specified POI is in another world.");
					break;
				default:
					// something unexpected occurred.
					player.sendMessage("A system error occurred while trying to select the POI");
					_log.severe("Failed to select POI by ID during action: " + action);
					ex.printStackTrace();
					break;
			}
			
			return false;
		}
		
	}
}
