package crussell52.poi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.entity.Player;
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
     * Current config id -- Only updated if the current plugin
     * version introduces a "high risk" change to the configuration
     * options.
     *
     * A mismatch between this value and the configId in the config file
     * will trigger an automatic lock down.
     */
    private static final Integer CURRENT_ID = 1;

    /**
     * Contains the singleton instance for this class
     */
    private static Config _instance;

    /**
     * Contains the template to use for outputting configuration to a file.
     */
    private static String _configTemplate;

    /**
     * If true, the plugin is operating in "lock down" mode.
     */
    private boolean _isLocked = true;

    /**
     * Dictates how far to search and maximum distance
     * a player can be from a POI and still get directions
     */
    private int _distanceThreshold = 2000;

    /**
     * Dictates minimum distance between POIs.
     */
    private int _minPoiGap = 50;

    /**
     * Maximum number of search results when a player does
     * an area search.
     */
    private int _maxSearchResults = 10;

    /**
     * Map of permission keys and the maximum POIs associated
     * with each.
     */
    private Map<String, Integer> _maxPoiMap;

    /**
     * Maximum number of POIs a player can create in each world
     * if not specified by _maxPoiMap.
     */
    private Integer _maxPlayerPoiPerWorld = 10;

    /**
     * List of worlds in which POIs are not supported.
     */
    private List<String> _worldBlackList = new ArrayList<String>();

    /**
     * List of worlds in which POIs are not supported.
     */
    private List<String> _mapMarkerWorlds = new ArrayList<String>();

    /**
     * List of worlds in which POIs are not supported.
     */
    private List<String> _mapMarkerWhitelist = new ArrayList<String>();

    /**
     * List of worlds in which POIs are not supported.
     */
    private List<String> _mapMarkerBlacklist = new ArrayList<String>();

    /**
     * keep a handle to the last data folder used for loading.
     */
    private static File _dataFolder;

    /**
     * Keep a handle to the last log file used.
     */
    private static Logger _log;

    // hide default constructor -- everything should be accessed statically
    private Config()
    {
        _maxPoiMap = new HashMap<String, Integer>();
    }

    public static Map<String, Integer> getMaxPoiMap()
    {
        return _instance._maxPoiMap;
    }

    /**
     * Indicates whether or not the plugin is in lockdown mode.
     *
     * @return If the plugin is locked then all game commands will be blocked.
     */
    public static boolean isLocked() {
        return _instance._isLocked;
    }

    /**
     * Dictates how far to search and maximum distance
     * a player can be from a POI and still get directions
     *
     * @return
     */
    public static int getDistanceThreshold() {
        return _instance._distanceThreshold;
    }

    /**
     * Dictates minimum distance between POIs.
     *
     * @return
     */
    public static int getMinPoiGap() {
        return _instance._minPoiGap;
    }

    /**
     * Maximum number of search results when a player does
     * an area search.
     *
     * @return
     */
    public static int getMaxSearchResults() {
        return _instance._maxSearchResults;
    }

    /**
     * returns the maximum number of POIs a given player is allowed.
     *
     * @param player
     * @return
     */
    public static int getMaxPoiPerWorld(Player player)
    {
        // start with no lowest
        Integer lowestMax = null;

        // loop over available maximums
        for (Map.Entry<String, Integer> entry : _instance._maxPoiMap.entrySet()) {
            // see if the player has the permission associated with this max
            _log.info("Looking for: " + entry.getKey());
            if (player.hasPermission("crussell52.poi.max." + entry.getKey())) {
                _log.info("Permission found. Value: " + entry.getValue());
                // player has the related permission, but we want the most restrictive
                // value... see if it is the lowest so far.  Note, -1 is a special case because
                // it is LEAST restrictive.
                if (lowestMax == null || (entry.getValue() < lowestMax && entry.getValue() != -1)) {
                    // lowest maximum so far.
                    _log.info("new lowest.");
                    lowestMax = entry.getValue();
                }
            }
        }

        // return the lowest max or the default if no explicit maximums were found.
        return lowestMax == null ? _instance._maxPlayerPoiPerWorld : lowestMax;
    }

    /**
     * Provides indicator of whether a given world has POIs enabled.
     */
    public static boolean isWorldSupported(String world) {
        return _instance._worldBlackList == null || !_instance._worldBlackList.contains(world.toLowerCase());
    }

    /**
     * Provides indicator of whether a given world supports POI map markers.
     */
    public static boolean isMapMarkerWorld(String world) {
        return isWorldSupported(world) && (_instance._mapMarkerWorlds == null || !_instance._mapMarkerWorlds.contains(world.toLowerCase()));
    }

    public static List<String> getMapMarkerWhitelist() {
        return _instance._mapMarkerWhitelist;
    }

    public static List<String> getMapMarkerBlacklist() {
        return _instance._mapMarkerBlacklist;
    }

    /**
     * Reloads the config from the file.
     *
     * @return
     */
    public static boolean reload() {

        if (_instance == null) {
            _log.severe("Tried to reload config before it had been loaded.");
            return false;
        }

        Config previous = _instance;
        if (!_load(_dataFolder, _log)) {
            _log.severe("Rolling back to last known configuration.");
            _instance = previous;
            return false;
        }

        return true;
    }

    /**
     * Loads the configuration file located in the specified data folder.
     *
     * @param dataFolder Where to look for the config file
     * @param log Where to log problems - stack traces go to standard error out.
     *
     * @return
     */
    public static boolean load(File dataFolder, Logger log) {

        if (!_load(dataFolder, log)) {
            _instance = new Config();
            return false;
        }

        return true;
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
    private static boolean _load(File dataFolder, Logger log) {

        // always start with a new instance.
        _instance = new Config();
        _log = log;

        // setup a YAML instance for read/write of config file
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(FlowStyle.BLOCK);
        options.setDefaultScalarStyle(ScalarStyle.PLAIN);
        Yaml yaml = new Yaml(options);

        // get a handle to the config file
        File dataFile = new File(dataFolder, "config.yml");

        if (!dataFile.exists()) {
            // log a warning about the lack of config file.
            log.warning("PointsOfInterest: No configuration file found -- assuming initial plugin install.");

            // try to create the file.
            try {
                if (!dataFile.createNewFile()) {
                    throw new Exception("File.createNewFile() returned false.");
                }
            }
            catch (Exception e) {
                log.severe("PointsOfInterest: Failed to create config file - trace to follow.");
                e.printStackTrace();
                return false;
            }

            // we will be defaulting to lockdown mode -- log a warning
            log.warning("The plugin has been forced into lockdown mode to give you a chance to adjust " +
                    "your configuration settings.");
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
                log.severe("PointsOfInterest: Failed to load or parse config file - trace to follow.");
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
                catch (Exception ignored) {}
            }
        }

        // see if we are in lockdown (either forced or manual)
        if (_instance._isLocked) {
            // Make appropriate log entries
            log.warning("Operating in \"lock down\" mode. To release the lock, please update the related " +
                        "configuration option and run the command to reload the config OR restart the server.");
        }

        // try to re-write the config file.
        // This will bring the file on disk up to date in the following ways:
        //   - Obsolete or incorrect config keys will be removed
        //   - Omitted keys (included ones introduced in an update) will
        //     be written to have default values.

        // See if we already have a configuration template for output
        if (_configTemplate == null) {
            // Read in the template.
            InputStream templateInput = null;
            InputStreamReader reader = null;

            try {
                // read in the template
                templateInput = (new Config()).getClass().getResourceAsStream("/resources/config_tpl.txt");
                reader = new InputStreamReader(templateInput, "UTF-8");
                char[] buffer = new char[1024];
                Integer read;
                StringBuilder stringBuilder = new StringBuilder();
                do {
                    read = reader.read(buffer, 0, buffer.length);
                    if (read > 0) {
                        stringBuilder.append(buffer, 0, read);
                    }
                } while (read >= 0);

                _configTemplate = stringBuilder.toString();
            }
            catch (Exception e) {
                log.severe("PointsOfInterest: Failed to read in config template. Can't write current config values to file. Trace to follow.");
                e.printStackTrace();
            }
            finally {
                // Close off resources. No recourse in the case of failures, so just ignore
                // exceptions.
                try {
                    reader.close();
                }
                catch (Exception ignored) { }

                try {
                    templateInput.close();
                }
                catch (Exception ignored) { }
            }
        }

        // See if we have a config template, now.
        if (_configTemplate != null) {
            // We have a config template... Time out output our config values.
            FileWriter writer = null;

            try {
                // create a hash map...
                // we will feed the config keys into it (one at a time) and use
                // Yaml to get the YAML representation for output.
                HashMap<String, Object> configMap = new HashMap<String, Object>();

                configMap.put("distanceThreshold", _instance._distanceThreshold);
                String output = _configTemplate.replace("#{distanceThreshold}#", yaml.dump(configMap));

                configMap.clear();
                configMap.put("configId", Config.CURRENT_ID); // always current
                output = output.replace("#{configId}#", yaml.dump(configMap));

                configMap.clear();
                configMap.put("lockDown", _instance._isLocked);
                output = output.replace("#{lockDown}#", yaml.dump(configMap));

                configMap.clear();
                configMap.put("minPoiGap", _instance._minPoiGap);
                output = output.replace("#{minPoiGap}#", yaml.dump(configMap));

                configMap.clear();
                configMap.put("maxSearchResults", _instance._maxSearchResults);
                output = output.replace("#{maxSearchResults}#", yaml.dump(configMap));

                configMap.clear();
                configMap.put("maxPlayerPoiPerWorld", _instance._maxPlayerPoiPerWorld);
                output = output.replace("#{maxPlayerPoiPerWorld}#", yaml.dump(configMap));

                configMap.clear();
                configMap.put("crussell52.poi.max", _instance._maxPoiMap);
                output = output.replace("#{crussell52.poi.max}#", yaml.dump(configMap));

                configMap.clear();
                configMap.put("worldBlacklist", _instance._worldBlackList);
                output = output.replace("#{worldBlacklist}#", yaml.dump(configMap));

                configMap.clear();
                configMap.put("mapMarkerWorlds", _instance._mapMarkerWorlds);
                output = output.replace("#{mapMarkerWorlds}#", yaml.dump(configMap));

                configMap.clear();
                configMap.put("mapMarkerWhitelist", _instance._mapMarkerWhitelist);
                output = output.replace("#{mapMarkerWhitelist}#", yaml.dump(configMap));

                configMap.clear();
                configMap.put("mapMarkerBlacklist", _instance._mapMarkerBlacklist);
                output = output.replace("#{mapMarkerBlacklist}#", yaml.dump(configMap));

                // adjust new lines to make the file human-readable according to
                // current system.
                output = output.replace("\n", System.getProperty("line.separator"));

                // create a file writer in "overwrite" mode
                // and use Yaml to dump the data into the file.
                writer = new FileWriter(dataFile, false);
                writer.write(output);
            }
            catch (Exception e) {
                log.warning("PointsOfInterest: Failed to rewrite config file - trace to follow.");
                e.printStackTrace();

                // this doesn't warrant a failure -- we read everything in fine, just weren't able
                // to tidy up the config file.
            }
            finally {
                // Make a best-effort attempt to clean up resources.
                try {
                    writer.close();
                } catch (Exception ignored) { }
            }
        }

        // record the data folder for future use.
        Config._dataFolder = dataFolder;

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

        // Version control.
        // Translate < v1.0.2 key for dynamic maximum POI permissions.
        if (map.containsKey("poi.action.add.max")) {
            map.put("crussell52.poi.max", map.get("poi.action.add.max"));
            map.remove("poi.action.max.add");
        }

        // see if lockDown is configured
        if (map.containsKey("lockDown")) {
            try {
                _instance._isLocked = (Boolean)map.get("lockDown");
            }
            catch (Exception ex) {
                log.warning("PointsOfInterest: Bad configuration for lockDown -- assuming true.");
            }
        }

        // see if config version is configured
        Integer configId = 0;
        if (map.containsKey("configId")) {
            try {
                configId = (Integer)map.get("configId");
            }
            catch (Exception ex) {
                log.severe("PointsOfInterest: Bad configuration for configId.");
                success = false;
            }
        }

        // if there is a version mismatch, then the version is not confirmed
        if (!configId.equals(Config.CURRENT_ID)) {
            // go into lockdown.
            _instance._isLocked = true;

            // log the reason for the lock down
            log.warning("PointsOfInterest: The plugin has been forced into \"lock down\" mode!");
            log.warning("PointsOfInterest: Either the configuration id is not recognized or a recent update to the");
            log.warning("PointsOfInterest: plugin has introduced a new configuration version which requires review.");
        }

        // see if a distance threshold is configured
        if (map.containsKey("distanceThreshold")) {
            try {
                _instance._distanceThreshold = (Integer)map.get("distanceThreshold");
            }
            catch (Exception ex) {
                log.severe("PointsOfInterest: Bad configuration for distanceThreshold.");
                success = false;
            }
        }

        // see if a minimum distance between POIs is configured
        if (map.containsKey("minPoiGap")) {
            try {
                _instance._minPoiGap = (Integer)map.get("minPoiGap");
            }
            catch (Exception ex) {
                log.severe("PointsOfInterest: Bad configuration for minPoiGap.");
                success = false;
            }
        }

        // see if a maximum number of search results
        if (map.containsKey("maxSearchResults")) {
            try {
                _instance._maxSearchResults = (Integer)map.get("maxSearchResults");
            }
            catch (Exception ex) {
                log.severe("PointsOfInterest: Bad configuration for maxSearchResults.");
                success = false;
            }
        }

        // see if per-world maximum number of POIs for each player is configured
        if (map.containsKey("maxPlayerPoiPerWorld")) {
            try {
                _instance._maxPlayerPoiPerWorld = (Integer)map.get("maxPlayerPoiPerWorld");
            }
            catch (Exception ex) {
                log.severe("PointsOfInterest: Bad configuration for maxPlayerPoiPerWorld.");
                success = false;
            }
        }

        if (map.containsKey("crussell52.poi.max")) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Integer> maxMap = (Map<String, Integer>)map.get("crussell52.poi.max");

                // null is a reasonable value...
                if (maxMap != null) {
                    _instance._maxPoiMap = maxMap;

                    // loop to make sure everything is what we expect it to be.
                    for (Map.Entry<String, Integer> entry : maxMap.entrySet()) {
                        if (!(entry.getKey() instanceof String) || !(entry.getValue() instanceof Integer)) {
                          log.severe("PointsOfInterest: Bad configuration for crussell52.poi.max");
                          success = false;
                          break;
                        }
                    }
                }
            }
            catch (Exception ex) {
                log.severe("PointsOfInterest: Bad configuration for crussell52.poi.max");
                success = false;
            }
        }

        try {
            _instance._worldBlackList = _processStringList(map, "worldBlacklist");
        } catch (Exception e) {
            _instance._worldBlackList = new ArrayList<String>();
            success = false;
        }

        try {
            _instance._mapMarkerWorlds = _processStringList(map, "mapMarkerWorlds");
        } catch (Exception e) {
            _instance._mapMarkerWorlds = new ArrayList<String>();
            success = false;
        }

        try {
            _instance._mapMarkerWhitelist = _processStringList(map, "mapMarkerWhitelist");
        } catch (Exception e) {
            _instance._mapMarkerWhitelist = new ArrayList<String>();
            success = false;
        }

        try {
            _instance._mapMarkerBlacklist = _processStringList(map, "mapMarkerBlacklist");
        } catch (Exception e) {
            _instance._mapMarkerBlacklist = new ArrayList<String>();
            success = false;
        }

        // return success indicator
        return success;
    }

    private static List<String> _processStringList(Map<String, Object> map, String configKey) throws Exception {
        List<String> configValue = new ArrayList<String>();
        if (map.containsKey(configKey)) {
            try {
                @SuppressWarnings("unchecked")
                ArrayList<String> tmp = (ArrayList<String>)map.get(configKey);

                // it's reasonable for this key to have a null value
                if (tmp != null) {
                    // convert each blacklisted world to lower-case before recording it
                    for (String world : tmp) {
                        configValue.add(world.toLowerCase());
                    }
                }
            }
            catch (Exception ex) {
                _log.severe("Bad configuration for " + configKey + ".");
                throw new Exception();
            }
        }

        return configValue;
    }
}
