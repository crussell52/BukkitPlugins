package crussell52.RubySlippers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
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
    
    //private final PlayerListener playerListener = new RubySlippersPlayerListener();
    
    /**
     * List of unsafe materials.
     * 
     * @see RubySlippers#onEnable()
     */
	private final ArrayList<Material> _unsafeFooting = new ArrayList<Material>();
    
    /**
     * {@inheritDoc}
     */
    public void onEnable() {
        // create files necessary for operation
    	_createSupportingFiles();
    	
    	// set up list of unsafe footings, if we haven't already
    	if (_unsafeFooting.isEmpty()) {
    		_unsafeFooting.add(Material.AIR);
    		_unsafeFooting.add(Material.CACTUS);
    		_unsafeFooting.add(Material.WOOD_PLATE);
    		_unsafeFooting.add(Material.STONE_PLATE);
    		_unsafeFooting.add(Material.LAVA);
    		_unsafeFooting.add(Material.FIRE);
    	}
    	
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
        
        // PluginManager pm = getServer().getPluginManager();
        // pm.registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, playerListener, Priority.Normal, this);
        
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
     * @param player
     * @return
     */
    private Location _findSafeHome(Player player) {
    	// try to get the recorded home for the player
    	Location home = _homes.getHome(player);
    	
    	// if we have a home and it is unsafe, start making adjustments
    	if (home != null && !_isSafeHome(home)) {
    		// make a record of the original y
    		// this is what we will return to before making horizontal adjustments
	    	Double originalY = home.getY();
	    	
	    	// create a flag which can be updated as adjustments are made
	    	// we keep executing the adjustment loop until we find somewhere safe.
			boolean isSafe = false;
	    	
			// primary adjustment loop
	    	do {
	    		// we need to try and find a safe landing.
	    		// how we do this is going to vary based on whether there is air
	    		// underfoot.
	    		if (home.getBlock().getFace(BlockFace.DOWN).getType() == Material.AIR) {
	    			// there is air under us... start moving DOWN to find safe landing
	    			// we only move down until we have something under us which is NOT air.
	    			do {
	    				home.setY(home.getY() - 1);
	    			} while (home.getBlock().getFace(BlockFace.DOWN).getType() == Material.AIR);
	    			
	    			// we've made all possible adjustments down
	    			// update isSafe flag
	    			isSafe = _isSafeHome(home);
	    		}
	    		else {
	    			// it is not air underneath us... it is some other unsafe thing.
	    			// we want to move up, but never above 126
	    			while (!isSafe && home.getY() < 126) {
	    				// move one block up and check to see if new location is safe
	    				home.setY(home.getY() + 1);
	    				isSafe = _isSafeHome(home);
	    			}
	    		}
	    		
	    		// we've made all possible vertical adjustments
	    		// if we've made it this far and we still don't have a safe home, then
	    		// we need to reset to the original vertical position and shift 1 block to the south.
	    		if (!isSafe) {
	    			// update the x/y of the home
	    			home.setY(originalY);
	    			home.setX(home.getX() + 1);
	    			
	    			// check the new location and adjust isSafe flag appropriately
	    			// update the isSafe flag
	    			isSafe = _isSafeHome(home);
	    		}
	    		
	    	} while(!isSafe);
    	}
    	
    	// tweak home to make sure the player ends up in the center of the block
    	if (home != null) {
    		home.setX(Math.floor(home.getX()) + .5d);
    		home.setY(Math.floor(home.getY()));
    		home.setZ(Math.floor(home.getZ()) + .5d);
    	}
    	
    	// return the safe home.
    	return home;
    }
    
    /**
     * Helper function which analyzes a given location and returns
     * a boolean indicator of whether or not it is safe to teleport to.
     * 
     * @param home
     */
    private boolean _isSafeHome(Location home) {
    	return (!_unsafeFooting.contains(home.getBlock().getFace(BlockFace.DOWN).getType()) && home.getBlock().getType() == Material.AIR && home.getBlock().getFace(BlockFace.UP).getType() == Material.AIR);
    }
    
    /**
     * Sends the player home, altering inventory as appropriate.
     * 
     * @param player
     * @param home
     */
    private void _sendHome(Player player, Location home) {
    	// see if player is within teleporting range.
    	double distance = home.toVector().distance(player.getLocation().toVector());
    	
    	// make sure we arent' outside the max distance
    	if (_config.getMaxDistance() > 0 && distance > _config.getMaxDistance()) {
    		player.sendMessage("You are too far away from home.");
    		return;
    	}
    	else if (_config.getFreeDistance() > 0 && distance < _config.getFreeDistance()) {
    		// handle free teleport case.
    		player.teleport(home);
    		player.sendMessage("There's not place like home -- no charge!");
    		return;
    	}    	    	
    	
    	// within allowed teleport range... perform teleport
    	if (player.teleport(home)) {
    		// TODO: actually deduct the items
        	Map<Material, Integer> costs = _config.getCostManager().getCosts(player, true);
        	_reportCosts(costs, player);
        	player.sendMessage("There's not place like home!");
    	}
    	
    	// TODO: stop reporting to console once stable.
		System.out.println("RubySlippers: " + player.getName() + " has been sent home: " + home.toString());
    }
    
    /**
     * Reports the costs of teleporting home with the current inventory
     * to the player.
     * 
     * @param player
     */
    private void _reportCosts(Map<Material, Integer> costs, Player player) {
    	for (Map.Entry<Material, Integer> entry : costs.entrySet()) {
    		// TODO: Figure out ideal formatting
    		if (entry.getValue() > 0) {
    			player.sendMessage(entry.getKey().name() + ":" + entry.getValue().toString());
    		}
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
				// TODO: this should factor in maxDistance and the freeDistance values
	        	Map<Material, Integer> costs = _config.getCostManager().getCosts(player, false);
	        	_reportCosts(costs, player);
			}
			else if ("tap".equals(args[0])) { 
				// get the player's home and send them there.
				Location home = _findSafeHome(player);
				if (home != null) {
					_sendHome(player, home);
				}
				else {
					player.sendMessage("There's no place like home -- don't you wish you had one?");
				}
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

