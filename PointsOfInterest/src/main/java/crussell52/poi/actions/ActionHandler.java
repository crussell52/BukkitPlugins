package crussell52.poi.actions;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import crussell52.poi.PoiException;
import crussell52.poi.PoiManager;

public abstract class ActionHandler {
	
	protected static Logger _log = Logger.getLogger("Minecraft");
	
	protected PoiManager _poiManager;
	public ActionHandler(PoiManager poiManager) {
		this._poiManager = poiManager;
	}
	
	public abstract void handleAction(CommandSender sender, String action, String[] args);
	
	protected boolean _playerCheck(CommandSender sender)
	{
		if (!(sender instanceof Player)) {
			sender.sendMessage("This action can only be performed by an in-game Player.");
			return false;
		}
		
		return true;
	}
	
	protected void _actionUsageError(CommandSender recipient, ArrayList<String> messages, String action) {
		for (String message : messages) {
			recipient.sendMessage(ChatColor.RED + message);
		}
		
		recipient.sendMessage(ChatColor.RED + "Use " + ChatColor.DARK_RED + "\"/poi help\" for guidance."); 
	}
	
	protected void _actionUsageError(CommandSender recipient, String message, String action) {
		ArrayList<String> messages = new ArrayList<String>();
		messages.add(message);
		this._actionUsageError(recipient, messages, action);
	}
	
	protected boolean _selectPOI(String[] args, int expectedIndex, Player player, String action)
	{
		try {
			int	id = Integer.parseInt(args[0]);
			this._poiManager.selectPOI(id, player);
			return true;
		}
		catch (PoiException ex) {
			
			switch (ex.getErrorCode()) {
				case PoiException.NO_POI_AT_ID:
					player.sendMessage("No POI found with the specified id.");
					break;
				case PoiException.POI_OUT_OF_WORLD:
					player.sendMessage("Specified POI is in another world.");
					break;
				default:
					player.sendMessage("A system error occurred while trying to select the POI");
					ActionHandler._log.severe(ex.toString());
					break;
			}
			
			return false;
		}
		catch (IndexOutOfBoundsException ex) {
			this._actionUsageError(player, "ID must be specified.", action);
			return false;
		}
		catch (NumberFormatException ex) {
			this._actionUsageError(player, "ID must be a number.", action);
			return false;
		}
	}
}
