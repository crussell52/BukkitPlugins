package crussell52.poi.actions;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import crussell52.poi.Poi;
import crussell52.poi.PoiManager;

public class SelectAction extends ActionHandler {

	public SelectAction(PoiManager poiManager) {
		super(poiManager);
	}

	@Override
	public void handleAction(CommandSender sender, String action, String[] args) {
		
		if (!this._playerCheck(sender)) {
			return;
		}
		
		if (this._selectPOI(args, 0, (Player)sender)) {
			Poi poi = this._poiManager.getSelectedPOI((Player)sender);
			sender.sendMessage("POI selected:");
			sender.sendMessage(poi.getShortSummary(""));
		}		
	}

}
