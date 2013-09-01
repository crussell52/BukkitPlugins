package crussell52.poi;

import java.util.HashMap;
import java.util.Map;

import crussell52.poi.actions.SummaryAction;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import crussell52.poi.api.PoiEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

/**
 * @author crussell
 *
 * class to listen for and handle player events
 */
public class PointsOfInterestPlayerListener implements Listener {

	/**
	 * Used for all poi interactions
	 */
	private PoiManager _poiManager;

    /**
     * Reference to the main plugin.
     */
    private Plugin _plugin;

	/**
	 * Used to keep track of each player's last known "range" status.
	 */
	// TODO: move this into the PoiManager class
	private final Map<Player, Boolean> _playerRangeStatus = new HashMap<Player, Boolean>();

    /**
     * A record of the pending summary for each player. A pending summary is queued whenever
     * a player uses the compass and cancelled if they use it again within 10 server ticks
     * (double-click).
     */
    private final Map<Player, BukkitTask> _pendingSummaries = new HashMap<Player, BukkitTask>();

	/**
	 * Creates a new instance with a PoiManager to use forvPOI interactions.
	 */
	public PointsOfInterestPlayerListener(PoiManager poiManager, Plugin plugin) {
		this._poiManager = poiManager;
        this._plugin = plugin;
	}

    /**
     * A helper method which finds the next POI relative to the currently active POI.
     *
     * @param player The player to base the radius search on.
     *
     * @return This will be the next POI after the currently selected POI in an area search. If no
     *         POI is currently selected, then this will be the first POI in the search results. If
     *         no POIs are nearby or the current POI is already the last POI in the results, then
     *         this will be null.
     */
    private Poi _findNextPoi(Player player)
    {
        try {
            // Find nearby points of interest.
            PoiResults results = _poiManager.getNearby(player);
            if (results.size() == 0) {
                player.sendMessage("There are no nearby points of interest.");
                return null;
            }

            // See if there is a currently selected POI.
            Poi selected = _poiManager.getSelectedPoi(player);
            if (selected == null) {
                // There was no selected POI. Return the closest one.
                return results.get(0);
            }

            // See if the selected poi exists in the current result set.
            int selectedIndex = results.indexOf(selected);
            if (selectedIndex == -1) {
                // The POI is not in the current list of those nearby. Return the closest one.
                return results.get(0);
            }

            // The poi exists in the result set. See if it is the furthest POI
            if (selectedIndex == (results.size() - 1)) {
                // The furthest one was already selected. There is no next-furthest.
                return null;
            }

            // The POI was found in the current result set and was not last. Return the next
            // furthest away one.
            return results.get(selectedIndex + 1);

        } catch (Exception ignore) {
            player.sendMessage("There was a system error while looking for nearby POIs");
            return null;
        }
    }

    /**
     * EventHandler for player putting a compass in their hand.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEquipCompass(PlayerItemHeldEvent event) {
        try {
            Player player = event.getPlayer();
            if (player.getInventory().getItem(event.getNewSlot()).getType().equals(Material.COMPASS)) {
                player.sendMessage("");
                player.sendMessage(ChatColor.YELLOW + "-- Double-click \"attack\" button to change compass target. --");
                player.sendMessage(ChatColor.YELLOW + "-- Click \"attack\" button to see current details. --");
            }
        }
        catch (Exception ignore) {}
    }

    /**
     * EventHandler for left-clicking a compass.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCompassUse(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (!action.equals(Action.LEFT_CLICK_AIR) && !action.equals(Action.LEFT_CLICK_BLOCK)) {
            return;
        }

        if (event.hasItem() && event.getItem().getType() == Material.COMPASS) {

            // See if the player double-clicked.
            final Player player = event.getPlayer();
            if (_pendingSummaries.containsKey(player)) {
                // The first click set a task to run in 500 milliseconds which removes itself
                // from the list of pending summaries when it executes.  If this player still
                // has a pending summary, then they clicked twice within 500 ms. (Double-click!)
                BukkitTask pendingSummary = _pendingSummaries.get(player);

                // Cancel the pending summary and remove it from the list of pending ones.
                pendingSummary.cancel();
                _pendingSummaries.remove(player);

                // Don't allow the normal interaction caused by this click.
                event.setUseInteractedBlock(Event.Result.DENY);
                Poi poi = _findNextPoi(player);
                if (poi == null) {
                    // Unselect currently selected POI.
                    _poiManager.unselectPoi(player);
                }
                else {
                    // We have a POI to select.
                    _poiManager.selectPOI(poi, player);
                }

                this._setCompass(player);
            }
            else {
                // There was no pending summary yet, so queue one up.
                _pendingSummaries.put(
                        player, player.getServer().getScheduler().runTaskLater(
                            _plugin, new Runnable() {
                                @Override
                                public void run() {
                                    Poi summaryPoi = _poiManager.getSelectedPoi(player);
                                    if (summaryPoi == null && player.getCompassTarget() != null) {
                                        player.sendMessage("");
                                        player.sendMessage(ChatColor.YELLOW + "---- Spawn Location ----");
                                        player.sendMessage(PointsOfInterest.getDirections(
                                                player.getLocation().toVector(),
                                                player.getCompassTarget().toVector(),
                                                -1, ChatColor.WHITE));
                                    }
                                    else {
                                        // send a summary report to the user with a nice header.
                                        player.sendMessage("");
                                        player.sendMessage(ChatColor.YELLOW + "---- POI ----");
                                        SummaryAction.sendSummary(player, summaryPoi);
                                    }
                                    _pendingSummaries.remove(player);
                                }
                            }, 10));
            }
        }
    }

    private void _setCompass(Player player)
    {
        Poi poi = _poiManager.getSelectedPoi(player);
        if (poi == null) {
            // Point compass back to spawn location.
            Location compassLoc;
            compassLoc = player.getBedSpawnLocation();
            if (compassLoc == null) {
                compassLoc = player.getWorld().getSpawnLocation();
            }

            player.setCompassTarget(compassLoc);
            player.sendMessage("");
            player.sendMessage(ChatColor.YELLOW + "-- Compass changed to Spawn ----");
            player.sendMessage(PointsOfInterest.getDirections(
                    player.getLocation().toVector(),
                    compassLoc.toVector(),
                    -1, ChatColor.WHITE));
        }
        else {

            // Set compass target to this POI's location.
            player.setCompassTarget(new Location(player.getServer().getWorld(poi.getWorld()),
                    poi.getX(), poi.getY(), poi.getZ()));

            // Notify the user and give them a summary of what was selected.
            player.sendMessage("");
            player.sendMessage(ChatColor.YELLOW + "---- Compass changed to POI ----");
            SummaryAction.sendSummary(player, poi);
        }
    }

	/**
	 * EventHandler for player movement.
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
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
