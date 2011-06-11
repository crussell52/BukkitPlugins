package crussell52.poi;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import crussell52.poi.commands.PoiCommand;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

/**
 * PointsOfInterest for Bukkit
 *
 * @author crussell52
 */
public class PointsOfInterest extends JavaPlugin {
    	
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
    	this._poiManager.initialize(this.getDataFolder());
    	
    	Config.load(this.getDataFolder(), this._log);
    	
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
	    	
	    	File config = new File(this.getDataFolder(), "config.yml");
	    	if (!config.exists()) {
	    		this._createDefaultConfig(config);
	    	}
    	} catch (Exception ex) {
    		this._log.severe("PointsOfInterest failed to create supporting files with error:" + ex);
    	}
    }
    
    private void _createDefaultConfig(File target) {
    	
    	// wipe out the old config if it exists
    	if (target.exists()) {
    		target.delete();
    	}
    	InputStream input = this.getClass().getResourceAsStream("/resources/config.yml");
    	
    	// make sure we have a handle to the default config
        if (input != null) {
        	// set up a var for the output stream
        	FileOutputStream output = null;
        	
	        try {
	        	// attempt to make the copy
	        	output = new FileOutputStream(target);
	            byte[] buf = new byte[1024];
	            int chunkLength = 0;
	            while ((chunkLength = input.read(buf)) > 0) {
	                output.write(buf, 0, chunkLength);
	            }
	        } 
	        catch (Exception ex) {
	        	// something went wrong during the copy
	        	// Not the end of the world, we can use the coded defaults, but we should
	        	// generate a warning.
	            _log.warning("Failed to create default config file -- will use built in defaults. Exception to follow. ");
	            ex.printStackTrace();
	        } 
	        finally {
	        	// success or fail, make sure our input/output streams are closed
	        	
	        	// first the input stream...
	        	try {
	        		input.close();
	            } 
	        	catch (Exception e) {
	        		// do nothing.
	        	}
	        	
	        	// then the output stream.
	        	try {
	        		output.close();
	            } 
	        	catch (Exception e) {
	        		// do nothing.
	        	}
	        }
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

