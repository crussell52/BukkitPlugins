package crussell52.gifts;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import crussell52.gifts.commands.GiftsCommand;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gifts for Bukkit
 *
 * @author crussell52
 */
public class Gifts extends JavaPlugin 
{
	/**
	 * Does the heavy lifting for Gift interactions.
	 */
	private GiftManager _giftManager;
    
	/**
	 * Used to log as necessary.
	 * 
	 * The stacktraces of critical exceptions are still output to the standard error out.
	 */
	public Logger log;
	
    /**
     * {@inheritDoc}
     */
    public void onEnable() {
    	
    	// get plugin description
    	PluginDescriptionFile pdfFile = this.getDescription();
    	
    	// get a handle to the Minecraft logger
    	this.log = Logger.getLogger("Minecraft");

    	this.getConfig().options().copyDefaults(true);
        saveConfig();

        this._giftManager = new GiftManager(this);
        if (!this._giftManager.initialize(this.getConfig())) {
            this.log.log(Level.SEVERE, "Disabling plugin: Gifts");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
    	
    	// handle the gifts command
    	getCommand("gifts").setExecutor(new GiftsCommand(this._giftManager));
    	    	
        // Identify that we have been loaded
        this.log.info( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );        
    }
    
    /**
     * {@inheritDoc}
     */
    public void onDisable() {
        // TODO: Place any custom disable code here

        // NOTE: All registered events are automatically unregistered when a plugin is disabled

        // EXAMPLE: Custom code, here we just output some info so we can check all is well
        this.log.info("Gifts disabled.");
    }
}

