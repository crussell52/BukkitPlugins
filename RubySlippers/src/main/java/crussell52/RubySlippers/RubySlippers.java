package crussell52.RubySlippers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
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
     * Parses config file and stores parsed values for use.
     * 
     * TODO: consider singleton to allow direct access by all classes
     */
    private final ConfigParser _config = new ConfigParser();
    
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
        
        // try to load up configurations
        try {
			_config.parse(this.getDataFolder());
		} catch (ClassCastException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    /**
     * Responsible for creating files necessary for operation
     */
    protected void _createSupportingFiles() {
    	try {
	    	this.getDataFolder().mkdir();
	    	new File(this.getDataFolder(), "homes.yml").createNewFile();
	    	new File(this.getDataFolder(), "config.yml").createNewFile();
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
    	// see if player is within teleporting range.
    	if (home.toVector().distance(player.getLocation().toVector()) > _config.getMaxDistance()) {
    		player.sendMessage("You are too far away from home.");
    		return;
    	}
    	
    	// within allowed teleport range... perform teleport
    	player.teleportTo(home);
    	
    	// TODO: actually deduct the items
    	_reportCosts(player);
    	
    	// TODO: stop reporting to console once stable.
		System.out.println("RubySlippers: " + player.getName() + " has been sent home: " + home.toString());
    }
    
    /**
     * Reports the costs of teleporting home with the current inventory
     * to the player.
     * 
     * @param player
     */
    private void _reportCosts(Player player) {
    	Map<Material, Integer> costs = _config.getCostManager().getCosts(player);
    	for (Map.Entry<Material, Integer> entry : costs.entrySet()) {
    		// TODO: Figure out ideal formatting
    		player.sendMessage(entry.getKey().name() + ":" + entry.getValue().toString());
    	}
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
    	
    	// check for the ruby slippers command
    	// TODO: figure out the difference between commandLabel and command.getName() (aliases?)
    	if (commandLabel.equals("rs")) {
    		// look for op commands
    		if (sender.isOp()) {
    			if (args.length > 0 && "config".equals(args[0])) {
    				// TODO: abstract this out.
    				try {
						_config.parse(this.getDataFolder());
					} catch (ClassCastException e) {
						e.printStackTrace();
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					sender.sendMessage("RubySlippers: Config file reloaded.");
					return true;
    			}
    		}
    		
    		// make sure it was a player that issued the command
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
				try {
					_homes.save();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				// report to the player and to the console.
				// TODO: better formatted player message (report coordinates at all?)
				// TODO: once stable, stop reporting to console.
				player.sendMessage("This is now your home. (" + newHome.toString() + ")");
				System.out.println("RubySlippers: " + player.getDisplayName() + " set a new home at " + newHome.toString());
			}
			else if ("cost".equals(args[0])) {
				// cost is simply for now... always 10% of diamonds.
				_reportCosts(player);
			}
			else if ("tap".equals(args[0])) { 
				// get the player's home and send them there.
				Location home = _getHome(player);
				_sendHome(player, home);			
				
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

