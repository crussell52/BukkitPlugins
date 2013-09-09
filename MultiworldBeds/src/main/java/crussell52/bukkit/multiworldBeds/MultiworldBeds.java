package crussell52.bukkit.multiworldBeds;

import crussell52.bukkit.common.CustomConfig;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class MultiworldBeds extends JavaPlugin implements Listener
{

    private CustomConfig _data;

    static
    {
        // Make sure BedSpawn instances can be aliased. This will (perhaps) make it less scary when
        // view/edit the .yml files.
        ConfigurationSerialization.registerClass(BedSpawn.class, "bedSpawn");
    }

    private String _getSpawnKey(Player player) {
        return player.getName() + "_" + player.getWorld().getName();
    }

//    // TODO: This belongs it it's own plugin or something!
//    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
//        if(cmd.getName().equalsIgnoreCase("wtime")){
//            // Don't allow the console to use this command!
//            if (!(sender instanceof Player)) {
//                sender.sendMessage("This command can not originate from the console.");
//                return true;
//            }
//
//            Player player = (Player)sender;
//
//            // Make sure the player has permissions for this command.
//            if (!player.hasPermission("crussell52.commands.wtime")) {
//                sender.sendMessage(ChatColor.YELLOW + "You can not adjust the time of this world.");
//                return true;
//            }
//
//
//            if (args.length < 2) {
//                sender.sendMessage(ChatColor.RED + "Incorrect usage. Correct usage:");
//                sender.sendMessage(ChatColor.RED + "/wtime set <value>");
//                sender.sendMessage(ChatColor.RED + "/wtime add <value>");
//                return true;
//            }
//
//            Integer timeValue;
//            if (args[0].equalsIgnoreCase("add")) {
//                try {
//                    timeValue = Integer.parseInt(args[1]);
//                }
//                catch (Exception e) {
//                    timeValue = 0;
//                }
//
//                player.getWorld().setFullTime(player.getWorld().getFullTime() + timeValue);
//                sender.sendMessage("Added " + timeValue.toString() + " to time");
//            } else if (args[0].equalsIgnoreCase("set")) {
//                try {
//                    timeValue = Integer.parseInt(args[1]);
//                }
//                catch (Exception e) {
//                    timeValue = args[1].equalsIgnoreCase("night") ? 12500 : 0;
//                }
//
//                player.getWorld().setFullTime(timeValue);
//                sender.sendMessage("Set time to " + timeValue.toString());
//            } else {
//                sender.sendMessage(ChatColor.RED + "Incorrect usage. Correct usage:");
//                sender.sendMessage(ChatColor.RED + "/wtime set <value>");
//                sender.sendMessage(ChatColor.RED + "/wtime add <value>" + ChatColor.WHITE);
//            }
//
//            return true;
//        }
//
//        // Unhandled command.
//        return false;
//    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onEnable()
    {
        this.getServer().getPluginManager().registerEvents(this, this);
        this._data = new CustomConfig(this, "bedSpawns.yml");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {

        if (event.isCancelled()) {
            return;
        }

        Block loc = event.getBed();
        BedSpawn bedSpawn = new BedSpawn(event.getPlayer().getWorld().getName(), loc.getX(), loc.getY(), loc.getZ());
        this._data.getConfig().set(this._getSpawnKey(event.getPlayer()), bedSpawn);
        this._data.saveConfig();
        this._setBedSpawn(event.getPlayer());
    }

    private void _setBedSpawn(Player player)
    {
        BedSpawn bedSpawn = (BedSpawn) this._data.getConfig().get(this._getSpawnKey(player));
        if (bedSpawn != null) {
            try {
                Location loc = bedSpawn.toLocation(this.getServer());
                player.setBedSpawnLocation(loc);
            }
            catch (Exception ignored) {}
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLogin(PlayerLoginEvent event) {
        _setBedSpawn(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        _setBedSpawn(event.getPlayer());
    }
}