package crussell52.RubySlippers;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * RubySlippers for Bukkit
 *
 * @author crussell52
 */
public class RubySlippers extends JavaPlugin {
	
	public static final String dataDir = "plugins/RubySlippers/"; 
	
    //private final RubySlippersPlayerListener playerListener = new RubySlippersPlayerListener(this);
    //private final RubySlippersBlockListener blockListener = new RubySlippersBlockListener(this);
    private final HashMap<Player, Boolean> debugees = new HashMap<Player, Boolean>();
    
    private final HashMap<String, WorldHomes> homes = new HashMap<String, WorldHomes>();
    
    public void onEnable() {
        // TODO: Place any custom enable code here including the registration of any events
    	
    	_createSupportingFiles();
    	
        // Register our events
        //PluginManager pm = getServer().getPluginManager();
       
        // EXAMPLE: Custom code, here we just output some info so we can check all is well
        PluginDescriptionFile pdfFile = this.getDescription();
        System.out.println( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
    }
    
    protected void _createSupportingFiles() {
    	try {
    		System.out.println("Trying to create directory");
	    	System.out.println(new File(dataDir).mkdir());
	    	System.out.println("Trying to create properties");
	    	System.out.println(new File(dataDir + "plugin.properties").createNewFile());
    	} catch (Exception ex) {
    		ex.printStackTrace();
    	}
    }
    
    protected Location _getHome(Player player) {
    	WorldHomes worldHomes = _getWorldHomes(player.getWorld());
    	return worldHomes.get(player);
    }
    
    protected void _setHome(Player player, Location home) {
    	WorldHomes worldHomes = _getWorldHomes(home.getWorld());
    	worldHomes.put(player, home);
    	try {
    		worldHomes.store();
    	} catch (IOException ex) {
    		// output to the console and let the player know that we did not save their home
    		String notification = "Failed to persist homes for world: " + home.getWorld(); 
    		System.out.println("RubySlippers: " + notification + ", player: " + player.getName());
    		player.sendMessage(notification);
    		player.sendMessage("(It will still work, unless the server restarts.)");
    		ex.printStackTrace();
    	}
    }
    
    protected WorldHomes _getWorldHomes(World world) {
    	// see if the world homes has already been initialized.
    	if (homes.containsKey(world.getName())) {
    		// nothing to do.
    		return homes.get(world.getName());
    	}
    	
    	// we don't have home data for this world
    	// create a new WorldHomes class and add it to the HashTable
    	WorldHomes worldHomes = new WorldHomes();
    	homes.put(world.getName(), worldHomes);
    	
    	// attempt to load existing data
    	try {
    		worldHomes.load(world);
    	} catch (IOException ex) {
    		// report failure to the console.
    		System.out.println("Unable to load home data for world: " + world.getName());
    		ex.printStackTrace();
    	}
    	
		// return the instance
		return worldHomes;
    }
    
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
    	
    	if (commandLabel.equals("ruby")) {
    		if (!(sender instanceof Player)) {
    			sender.sendMessage("This command does nothing from the server console.");
    			return true;
    		}
    		
    		if (args.length > 1) {
    			return false;
    		}
    		
    		Player player = (Player) sender;
    		
    		if (args.length == 0) {
    			player.sendMessage(command.getUsage());
    			player.sendMessage("You are now wearing your ruby slippers... they look fabulous!");
      		}
    		else {
    			if ("home".equals(args[0])) {
    				Location home = player.getLocation();
    				_setHome(player, home);
    				
    				player.sendMessage("This is now your home. (" + home.toString() + ")");
    				System.out.println("RubySlippers: " + player.getDisplayName() + " set a new home at " + home.toString());
    			}
    			else if ("tap".equals(args[0])) { 
					try {
						Location home = _getHome(player);
						
						if (home.toVector().distance(player.getLocation().toVector()) > 100) {
							player.sendMessage("You are too far from home. (Next version!)");
						}
						else {
							player.teleportTo(home);
	    					player.sendMessage("There's no place like home!");
	    					System.out.println("RubySlippers: " + player.getDisplayName() + " teleported home: " + home.toString());
						}
						
					} catch (Exception ex) {
						homes.remove(player.getName());
						System.out.println("Bad home stored for " + player.getName() + ".  Value cleared.");
					}
    			}
    		}
    
    	    return true;
    	}
    	
    	return false;
    }
    
    
    public void onDisable() {
        // TODO: Place any custom disable code here

        // NOTE: All registered events are automatically unregistered when a plugin is disabled

        // EXAMPLE: Custom code, here we just output some info so we can check all is well
        System.out.println("Goodbye world!");
    }
    public boolean isDebugging(final Player player) {
        if (debugees.containsKey(player)) {
            return debugees.get(player);
        } else {
            return false;
        }
    }

    public void setDebugging(final Player player, final boolean value) {
        debugees.put(player, value);
    }
}

