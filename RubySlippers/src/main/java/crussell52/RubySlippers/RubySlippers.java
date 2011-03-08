package crussell52.RubySlippers;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * RubySlippers for Bukkit
 *
 * @author crussell52
 */
public class RubySlippers extends JavaPlugin {

    /**
     * Instance of Homes to manage homes for each player per world.
     */
    private final Homes _homes = new Homes();
    
    /**
     * {@inheritDoc}
     */
    public void onEnable() {
        // create files necessary for operation
    	_createSupportingFiles();
       
        // Identify that we have been loaded
        PluginDescriptionFile pdfFile = this.getDescription();
        System.out.println( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );

        // try to get homes data
        try {
        	_homes.load(this.getDataFolder());
        } catch (Exception ex) {
        	ex.printStackTrace();
        	System.out.println("Failed to load existing homes");
        	// TODO: maybe we should let all the users know on login?
        }
    }
    
    /**
     * Responsible for creating files necessary for operation
     */
    protected void _createSupportingFiles() {
    	try {
	    	this.getDataFolder().mkdir();
	    	new File(this.getDataFolder(), "homes.yml").createNewFile();
    	} catch (IOException ex) {
    		ex.printStackTrace();
    	}
    }
    
    /**
     * used to get the received player's home within their current world.
     * 
     * <p>If no home data is availble, then the world's spawn point is used.</p>
     * 
     * @param player
     * @return
     */
    private Location _getHome(Player player) {
    	// try to get the recorded home for the player
    	Location home = _homes.getHome(player);
    	
    	// see if we found a home
    	if (home == null) {
    		// no home found, use spawn location
    		// TODO: should we do this without notifying the user? maybe we just fail.
    		home = player.getWorld().getSpawnLocation();
    	}    	
    	
    	return home;
    }
    
    /**
     * Sends the player home, altering inventory as appropriate.
     * 
     * @param player
     * @param home
     */
    private void _sendHome(Player player, Location home) {
    	// TODO: very rough implementation
    	
    	int total = 0;
    	int totalRemove = 0;
    	int stackRemove = 0;
    	PlayerInventory inv = player.getInventory();
    	HashMap<Integer, ? extends ItemStack> stack = inv.all(Material.DIAMOND);

    	// TODO: Looping twice is less than efficient - find way accurately decrement in a single loop.
    	for (ItemStack value : stack.values()) {
    		total += value.getAmount();
    	}
    	
    	totalRemove = (int)Math.ceil(total * .10);
    	System.out.println("removing: " + Integer.toString(totalRemove));
    	for (ItemStack value : stack.values()) {
    		stackRemove = Math.min(totalRemove, value.getAmount());
    		value.setAmount(value.getAmount() - stackRemove);
    		totalRemove -= stackRemove;
    		if (totalRemove <= 0) {
    			// stop looping
    			break;
    		}
    	}
   	
    	player.teleportTo(home);
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
    	
    	// check for the ruby slippers command
    	// TODO: figure out the difference between commandLabel and command.getName() (aliases?)
    	if (commandLabel.equals("rs")) {
    		// make sure it was a plyer that issued the command
    		if (!(sender instanceof Player)) {
    			sender.sendMessage("This command does nothing from the server console.");
    			return true;
    		}
    		
    		// currently, all executions should have one or zero arguments
    		if (args.length > 1) {
    			return false;
    		}
    		
    		// passed basic validation, cast the sender as a player
    		// and evaluate the action
    		Player player = (Player) sender;
    		
    		// see if we have arguments
    		if (args.length == 0) {
    			// no argument, assume they were trying to put on their slippers 
    			// (this does nothing... just personal amusement)
    			player.sendMessage("You are now wearing your ruby slippers... they look fabulous!");
    			return false;
      		}
    		
			// kansas action is used to set home
			if ("kansas".equals(args[0])) {
				
				// set the players home
				Location newHome = _homes.setHome(player);
				
				// report to the player and to the console.
				// TODO: better formatted player message (report coordinates at all?)
				// TODO: once stable, stop reporting to console.
				player.sendMessage("This is now your home. (" + newHome.toString() + ")");
				System.out.println("RubySlippers: " + player.getDisplayName() + " set a new home at " + newHome.toString());
			}
			else if ("cost".equals(args[0])) {
				// cost is simply for now... always 10% of diamonds.
				// TODO: calculate what they will actually lose based on configuration
				player.sendMessage("You will lose 10% of your diamonds!");
			}
			else if ("tap".equals(args[0])) { 
				// get the player's home and send them there.
				Location home = _getHome(player);
				_sendHome(player, home);
				
				// TODO: stop reporting to console once stable.
				System.out.println("RubySlippers: " + player.getName() + " has been sent home: " + home.toString());
			}
			else {
				// unrecognized action
				return false;
			}
			
			// recognized and handled action
			return true;
		} // end handling of "rs" command
    	
    	// no other commands recognized, return false (displays usage)
    	return false;
    }
    
    
    /**
     * {@inheritDoc}
     */
    public void onDisable() {
        // TODO: Place any custom disable code here

        // NOTE: All registered events are automatically unregistered when a plugin is disabled

        // EXAMPLE: Custom code, here we just output some info so we can check all is well
        System.out.println("Ruby Slippers disabled.");
    }
}

