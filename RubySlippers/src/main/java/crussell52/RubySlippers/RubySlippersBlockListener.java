package crussell52.RubySlippers;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.Material;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPhysicsEvent;

/**
 * RubySlippers block listener
 * @author crussell52
 */
public class RubySlippersBlockListener extends BlockListener {
    private final RubySlippers plugin;

    public RubySlippersBlockListener(final RubySlippers plugin) {
        this.plugin = plugin;
    }

    //put all Block related code here
}
