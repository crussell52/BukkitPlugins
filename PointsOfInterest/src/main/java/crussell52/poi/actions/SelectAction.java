package crussell52.poi.actions;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import crussell52.poi.Poi;
import crussell52.poi.PoiManager;

public class SelectAction extends ActionHandler {

	/**
	 * {@inheritDoc}
	 * 
	 * @param poiManager
	 */
	public SelectAction(PoiManager poiManager) {
		super(poiManager);
		
		this._relatedPermission = "poi.action.view";
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleAction(CommandSender sender, String action, String[] args) {
		if (!this._canExecute(sender)){
			return;
		}
		
		// attempt to select the POI
		if (this._selectPOI(args, 0, (Player)sender, action)) {
			Poi poi = this._poiManager.getSelectedPoi((Player)sender);
			sender.sendMessage("POI selected:");
			sender.sendMessage(poi.getShortSummary(ChatColor.WHITE));
		}
	}

}
