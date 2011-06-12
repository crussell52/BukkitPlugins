package crussell52.poi.actions;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import crussell52.poi.Config;
import crussell52.poi.PoiException;
import crussell52.poi.PoiManager;

public class AddAction extends ActionHandler {

	public AddAction(PoiManager poiManager) {
		super(poiManager);
	}

	@Override
	public void handleAction(CommandSender sender, String action, String[] args) {
		// we need a player to perform this action
		if (!this._playerCheck(sender)) {
			return;
		}
		
		// we need a name to be specified.
		if (args.length < 1) {
			this._actionUsageError(sender, "A name must be provided.", action); 
			return;
		}
		
		// name should be the last argument...
		// it looks like they tried to include spaces.
		if (args.length > 1) {
			this._actionUsageError(sender, "POI name can not have spaces.", action);
			return;
		}
		
		// make sure the name doesn't exceed the max length.
		if (args[0].length() > PoiManager.MAX_NAME_LENGTH) {
			this._actionUsageError(sender, "Names can not be more that " + PoiManager.MAX_NAME_LENGTH + " characters long.", action);
			return;
		}
		
		try {
			// TODO: config - POIGap
			this._poiManager.add(args[0], (Player)sender, Config.getMinPoiGap(), Config.getMaxPlayerPoiPerWorld());
			sender.sendMessage("POI " + args[0] + " Created!");
		}
		catch (PoiException poiEx) {
			if (poiEx.getErrorCode() == PoiException.TOO_CLOSE_TO_ANOTHER_POI) {
				sender.sendMessage("You are too close to another POI.");
			}
			else if (poiEx.getErrorCode() == PoiException.MAX_PLAYER_POI_EXCEEDED) {
				sender.sendMessage("You have reached your maximum allowed POIs for this world.");
			}
			else {
				_log.severe("There was an unexpected error while trying to add a location: " + args[0] + "|" + sender + "|" + Config.getMinPoiGap());
				poiEx.printStackTrace();
				sender.sendMessage("There was a system error setting your POI.");
			}
		}
	}

}
