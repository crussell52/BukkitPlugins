package crussell52.poi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.DumperOptions.ScalarStyle;
import org.yaml.snakeyaml.Yaml;

/**
 * This class contains static methods for 
 * getting configured values. 
 * 
 */
public class Config {
	
	/**
	 * Indicates that only Ops can create POIs.
	 */
	private static boolean _restrictAddToOps = true;

	/**
	 * Dictates how far to search and maximum distance
	 * a player can be from a POI and still get directions
	 */
	private static int _distanceThreshold = 2000;
	
	/**
	 * Dictates minimum distance between POIs.  
	 */
	private static int _minPoiGap = 50;
	
	/**
	 * Maximum number of search results when a player does
	 * an area search.
	 */
	private static int _maxSearchResults = 10;
	
	/**
	 * Maximum number of POIs a player can create in each world.
	 */
	private static int _maxPlayerPoiPerWorld = 10;
	
	/**
	 * List of worlds in which POIs are not supported.
	 */
	private static ArrayList<String> _worldBlackList;
	
	/**
	 * keep a handle to the last data folder used for loading.
	 */
	private static File _dataFolder;
	
	/**
	 * Keep a handle to the last log file used.
	 */
	private static Logger _log;
	
	// hide default constructor -- everything should be accessed statically
	private Config() {}
	
	/**
	 * Indicates that only Ops can create POIs.
	 *
	 * @return
	 */
	public static boolean isAddRestrictedToOps() {
		return _restrictAddToOps;
	}
	
	/**
	 * Dictates how far to search and maximum distance
	 * a player can be from a POI and still get directions
	 * 
	 * @return 
	 */
	public static int getDistanceThreshold() {
		return _distanceThreshold;
	}
	
	/**
	 * Dictates minimum distance between POIs.  
	 * 
	 * @return
	 */
	public static int getMinPoiGap() {
		return _minPoiGap;
	}
	
	/**
	 * Maximum number of search results when a player does
	 * an area search.
	 * 
	 * @return
	 */
	public static int getMaxSearchResults() {
		return _maxSearchResults;
	}
	
	/**
	 * Maximum number of POIs a player can create in each world.
	 * 
	 * @return
	 */
	public static int getMaxPlayerPoiPerWorld() {
		return _maxPlayerPoiPerWorld;
	}
	
	/**
	 * List of worlds in which POIs are not supported.
	 */
	public static boolean isWorldSupported(String world) {
		return _worldBlackList == null || !_worldBlackList.contains(world.toLowerCase());
	}
	
