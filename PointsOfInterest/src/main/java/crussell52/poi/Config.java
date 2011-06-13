package crussell52.poi;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Logger;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

/**
 * This class contains static methods for 
 * getting configured values. 
 * 
 */
public class Config {

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
	 * Yaml instance for handling config data (.yml file)
	 */
	private static final Yaml _yaml = new Yaml(new SafeConstructor());
	
	// hide default constructor -- everything should be accessed statically
	private Config() {}
	
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
	 * Loads the configuration file located in the specified data folder.
	 * 
	 * @param dataFolder Where to look for the config file
	 * @param log Where to log problems - stack traces go to standard error out.
	 */
	@SuppressWarnings("unchecked")
	public static void load(File dataFolder, Logger log) {
		// keep a handle to the file so we don't need to re-instantiate every time it is used.
		File dataFile = new File(dataFolder, "config.yml");
		
		try {
			// parse the file as a map.
			FileInputStream input = new FileInputStream(dataFile);
			_processConfigMap((Map<String, Object>)_yaml.load(input), log);
			input.close();
		}
		catch (Exception ex) {
			log.severe("PointsOfInterest: Failed to load config file - trace to follow.");
			ex.printStackTrace();
		}
	}
	
	/**
	 * Receives the entire configuration in as a map and populates
	 * properties as appropriate.
     *
	 * @param map The configuration values parsed from the config file.
	 * @param log Where to log problems - stack traces go to standard error out.
	 */
	private static void _processConfigMap(Map<String, Object> map, Logger log) {
		// see if we have any data to process
		if (map == null) {
			return;
		}
		
		// see if a distance threshold is configured
		if (map.containsKey("distanceThreshold")) {
			try {
				_distanceThreshold = (Integer)map.get("distanceThreshold");
			}
			catch (Exception ex) {
				log.severe("PointsOfInterest: Bad configuration for distanceThreshold.");
			}
		}
		
		// see if a minimum distance between POIs is configured
		if (map.containsKey("minPoiGap")) {
			try {
				_minPoiGap = (Integer)map.get("minPoiGap");
			}
			catch (Exception ex) {
				log.severe("PointsOfInterest: Bad configuration for minPoiGap.");
			}
		}
		
		// see if a maximum number of search results
		if (map.containsKey("maxSearchResults")) {
			try {
				_maxSearchResults = (Integer)map.get("maxSearchResults");
			}
			catch (Exception ex) {
				log.severe("PointsOfInterest: Bad configuration for maxSearchResults.");
			}
		}
		
		// see if per-world maximum number of POIs for each player is configured
		if (map.containsKey("maxPlayerPoiPerWorld")) {
			try {
				_maxPlayerPoiPerWorld = (Integer)map.get("maxPlayerPoiPerWorld");
			}
			catch (Exception ex) {
				log.severe("PointsOfInterest: Bad configuration for maxPlayerPoiPerWorld.");
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
			}
		}		
	}
	
}
