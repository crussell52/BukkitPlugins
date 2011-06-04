package crussell52.poi.actions;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import crussell52.poi.POI;
import crussell52.poi.POIManager;

public class SelectAction extends ActionHandler {

	public SelectAction(POIManager poiManager) {
		super(poiManager);
		this._isOwnerOnly = false;
	}

	@Override
	public void handleAction(CommandSender sender, String action, String[] args) {
		
		if (!this._playerCheck(sender)) {
			return;
		}
		
		if (this._selectPOI(args, 0, (Player)sender)) {
			POI poi = this._poiManager.getSelectedPOI((Player)sender);
			sender.sendMessage("POI selected:");
			sender.sendMessage(poi.getShortSummary(""));
		}		
	}

}
