package crussell52.poi;

import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import crussell52.poi.api.IPointsOfInterest;
import crussell52.poi.api.PoiEvent;
import crussell52.poi.api.IPoiListener;
import crussell52.poi.commands.PoiCommand;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * PointsOfInterest for Bukkit
 *
 * @author crussell52
 * @param <IPoiListener>
 */
public class PointsOfInterest extends JavaPlugin implements IPointsOfInterest 
{
	/**
	 * Does the heavy lifting for POI interactions.
	 */
	private final PoiManager _poiManager = new PoiManager();
    
	/**
	 * Used to log as necessary.
	 * 
	 * The stacktraces of critical exceptions are still output to the standard error out.
	 */
	private Logger _log;
	
	/**
	 * Used to keep track of who is listening
	 */
	private static final Map<PoiEvent.Type, ArrayList<IPoiListener>> _listeners = new HashMap<PoiEvent.Type, ArrayList<IPoiListener>>();
	
	/**
	 * {@inheritDoc}
	 */
	public void registerPoiListener(PoiEvent.Type type, IPoiListener poiListener) {
		// see if we have a container for this listener type
		if (!_listeners.containsKey(type)) {
			// we do not; create one.
			_listeners.put(type, new ArrayList<IPoiListener>());
		}
		
		// get the container for this type of listener
		ArrayList<IPoiListener> listenerList = _listeners.get(type); 
		
		// don't register the listener if it is already registered for this event.
		if (!listenerList.contains(poiListener)) {
			this._log.warning("Same IPoiListener registered more than once for the same event!");
			listenerList.add(poiListener);
		}
	}
	
	/**
	 * Used to notify all listeners when a PoiEvent occurs.
	 * 
	 * @param event
	 */
	static void _notifyListeners(PoiEvent event) {
		// nothing to do if we don't have listeners for this type of event.
		if (!_listeners.containsKey(event.getType())) {
			return;
		}
		
		// call the onEvent method of every listener for this type.
		for (IPoiListener listener : _listeners.get(event.getType())) {
			listener.onEvent(event);
		}
	}
	
    /**
     * {@inheritDoc}
     */
    public void onEnable() {
    	
    	// get plugin description
    	PluginDescriptionFile pdfFile = this.getDescription();
    	
    	// get a handle to the Minecraft logger
    	this._log = Logger.getLogger("Minecraft");

    	// create files necessary for operation
    	_createSupportingFiles();
    	
    	// attempt to load configuration
    	if(!Config.load(this.getDataFolder(), this._log)) {
    		// something went wrong reading in the config -- unsafe to run
    		this._log.severe(pdfFile.getName() + ": encountered problem loading config - Unsure if it is safe to run. Disabled.");
    		this.getServer().getPluginManager().disablePlugin(this);
    		return;
    	}
    	
    	// attempt to initialize the the poi manager.
    	if (!this._poiManager.initialize(this.getDataFolder())) {
    		this._log.severe(pdfFile.getName() + ": encountered problem preparing poi manager - Unsure if it is safe to run. Disabled.");
    		this.getServer().getPluginManager().disablePlugin(this);
    		return;
    	}
    	
    	// handle the poi command
    	getCommand("poi").setExecutor(new PoiCommand(this._poiManager));
    	
    	getServer().getPluginManager().registerEvent(Event.Type.PLAYER_MOVE, new PointsOfInterestPlayerListener(this._poiManager), Priority.Normal,this);
    	
        // Identify that we have been loaded
        this._log.info( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );        
    }
    
    /**
     * Responsible for creating files necessary for operation
     */
    protected void _createSupportingFiles() {
    	try {
	    	this.getDataFolder().mkdir();
	    	this._createConfigHelp();

    	} catch (Exception ex) {
    		this._log.severe("PointsOfInterest failed to create supporting files with error:" + ex);
    	}
    }
    
    /**
     * Used to copy the packaged config help out to the disk.
     * 
     * @param target the location on disk where the default config will be copied to.
     */
    private void _createConfigHelp() {
    	// wipe out the old config help if it exists
    	File target = new File(this.getDataFolder(), "config_help.txt");

    	if (target.exists()) {
    		target.delete();
    	}
    	
    	InputStream input = this.getClass().getResourceAsStream("/resources/config_help.txt");
    	
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
	            _log.warning("Failed to create config help file -- stacktrace to follow. ");
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

