package crussell52.poi.actions;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import crussell52.poi.Poi;
import crussell52.poi.PoiException;
import crussell52.poi.PoiManager;

public class RemoveAction extends ActionHandler {

	/**
	 * {@inheritDoc}
	 *
	 * @param poiManager
	 */
	public RemoveAction(PoiManager poiManager) {
		super(poiManager);

		this._relatedPermission = "poi.action.remove";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleAction(CommandSender sender, String action, String[] args) {
		if (!this._canExecute(sender)){
			return;
		}

        int id;
        try {
            id = Integer.parseInt(args[0]);
        }
        catch (Exception e) {
            this._actionUsageError(sender, "The first argument should be the ID (a number).", action);
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
            String name = StringUtils.join(ArrayUtils.subarray(args, 1, args.length), " ");

            // see if player is allowed to remove others' POIs
			if (player.hasPermission("poi.action.remove.others")) {
				// can remove others, so don't qualify with player name or world.
				this._poiManager.removePOI(id, name);
			}
			else {
				// can only remove their own, so qualify with name and world
				// TODO: should they really be required to be in the same world as the POI?
				this._poiManager.removePOI(id, name, player.getName(), player.getWorld().getName());
			}

			// if this was the player's selected POI, unselect it.
			Poi selected = this._poiManager.getSelectedPoi(player);
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
				error = "A system error occurred while trying to remove that POI.";
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
