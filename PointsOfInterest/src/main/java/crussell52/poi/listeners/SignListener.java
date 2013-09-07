package crussell52.poi.listeners;

import crussell52.poi.*;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.plugin.Plugin;

import java.util.Iterator;
import java.util.List;

public class SignListener implements Listener {

    private PoiManager _poiManager;
    private Plugin _plugin;

    public SignListener(PoiManager poiManager, Plugin plugin) {
        _poiManager = poiManager;
        _plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSignBurn(BlockBurnEvent event) {
        if (_isPoiSign(event.getBlock()) != null || _hasAttachedPoiSign(event.getBlock()) != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSignBreak(BlockBreakEvent event) {
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
        if (poi.getOwner().equals(event.getPlayer().getName())) {
            try {
                _poiManager.removePOI(poi.getId(), "socks");
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

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChunkLoad(ChunkLoadEvent event) throws PoiException {
        final Chunk chunk = event.getChunk();
        try {
            final List<Poi> results = _poiManager.getChunkPoi(chunk);
            if (results.size() > 0) {
                _plugin.getServer().getScheduler().runTaskLater(_plugin, new Runnable() {
                    @Override
                    public void run() {
                        for (Poi poi : results) {
                            Block block = chunk.getWorld().getBlockAt(poi.getX(), poi.getY(), poi.getZ());
                            if (!PointsOfInterest.resemblesPoiSign(block)) {
                                block.setType(Material.SIGN_POST);
                            }

                            Sign sign = (Sign) block.getState();
                            String[] lines = new String[] {"", "", "", ""};
                            PointsOfInterest.setSignText(lines, poi.getName(), poi.getOwner(), poi.getId());
                            for (int i = 0; i < lines.length; i++) {
                                sign.setLine(i, lines[i]);
                            }
                            sign.update();
                        }
                    }
                }, 20);

            }
        } catch (PoiException e) {
            _plugin.getLogger().warning("Unable to load POIs on chunk load.");
            throw e;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event) {
        String[] lines = event.getLines();
        if (StringUtils.trimToEmpty(lines[0]).equalsIgnoreCase("[POI]")) {
            Player player = event.getPlayer();
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
        // Loop over every affected block. If any of them have a POI sign attached to it, then
        // the event must be cancelled.
        for (Block block : event.getBlocks()) {
            if (_hasAttachedPoiSign(block) != null) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSignMove(BlockPistonRetractEvent event) {
        if(_hasAttachedPoiSign(event.getRetractLocation().getBlock()) != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSignExplode(EntityExplodeEvent event) {
        // Loop over all affected blocks and remove all those which are poi signs or have
        // poi signs attached.
        for (Iterator<Block> it = event.blockList().iterator(); it.hasNext();) {
            Block block = it.next();
            if (_isPoiSign(block) != null || _hasAttachedPoiSign(block) != null) {
                it.remove();
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