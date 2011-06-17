package crussell52.poi;

import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;

import crussell52.poi.api.PoiEvent;

/**
 * @author crussell
 * 
 * class to listen for and handle player events
 */
public class PointsOfInterestPlayerListener extends PlayerListener {

	/**
	 * Used for all poi interactions
	 */
	private PoiManager _poiManager;
	
	/**
	 * Used to keep track of each player's last known "range" status.
	 */
	// TODO: move this into the PoiManager class
	private final HashMap<Player, Boolean> _playerRangeStatus = new HashMap<Player, Boolean>();
	
	/**
	 * Creates a new instance with a PoiManager to use for 
	 * POI interactions.
	 */
	public PointsOfInterestPlayerListener(PoiManager poiManager) {
		this._poiManager = poiManager;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void onPlayerMove(PlayerMoveEvent event) {
		// see if the player has a poi
		Player player = event.getPlayer();
		Poi selectedPoi = this._poiManager.getSelectedPoi(player);
		if (selectedPoi == null) {
			// no selected poi, early exit.
			return;
		}
		
		// see if the player's new position is within range of the poi.
		boolean inRange = event.getTo().toVector().distance(selectedPoi.getVector()) <= Config.getDistanceThreshold();
		
		// see if their current range status represents a change from their last known status.
		if (!this._playerRangeStatus.containsKey(player) || this._playerRangeStatus.get(player) != inRange) {
			this._playerRangeStatus.put(player, inRange);
			PointsOfInterest._notifyListeners(PoiEvent.rangeEvent(player, selectedPoi, inRange));
		}
	}
}
