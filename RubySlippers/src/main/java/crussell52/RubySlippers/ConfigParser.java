package crussell52.RubySlippers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

public class ConfigParser {
	/**
	 * CostManager Instance which is populated by the contents of the config file.
	 */
	private final CostManager _costManager = new CostManager();
	
	/**
	 * Maximum distance players are allowed teleport.
	 * 
	 * <p>Default value is 0 (zero), which indicates "no maximum"</p>
	 */
	private Integer _maxDistance = 0;
	
	/**
	 * Teleports from this distance or closer do not cost anything.
	 * 
	 * <p>Default value is 0 (zero), which indicates "never free"</p>
	 */
	private Integer _freeDistance = 0;
	
	/**
	 * Yaml instance for handling saved data (.yml file)
	 */
	private static final Yaml _yaml = new Yaml(new SafeConstructor());
	
	/**
	 * reference to the data file which is used to persist home data.
	 */
	private  File _dataFile;
	
	/**
	 * Returns the CostManager instance containing data from the last parse.
	 * 
	 * @return
	 */
	public CostManager getCostManager() {
		return _costManager;
	}
	
	/**
	 * Maximum distance players are allowed teleport.
	 * 
	 * <p>Default value is 0 (zero), which indicates "no maximum"</p>
	 */
	public int getMaxDistance() {
		return _maxDistance;
	}
	
	/**
	 * Teleports from this distance or closer do not cost anything.
	 * 
	 * <p>Default value is 0 (zero), which indicates "never free"</p>
	 */
	public int getFreeDistance() {
		return _freeDistance;
	}
	
	/**
	 * Loads saved data from config.yml under specified data folder, and parses
	 * the content.
	 * 
	 * @param dataFolder
	 * @throws IOException
	 * @throws ClassCastException
	 * @throws FileNotFoundException
	 */
	@SuppressWarnings("unchecked")
	public void parse(File dataFolder) throws IOException, ClassCastException, FileNotFoundException {
		// keep a handle to the file so we don't need to re-instantiate every time it is used.
		_dataFile = new File(dataFolder, "config.yml");
		
		try {
			// parse the file as a map.
			FileInputStream input = new FileInputStream(_dataFile);
			_processConfigMap((Map<String, Object>)_yaml.load(input));
			input.close();
		}
		catch (ClassCastException ex) {
			System.out.println("RubySlippers: Invalid config file: config.yml");
		}
	}
	
	/**
	 * Receives the entire configuration in as a map and populates
	 * properties as appropriate.
	 * 
	 * @param map
	 */
	private void _processConfigMap(Map<String, Object> map) {
		// see if we have any data to process
		if (map == null) {
			System.out.println("RubySlippers: Empty Configuration!");
			return;
		}
		
		// see if max distance is configured
		if (map.containsKey("maxDistance")) {
			try {
				_maxDistance = (Integer)map.get("maxDistance");
			}
			catch (Exception ex) {
				System.out.println("RubySlippers: Bad configuration for maxDistance.");
			}
		}
		
		// see if free distance is configured
		if (map.containsKey("freeDistance")) {
			try {
				_freeDistance = (Integer)map.get("freeDistance");
			}
			catch (Exception ex) {
				System.out.println("RubySlippers: Bad configuration for freeDistance.");
			}
		}
		
		// extract the default cost (if configured)
		if (map.containsKey("defaultCost")) {
			_costManager.setDefaultCost(ConfigParser.extractCost(map, "defaultCost", 0d));
		}
		
		// see if costs are configured
		if (map.containsKey("costs")) {
			// attempt to read in the data of the cost key
			try {
				// read config file into a local var so we can minimize the
				// range of the necessary @SuppressWarnings
				@SuppressWarnings("unchecked")
				HashMap<String, Object> materialCosts = (HashMap<String, Object>)map.get("costs");
				
				// hand the resulting map off to the CostManager for processing.
				_costManager.loadMaterialCosts(materialCosts);
			}
			catch (ClassCastException ex) {
				ex.printStackTrace();
				// Something was not in the format we were expecting.
				// report a problem with the cost configuration.
				System.out.println("RubySlippers: Invalid configuration key (costs).");
			}
		}
	}
	
	/**
	 * Given a key and a map, extracts the related Map item and attempts to convert it to a double.
	 * 
	 * <p>If the item can not be read in, then the value of <code>defaultVal</code> is returned.</p>
	 * 
	 * @param map
	 * @param key
	 * @param defaultVal
	 * @return
	 */
	public static Double extractCost(Map<String, Object> map, String key, Double defaultVal) 
	{
		double returnVal;
		// try to read in the cost as a double first
		try {
			returnVal = (Double)map.get(key);
		}
		catch (ClassCastException ex) {
			// failed reading it in as a double... 
			// it might be written as an int
			try {
				returnVal = (double)(Integer)map.get(key);
			}
			catch (ClassCastException ex2) {
				// Also not configured as an int... it is misconfigured.
				// notify the console
				System.out.println("RubySlippers: Invalid cost configured under key, \"" + key + "\"");
				return defaultVal;
			}
		}
		
		return returnVal;
	}
}
