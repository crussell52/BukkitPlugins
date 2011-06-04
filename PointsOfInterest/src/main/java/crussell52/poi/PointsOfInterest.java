package crussell52.poi;


import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import crussell52.poi.commands.PoiCommand;

import java.util.logging.Logger;


/**
 * PointsOfInterest for Bukkit
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
	
	private final PoiManager _poiManager = new PoiManager();
    
	private Logger _log;
	
    /**
     * {@inheritDoc}
     */
    public void onEnable() {
    	// get a handle to the Minecraft logger
    	this._log = Logger.getLogger("Minecraft");
    	
        // create files necessary for operation
    	_createSupportingFiles();
    	
    	// TODO: handle failure case.
    	this._poiManager.initialize();
    	
    	getCommand("poi").setExecutor(new PoiCommand(this._poiManager));
    	
        // Identify that we have been loaded
        PluginDescriptionFile pdfFile = this.getDescription();
        this._log.info( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );        
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
    		this._log.severe("PointsOfInterest failed to create supporting files with error:" + ex);
    	}
    }

    /**
     * {@inheritDoc}
     */
    public void onDisable() {
        // TODO: Place any custom disable code here

        // NOTE: All registered events are automatically unregistered when a plugin is disabled

        // EXAMPLE: Custom code, here we just output some info so we can check all is well
        this._log.info("PointsOfInterest disabled.");
    }
}

