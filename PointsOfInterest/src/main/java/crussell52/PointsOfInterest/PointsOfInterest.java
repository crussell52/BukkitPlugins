package crussell52.PointsOfInterest;


import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;


/**
 * RubySlippers for Bukkit
 *
 * @author crussell52
 */
public class PointsOfInterest extends JavaPlugin {
    
    /**
     * Parses config file and stores parsed values for use.
     * 
     * TODO: consider singleton to allow direct access by all classes
     */
    //private final ConfigParser _config = new ConfigParser();
	
	private final POIManager _poiManager = new POIManager();
    
	private Logger _log;
	
	private Map<Player, POI> _selectedPOIs = new HashMap<Player, POI>();
        
    /**
     * {@inheritDoc}
     */
    public void onEnable() {
    	// get a handle to the Minecraft logger
    	_log = Logger.getLogger("Minecraft");
    	
        // create files necessary for operation
    	_createSupportingFiles();
    	
    	// TODO: handle failure case.
    	_poiManager.initialize();
    	
        // Identify that we have been loaded
        PluginDescriptionFile pdfFile = this.getDescription();
        _log.info( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );        
    }
    
    
    
    /**
     * Responsible for creating files necessary for operation
     */
    protected void _createSupportingFiles() {
    	try {
	    	this.getDataFolder().mkdir();
	    	//new File(this.getDataFolder(), "homes.yml").createNewFile();
	    	//new File(this.getDataFolder(), "config.yml").createNewFile();
    	} catch (Exception ex) {
    		_log.severe("PointsOfInterest failed to create supporting files with error:" + ex);
    	}
    }
    
    
    /**
     * {@inheritDoc}
     */
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
    	
    	// check for the ruby slippers command
    	// TODO: figure out the difference between commandLabel and command.getName() (aliases?)
    	if (commandLabel.equalsIgnoreCase("poi")) {
    	
    		// make sure it was a player that issued the command
    		if (!(sender instanceof Player)) {
    			sender.sendMessage("This command does nothing from the server console.");
    			return true;
    		}
    		
    		// pull out the players location so we don't have to keep using the getter
    		Location playerLoc = ((Player)sender).getLocation();
    		if (args.length == 0) {
    			sender.sendMessage("You must specify an action.");
    			return true;
    		}
    		else if (args[0].equalsIgnoreCase("search")) {
    			// output a list of nearby POIs
    			try {
    				ArrayList<POI> poiList = _poiManager.getPOIs(playerLoc, 2000, 5);
    				sender.sendMessage("\u00a72" + poiList.size() + " POIs found:");
        			Iterator<POI> iterator = poiList.iterator();
        			int i = 0;
        			String colorCode;
        			while(iterator.hasNext()) {
        				POI poi = iterator.next();
        				colorCode = (++i % 2) == 0 ? "\u00a77" : "";
        				sender.sendMessage(colorCode + "[Id: " + poi.getId() + "] " + poi.getName() + " (Owner: " + poi.getOwner() + ")");
        			
            			int deltaX = (int)playerLoc.getX() - poi.getX();
            			int deltaY = (int)playerLoc.getY() - poi.getY();
            			int deltaZ = (int)playerLoc.getZ() - poi.getZ();
            			
            			int distance = (int)playerLoc.toVector().distance(poi.getVector());
            			
            			String message = colorCode + "    " + distance + " meters (";
            			message += (deltaX > 0 ? "North:" : "South:") + (int)Math.abs(deltaX) + ", ";
            			message += (deltaZ > 0 ? "East:" : "West:") + (int)Math.abs(deltaZ) + ", ";
            			message += (deltaY > 0 ? "Down:" : "Up:") + (int)Math.abs(deltaY) + ")";
            			sender.sendMessage(message);
            		}
    			}
    			catch (POIException poiEx) {
    				sender.sendMessage("There was a system error while looking for nearby POIs");
    				_log.severe(poiEx.toString());
    			}    			
    		}
    		else if (args[0].equalsIgnoreCase("select")) {
    			int id;
    			try {
    				id = Integer.parseInt(args[1]);
    			}
    			catch (Exception ex) {
    				sender.sendMessage("A numeric id must be specified.");
    				return true;
    			}
    			
    			try {
    				_selectedPOIs.put((Player)sender, _poiManager.getPOI(id, (Player)sender, 2000));
    			}
    			catch (POIException ex) {
    				if (ex.getErrorCode() == POIException.NO_POI_AT_ID) {
    					sender.sendMessage("No POI found with that id.");
    				}
    				else if (ex.getErrorCode() == POIException.POI_OUT_OF_WORLD) {
    					sender.sendMessage("You can not select a POI in another world, unless you own it.");
    				}
    				else if (ex.getErrorCode() == POIException.POI_OUT_OF_RANGE) {
    					sender.sendMessage("You can not select a POI which is that far away, unless you own it.");
    				}
    				else {
    					sender.sendMessage("An unexpected system error occured.");
    					_log.severe("Unexpected error while trying to get id.");
    					_log.severe(ex.toString());
    				}
    			}
    		}
    		else if (args[0].equalsIgnoreCase("add")) {
    			if (args.length != 2) {
    				sender.sendMessage("A name (without spaces) must be given."); 
    				return true;
    			}
    			
    			try {
    				_poiManager.addPOI(args[1], (Player)sender, 50);
    				sender.sendMessage("POI " + args[0] + " Created!");
    			}
    			catch (POIException poiEx) {
    				if (poiEx.getErrorCode() == POIException.TOO_CLOSE_TO_ANOTHER_POI) {
    					sender.sendMessage("You are too close to another POI.");
    				}
    				else {
    					_log.severe("There was an unexpected error while trying to add a location: " + args[1] + "|" + sender + "|" + 50);
    					_log.severe(poiEx.toString());
    					sender.sendMessage("There was a system error setting your POI.");
    				}
    			}
    			
    			return true;
    		}
    		else {
    			// right now, we only support 0 or 1 arguments.
    			return false;
    		}
    		
    		return true;
    	}
    	
    	return false;
    }
    
    
    /**
     * {@inheritDoc}
     */
    public void onDisable() {
        // TODO: Place any custom disable code here

        // NOTE: All registered events are automatically unregistered when a plugin is disabled

        // EXAMPLE: Custom code, here we just output some info so we can check all is well
        _log.info("PointsOfInterest disabled.");
    }
}