	/**
	 * Reloads the config from the file.
	 * 
	 * @return
	 */
	public static boolean reload() {
		try {
			return Config.load(Config._dataFolder, Config._log);
		}
		catch (Exception ex) {
			System.out.println("Error when reloading config from file -- Was it ever loaded?.");
			ex.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Loads the configuration file located in the specified data folder.
	 * 
	 * @param dataFolder Where to look for the config file
	 * @param log Where to log problems - stack traces go to standard error out.
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static boolean load(File dataFolder, Logger log) {
		
		// setup a YAML instance for read/write of config file
		DumperOptions options = new DumperOptions();
		options.setDefaultFlowStyle(FlowStyle.BLOCK);
		options.setDefaultScalarStyle(ScalarStyle.PLAIN);
		Yaml yaml = new Yaml(options);
		
		// get a handle to the config file
		File dataFile = new File(dataFolder, "config.yml");
				
		if (!dataFile.exists()) {
			// try to create the file.
			try {
				dataFile.createNewFile();
			}
			catch (IOException e) {
				log.severe("PointsOfInterest: Failed to create config file - trace to follow.");
				e.printStackTrace();
				return false;
			}
		}
		else {
			// we have a config file, try to read it.
			FileInputStream input = null;
			try {
				// parse the file as a map.
				input = new FileInputStream(dataFile);
				if (!_processConfigMap((Map<String, Object>)yaml.load(input), log)) {
					// something was misconfigured.
					return false;
				}
			}
			catch (Exception e) {
				log.severe("PointsOfInterest: Failed to load or parse config file. Defaut configuration being used. - trace to follow.");
				e.printStackTrace();
				
				// problem opening or parsing the file.
				return false;
			}
			finally {
				// make sure we close the streams.
				// handle failures quietly.
				try { 
					input.close();
				}
				catch (Exception ex) {}
			}
		}
		
		// try to re-write the config file.
		// This will bring the file on disk up to date in the following ways:
		//   - Obsolete or incorrect config keys will be removed
		//   - Omitted keys (included ones introduced in an update) will 
		//     be written to have default values.
		FileWriter writer = null;
		try {
			// create a file writer in "overwrite" mode
			// and use Yaml to dump the data into the file.
			  writer = new FileWriter(dataFile, false);
			  
			  HashMap<String, Object> configMap = new HashMap<String, Object>();
			  configMap.put("distanceThreshold", Config._distanceThreshold);
			  configMap.put("minPoiGap", Config._minPoiGap);
			  configMap.put("maxSearchResults", Config._maxSearchResults);
			  configMap.put("maxPlayerPoiPerWorld", Config._maxPlayerPoiPerWorld);
			  configMap.put("worldBlacklist", Config._worldBlackList);
			  configMap.put("restrictAddToOps", Config._restrictAddToOps);
			  
			  yaml.dump(configMap, writer);	
		}
		catch (Exception e) {
			log.warning("PointsOfInterest: Failed to rewrite config file - trace to follow.");
			e.printStackTrace();
			
			// this doesn't warrant a failure -- we read everyting in fine, just weren't able
			// to tidy up the config file.
		} 
		finally {
			// make sure the writer is closed
			// handle failures to close quietly
			try {
				writer.close();
			}
			catch (Exception e) { }
		}
		
		// record the datafolder and logger for future use.
		Config._dataFolder = dataFolder;
		Config._log = log;
		
		// didn't hit any of the early exits, so everything went fine.
		return true;
	}
	
	/**
	 * Receives the entire configuration in as a map and populates
	 * properties as appropriate.
     *
	 * @param map The configuration values parsed from the config file.
	 * @param log Where to log problems - stack traces go to standard error out.
	 * 
	 * @return
	 */
	private static boolean _processConfigMap(Map<String, Object> map, Logger log) {
		// see if we have any data to process
		if (map == null) {
			// no data, easy success
			return true;
		}
		
		// start off assuming success
		// if there any problems, we'll flip this flag but we want
		// every key to get its opportunity to load.
		boolean success = true;
		
		// see if ops-only is configured
		if (map.containsKey("restrictAddToOps")) {

			try {
				_restrictAddToOps = (Boolean)map.get("restrictAddToOps");
			}
			catch (Exception ex) {
				log.severe("PointsOfInterest: Bad configuration for isAddOpsOnly.");
				success = false;
			}
		}

		// see if a distance threshold is configured
		if (map.containsKey("distanceThreshold")) {
			try {
				_distanceThreshold = (Integer)map.get("distanceThreshold");
			}
			catch (Exception ex) {
				log.severe("PointsOfInterest: Bad configuration for distanceThreshold.");
				success = false;
			}
		}
		
		// see if a minimum distance between POIs is configured
		if (map.containsKey("minPoiGap")) {
			try {
				_minPoiGap = (Integer)map.get("minPoiGap");
			}
			catch (Exception ex) {
				log.severe("PointsOfInterest: Bad configuration for minPoiGap.");
				success = false;
			}
		}
		
		// see if a maximum number of search results
		if (map.containsKey("maxSearchResults")) {
			try {
				_maxSearchResults = (Integer)map.get("maxSearchResults");
			}
			catch (Exception ex) {
				log.severe("PointsOfInterest: Bad configuration for maxSearchResults.");
				success = false;
			}
		}
		
		// see if per-world maximum number of POIs for each player is configured
		if (map.containsKey("maxPlayerPoiPerWorld")) {
			try {
				_maxPlayerPoiPerWorld = (Integer)map.get("maxPlayerPoiPerWorld");
			}
			catch (Exception ex) {
				log.severe("PointsOfInterest: Bad configuration for maxPlayerPoiPerWorld.");
				success = false;
			}
		}

		_worldBlackList = new ArrayList<String>();
		if (map.containsKey("worldBlacklist")) {
			try {
				@SuppressWarnings("unchecked")
				ArrayList<String> tmp = (ArrayList<String>)map.get("worldBlacklist");
				
				// it's reasonable for this key to have a null value
				if (tmp != null) {
					// convert each blacklisted world to lower-case before recording it
					for (String world : tmp) {
						_worldBlackList.add(world.toLowerCase());
					}
				}
			}
			catch (Exception ex) {
				log.severe("PointsOfInterest: Bad configuration for worldBlacklist.");
				success = false;
			}
		}
		
		// return success indicator
		return success;
	}
	
}
