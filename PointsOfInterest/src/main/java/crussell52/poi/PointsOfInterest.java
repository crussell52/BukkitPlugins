package crussell52.poi;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import crussell52.poi.api.IPointsOfInterest;
import crussell52.poi.api.PoiEvent;
import crussell52.poi.api.IPoiListener;
import crussell52.poi.commands.PoiCommand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * PointsOfInterest for Bukkit
 *
 * @author crussell52
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

    	final PluginManager pm = getServer().getPluginManager();
    	pm.registerEvents(new PointsOfInterestPlayerListener(this._poiManager, this ), this);

        // Identify that we have been loaded
        this._log.info( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
    }

    /**
     * Responsible for creating files necessary for operation
     */
    protected void _createSupportingFiles() {
    	try {
	    	if (!this.getDataFolder().exists() && !this.getDataFolder().mkdir()) {
                throw new Exception("Failed to create data directory.");
            }

    	} catch (Exception ex) {
    		this._log.severe("PointsOfInterest failed to create supporting files with error:" + ex);
    	}
    }
}

