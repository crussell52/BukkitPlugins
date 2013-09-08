package crussell52.poi.listeners;

import java.util.HashMap;
import java.util.Map;

import crussell52.poi.*;
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
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

/**
 * @author crussell
 *
 * class to listen for and handle player events
 */
public class PlayerListener implements Listener {

    /**
     * Used for all poi interactions
     */
    private PoiManager _poiManager;

    /**
     * Reference to the main plugin.
     */
    private Plugin _plugin;

    /**
     * Keeps track of the last time the player received a compass-use hint so as to not spam them.
     */
    private final Map<Player, Long> _compassHintCoolDown = new HashMap<Player, Long>();

    /**
     * A record of the pending summary for each player. A pending summary is queued whenever
     * a player uses the compass and cancelled if they use it again within 10 server ticks
     * (double-click).
     */
    private final Map<Player, BukkitTask> _pendingSummaries = new HashMap<Player, BukkitTask>();

    /**
     * Creates a new instance with a PoiManager to use forvPOI interactions.
     */
    public PlayerListener(PoiManager poiManager, Plugin plugin) {
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
        Player player = event.getPlayer();
        if (player.hasPermission("crussell52.poi.compass") && Config.isWorldSupported(player.getWorld().getName())) {
            try {
                if (player.getInventory().getItem(event.getNewSlot()).getType().equals(Material.COMPASS)) {
                    // See if we've provided the compass hint in the last 15 minutes.
                    if (_compassHintCoolDown.containsKey(player) &&
                            (System.currentTimeMillis() - _compassHintCoolDown.get(player)) <= 900000) {
                        return;
                    }

                    // Provide the a hint.
                    _compassHintCoolDown.put(player, System.currentTimeMillis());
                    player.sendMessage("");
                    player.sendMessage(ChatColor.YELLOW + "Use " + ChatColor.GOLD + "/poi help compass " + ChatColor.YELLOW + " for details on how to use your");
                    player.sendMessage(ChatColor.YELLOW + "compass to interact with Points of Interest.");
                }
            }
            catch (Exception ignore) {}
        }
    }

    /**
     * EventHandler for left-clicking a compass.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCompassUse(PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        if (player.hasPermission("crussell52.poi.compass") && Config.isWorldSupported(player.getWorld().getName())) {

            Action action = event.getAction();
            if (action == Action.RIGHT_CLICK_BLOCK) {
                Poi poi = _poiManager.getPoiAt(event.getClickedBlock().getLocation());
                if (poi != null) {
                    event.getPlayer().sendMessage("Welcome to " + poi.getName() + ", created by " + poi.getOwner() + "!");
                    return;
                }
            }

            if (event.hasItem() && event.getItem().getType() == Material.COMPASS && action == Action.RIGHT_CLICK_AIR) {
                // See if the player double-clicked.
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
                                                    ChatColor.WHITE));
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
                    ChatColor.WHITE));
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
}
