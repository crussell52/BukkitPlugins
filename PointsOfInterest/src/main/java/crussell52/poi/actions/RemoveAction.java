package crussell52.poi.actions;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import crussell52.poi.Poi;
import crussell52.poi.PoiException;
import crussell52.poi.PoiManager;

public class RemoveAction extends ActionHandler {

	public RemoveAction(PoiManager poiManager) {
		super(poiManager);
	}

	@Override
	public void handleAction(CommandSender sender, String action, String[] args) {
		// you have to a player (for now) to do this.
		if (!this._playerCheck(sender)) {
			return;
		}
		
		if (args.length < 2) {
			this._actionUsageError(sender, "You must specify the name and ID of the POI to remove.", action);
			return;
		}
		
		try {
			// attempt to remove the POI
			// if anything goes wrong, an Exception will be thrown and caught.
			Player player = (Player)sender;
			int id = Integer.parseInt(args[0]);
			this._poiManager.removePOI(id, args[1], player.getName(), player.getWorld().getName());
			
			// if this was the player's selected POI, unselect it.
			Poi selected = this._poiManager.getSelectedPOI(player);
			if (selected != null && selected.getId() == id) {
				this._poiManager.unselectPoi(player);
			}
			
			// acknowledge success to the user.
			sender.sendMessage("POI removed!");
		}
		catch (PoiException ex) {
			String error;
			if (ex.getErrorCode() == PoiException.NO_POI_AT_ID) {
				error = "No POI with the specified Id.";
			}
			else if (ex.getErrorCode() == PoiException.POI_BELONGS_TO_SOMEONE_ELSE) {
				error = "You can not delete somebody else's POI.";
			}
			else if (ex.getErrorCode() == PoiException.POI_OUT_OF_WORLD) {
				error = "That POI is in another world.";
			}
			else if (ex.getErrorCode() == PoiException.POI_NAME_MISMATCH) {
				error = "The POI with that id has a different name.";
			}
			else {
				error = "A system error occured while trying to remove that POI.";
				ActionHandler._log.severe("Error trying to delete POI.");
				ActionHandler._log.severe(ex.toString());
			}
			
			sender.sendMessage(ChatColor.RED + error);
		}
		catch (NumberFormatException ex) {
			this._actionUsageError(sender, "ID is expected to be a number instead of: " + args[0], action);
		}
		
		
	}

}
