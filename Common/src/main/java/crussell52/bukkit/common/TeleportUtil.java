package crussell52.bukkit.common;

import java.lang.IllegalArgumentException;
import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;



public class TeleportUtil
{
    /**
     * List of unsafe materials.
     */
    private static final ArrayList<Material> _unsafeFooting = new ArrayList<Material>();

    static
    {
        // Build out the list of unsafe materials to be standing on.
        _unsafeFooting.add(Material.AIR);
        _unsafeFooting.add(Material.CACTUS);
        _unsafeFooting.add(Material.WOOD_PLATE);
        _unsafeFooting.add(Material.STONE_PLATE);
        _unsafeFooting.add(Material.LAVA);
        _unsafeFooting.add(Material.FIRE);
    }

    /**
     * Helper function which analyzes a given location and returns
     * a boolean indicator of whether or not it is safe to teleport to.
     *
     * @param target
     */
    private boolean _isSafeHome(Location target) {
        return (!_unsafeFooting.contains(target.getBlock().getRelative(BlockFace.DOWN).getType()) && target.getBlock().getType() == Material.AIR && target.getBlock().getRelative(BlockFace.UP).getType() == Material.AIR);
    }

    /**
     * used to get the received player's home within their current world.
     *
     * @throws IllegalArgumentException
     *
     * @param target
     */
    public Location findSafeLanding(Location target) {
        if (target == null)
        {
            throw new IllegalArgumentException("target argument can not be null!");
        }

        Location safeLanding = target.clone();

        // if we have a home and it is unsafe, start making adjustments
        if (!_isSafeHome(safeLanding))
        {
            // make a record of the original y
            // this is what we will return to before making horizontal adjustments
            Double originalY = safeLanding.getY();

            // create a flag which can be updated as adjustments are made
            // we keep executing the adjustment loop until we find somewhere safe.
            boolean isSafe = false;

            // primary adjustment loop
            do {
                // we need to try and find a safe landing.
                // how we do this is going to vary based on whether there is air
                // underfoot.
                if (safeLanding.getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR) {
                    // there is air under us... start moving DOWN to find safe landing
                    // we only move down until we have something under us which is NOT air.
                    do {
                        safeLanding.setY(safeLanding.getY() - 1);
                    } while (safeLanding.getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR);

                    // we've made all possible adjustments down
                    // update isSafe flag
                    isSafe = _isSafeHome(safeLanding);
                }
                else {
                    // it is not air underneath us... it is some other unsafe thing.
                    // we want to move up, but never above 256
                    while (!isSafe && safeLanding.getY() < 256) {
                        // move one block up and check to see if new location is safe
                        safeLanding.setY(safeLanding.getY() + 1);
                        isSafe = _isSafeHome(safeLanding);
                    }
                }

                // we've made all possible vertical adjustments
                // if we've made it this far and we still don't have a safe home, then
                // we need to reset to the original vertical position and shift 1 block to the south.
                if (!isSafe) {
                    // update the x/y of the home
                    safeLanding.setY(originalY);
                    safeLanding.setX(safeLanding.getX() + 1);

                    // check the new location and adjust isSafe flag appropriately
                    // update the isSafe flag
                    isSafe = _isSafeHome(safeLanding);
                }

            } while(!isSafe);
        }

        // tweak target to make sure the player ends up in the center of the block
        safeLanding.setX(Math.floor(safeLanding.getX()) + .5d);
        safeLanding.setY(Math.floor(safeLanding.getY()));
        safeLanding.setZ(Math.floor(safeLanding.getZ()) + .5d);

        // return the safe location.
        return safeLanding;
    }
}
