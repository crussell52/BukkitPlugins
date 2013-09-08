package crussell52.poi.listeners;

import crussell52.poi.*;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.world.ChunkLoadEvent;

import java.util.Iterator;

public class SignListener implements Listener {

    private PoiManager _poiManager;
    private PointsOfInterest _plugin;

    public SignListener(PoiManager poiManager, PointsOfInterest plugin) {
        _poiManager = poiManager;
        _plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSignBurn(BlockBurnEvent event) {
        if (Config.isWorldSupported(event.getBlock().getWorld().getName())) {
            if (_isPoiSign(event.getBlock()) != null || _hasAttachedPoiSign(event.getBlock()) != null) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSignBreak(BlockBreakEvent event) {
        if (Config.isWorldSupported(event.getBlock().getWorld().getName())) {
            // The event must be cancelled if the block being broken is a POI sign that belongs
            // to somebody other than the breaker.
            Poi poi = _isPoiSign(event.getBlock());
            if (poi == null) {
                // Not a POI, but does it have a POI sign attached?
                if (_hasAttachedPoiSign(event.getBlock()) != null) {
                    event.setCancelled(true);
                }
                return;
            }

            // It is a poi sign. See if the player doing the breaking owns it.
            Player player = event.getPlayer();
            if (poi.getOwner().equals(event.getPlayer().getName())) {
                try {
                    // See if the player has permission to remove POIs.
                    if (!player.hasPermission("poi.action.remove")) {
                        player.sendMessage("You do not have permission to remove POIs.");
                        event.setCancelled(true);
                        return;
                    }
                    _poiManager.removePOI(poi.getId(), poi.getName());
                    _poiManager.unselectPoi(player, poi.getId());
                    event.getPlayer().sendMessage("POI removed!");
                } catch (PoiException e) {
                    event.getPlayer().sendMessage(ChatColor.RED + "An error occurred while removing POI.");
                }
            }
            else {
                // Not the owner of the POI, cancel the breaking.
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChunkLoad(ChunkLoadEvent event) throws PoiException {
        if (Config.isWorldSupported(event.getWorld().getName())) {
            final int chunkX = event.getChunk().getX();
            final int chunkZ = event.getChunk().getZ();
            final String worldName = event.getWorld().getName();
            _plugin.getServer().getScheduler().runTaskLater(_plugin, new Runnable() {
                @Override
                public void run() {
                    _plugin.updateChunkSigns(worldName, chunkX, chunkZ);
                }
            }, 1);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event) {
        String[] lines = event.getLines();
        if (StringUtils.trimToEmpty(lines[0]).equalsIgnoreCase("[POI]")) {
            Player player = event.getPlayer();
            if (!Config.isWorldSupported(event.getBlock().getWorld().getName())) {
                event.setCancelled(true);
                player.sendMessage("Points of Interest are not allowed in this world.");
                return;
            }

            if (!player.hasPermission("poi.action.add")) {
                event.setCancelled(true);
                player.sendMessage("You do not have permission to create Points of Interest.");
                return;
            }

            // Cleanup the sign lines.
            lines[1] = StringUtils.trimToEmpty(lines[1]);
            lines[2] = StringUtils.trimToEmpty(lines[2]);
            lines[3] = StringUtils.trimToEmpty(lines[3]);

            // At least one line must be empty since POI names can only have two words.
            if (lines[1].equals("")) {
                player.sendMessage(ChatColor.RED + "The POI name must start on the 2nd line.");
                event.setCancelled(true);
                return;
            }

            if (!lines[3].equals("")) {
                player.sendMessage(ChatColor.RED + "The last line on a POI sign must be empty.");
                event.setCancelled(true);
                return;
            }

            String name = StringUtils.trim(lines[1] + " " + lines[2]);
            try {
                int id = this._poiManager.add(name, player.getName(), event.getBlock().getLocation(), Config.getMinPoiGap(), Config.getMaxPoiPerWorld(player));
                PointsOfInterest.setSignText(lines, lines[1], lines[2], player.getName(), id);
                player.sendMessage("POI " + name + " Created!");
            }
            catch (PoiException poiEx) {
                if (poiEx.getErrorCode() == PoiException.TOO_CLOSE_TO_ANOTHER_POI) {
                    player.sendMessage("You are too close to another POI.");
                }
                else if (poiEx.getErrorCode() == PoiException.MAX_PLAYER_POI_EXCEEDED) {
                    player.sendMessage("You have reached your maximum allowed POIs for this world.");
                }
                else {
                    _plugin.getLogger().severe("There was an unexpected error while trying to add a poi from a sign: " + name + "|" + player + "|" + Config.getMinPoiGap());
                    poiEx.printStackTrace();
                    player.sendMessage("There was a system error setting your POI.");
                }

                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSignMove(BlockPistonExtendEvent event) {
        if (Config.isWorldSupported(event.getBlock().getWorld().getName())) {
            // Loop over every affected block. If any of them have a POI sign attached to it, then
            // the event must be cancelled.
            for (Block block : event.getBlocks()) {
                if (_hasAttachedPoiSign(block) != null) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSignMove(BlockPistonRetractEvent event) {
        if (Config.isWorldSupported(event.getBlock().getWorld().getName())) {
            if(_hasAttachedPoiSign(event.getRetractLocation().getBlock()) != null) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSignExplode(EntityExplodeEvent event) {
        if (Config.isWorldSupported(event.getEntity().getWorld().getName())) {
            // Loop over all affected blocks and remove all those which are poi signs or have
            // poi signs attached.
            for (Iterator<Block> it = event.blockList().iterator(); it.hasNext();) {
                Block block = it.next();
                if (_isPoiSign(block) != null || _hasAttachedPoiSign(block) != null) {
                    it.remove();
                }
            }
        }
    }

    private Poi _isPoiSign(Block block) {
        if (PointsOfInterest.resemblesPoiSign(block)) {
            return _poiManager.getPoiAt(block.getLocation());
        }

        return null;
    }

    private boolean _hasAttachedPoiSign(Block subject, BlockFace relative)
    {
        subject = subject.getRelative(relative);
        if (relative == BlockFace.UP) {
            return subject.getType() == Material.SIGN_POST &&
                    _isPoiSign(subject) != null;
        }

        return subject.getType() == Material.WALL_SIGN &&
                ((org.bukkit.material.Sign) subject.getState().getData()).getFacing() == relative &&
                _isPoiSign(subject) != null;
    }

    private BlockFace _hasAttachedPoiSign(Block block) {

        if (_hasAttachedPoiSign(block, BlockFace.NORTH)) {
            return BlockFace.NORTH;
        }

        if (_hasAttachedPoiSign(block, BlockFace.SOUTH)) {
            return BlockFace.SOUTH;
        }

        if (_hasAttachedPoiSign(block, BlockFace.EAST)) {
            return BlockFace.EAST;
        }

        if (_hasAttachedPoiSign(block, BlockFace.WEST)) {
            return BlockFace.WEST;
        }

        if (_hasAttachedPoiSign(block, BlockFace.UP)) {
            return BlockFace.UP;
        }

        return null;
    }
}