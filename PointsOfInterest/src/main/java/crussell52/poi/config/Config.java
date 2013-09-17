package crussell52.poi.config;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import crussell52.poi.PoiException;
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
    private boolean _isLocked;

    /**
     * Dictates how far to search and maximum distance
     * a player can be from a POI and still get directions
     */
    private int _distanceThreshold;

    /**
     * Dictates minimum distance between POIs.
     */
    private int _minPoiGap = 50;

    /**
     * Maximum number of search results when a player does
     * an area search.
     */
    private int _maxSearchResults;

    /**
     * Map of permission keys and the maximum POIs associated
     * with each.
     */
    private Map<String, Integer> _maxPoiMap;

    /**
     * Maximum number of POIs a player can create in each world
     * if not specified by _maxPoiMap.
     */
    private Integer _maxPlayerPoiPerWorld;

    /**
     * List of worlds in which POIs are not supported.
     */
    private List<String> _worldBlackList;

    /**
     * List of worlds in which POIs are not supported.
     */
    private List<String> _mapMarkerWorlds;

    /**
     * List of worlds in which POIs are not supported.
     */
    private List<String> _mapMarkerWhitelist;

    /**
     * List of worlds in which POIs are not supported.
     */
    private List<String> _mapMarkerBlacklist;

    /**
     * keep a handle to the last data folder used for loading.
     */
    private File _dataFolder;

    /**
     * Keep a handle to the last log file used.
     */
    private Logger _log;
    private HashMap<String, PoiType> _poiTypes;
    private String _defaultMapMarkerIcon;

    // hide default constructor -- everything should be accessed statically
    private Config()
    {
        // Hidden constructor.
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
            _instance._log.info("Looking for: " + entry.getKey());
            if (player.hasPermission("crussell52.poi.max." + entry.getKey())) {
                _instance._log.info("Permission found. Value: " + entry.getValue());
                // player has the related permission, but we want the most restrictive
                // value... see if it is the lowest so far.  Note, -1 is a special case because
                // it is LEAST restrictive.
                if (lowestMax == null || (entry.getValue() < lowestMax && entry.getValue() != -1)) {
                    // lowest maximum so far.
                    _instance._log.info("new lowest.");
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

    public static String getDefaultMapMarkerIcon() {
        return _instance._defaultMapMarkerIcon;
    }

    public static boolean isPoiType(String id)
    {
        return id != null && _instance._poiTypes.containsKey(id.toLowerCase());
    }

    public static PoiType getPoiType(String id)
    {
        if (_instance._poiTypes.containsKey(id)) {
            return _instance._poiTypes.get(id);
        }
        else {
            return _instance._poiTypes.get("default");
        }
    }

    /**
     * Reloads the config from the file.
     *
     * @return
     */
    public static void reload() throws PoiException {

        if (_instance == null) {
            throw new PoiException(PoiException.SYSTEM_ERROR, "Tried to reload config before it had been loaded.");
        }

        Config previous = _instance;
        try {
            load(_instance._dataFolder, _instance._log);
        } catch (PoiException e) {
            _instance = previous;
            previous._log.severe(e.getMessage());
            throw new PoiException(PoiException.SYSTEM_ERROR, "Failed to reload config. Reverted to last known configuration.");
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
    public static void load(File dataFolder, Logger log) throws PoiException {

        // always start with a new instance.
        _instance = new Config();

        // Record the log and data folder for general use.
        _instance._log = log;
        _instance._dataFolder = dataFolder;

        // setup a YAML instance for read of config file
        Yaml yaml = new Yaml();

        // get a handle to the config file and create var to contain config map.
        File dataFile = new File(dataFolder, "config.yml");
        Map<String, Object> configMap;

        if (!dataFile.exists()) {
            // log a warning about the lack of config file.
            log.warning("No configuration file found -- assuming initial plugin install.");
            log.warning("The plugin will be forced into lock-down mode to give you a chance to adjust " +
                    "your initial configuration settings before player start adding POIs.");

            // try to create the file.
            try {
                if (!dataFile.createNewFile()) {
                    throw new PoiException(PoiException.SYSTEM_ERROR, "File.createNewFile() returned false.");
                }
            }
            catch (Exception e) {
                throw new PoiException(PoiException.SYSTEM_ERROR, "Failed to create config file.");
            }

            // Force the config map to an empty map so we can skip the process of loading the file
            // we just created.
            configMap = new HashMap<String, Object>();
        }
        else {
            // we have a config file, try to read it.
            FileInputStream input = null;
            try {
                // parse the file as a map.
                input = new FileInputStream(dataFile);
                @SuppressWarnings({"unchecked", "UnnecessaryLocalVariable"})
                Map<String, Object> yamlData = (Map<String, Object>)yaml.load(input);
                configMap = yamlData;
            }
            catch (Exception e) {
                e.printStackTrace();
                throw new PoiException(PoiException.SYSTEM_ERROR, "Config file exists, but failed to read it.");
            }
            finally {
                // make sure we close the streams.
                // handle failures quietly.
                try {
                    if (input != null) {
                        input.close();
                    }
                }
                catch (Exception ignored) {}
            }
        }

        try {
            // Parse the config map.
            _processConfigMap(configMap, log);
        }
        catch (PoiException poiEx) {
            log.severe(poiEx.getMessage());
            throw new PoiException(PoiException.SYSTEM_ERROR, "Failed to parse config file.");
        }

        // see if we are in lock-down (either forced or manual)
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
        _instance._writeConfig();
    }

    private String _getConfigTemplate() throws PoiException {
        if (_configTemplate != null) {
            return _configTemplate;
        }

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

            return _configTemplate = stringBuilder.toString();
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new PoiException(PoiException.SYSTEM_ERROR, "Failed to read in config template.");
        }
        finally {
            // Close off resources. No recourse in the case of failures, so just ignore
            // exceptions.
            try {
                if (reader != null) {
                    reader.close();
                }
            }
            catch (Exception ignored) {}

            try {
                if (templateInput != null) {
                    templateInput.close();
                }
            }
            catch (Exception ignored) {}
        }
    }

    private void _writeConfig() throws PoiException {
        String configTemplate = _getConfigTemplate();

        // Create a var to hold our file writer.
        FileWriter writer = null;
        File dataFile = new File(_dataFolder, "config.yml");

        // setup a YAML instance for write of config file
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(FlowStyle.BLOCK);
        options.setDefaultScalarStyle(ScalarStyle.PLAIN);
        Yaml yaml = new Yaml(options);

        try {
            // create a hash map...
            // we will feed the config keys into it (one at a time) and use
            // Yaml to get the YAML representation for output.
            Map<String, Object> configMap = new HashMap<String, Object>();

            configMap.put("distanceThreshold", _instance._distanceThreshold);
            String output = configTemplate.replace("#{distanceThreshold}#", yaml.dump(configMap));

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
            configMap.put("defaultMapMarkerIcon", _instance._defaultMapMarkerIcon);
            output = output.replace("#{defaultMapMarkerIcon}#", yaml.dump(configMap));

            configMap.clear();
            ArrayList<Map<String, Object>> poiTypes = new ArrayList<Map<String, Object>>();
            for (PoiType poiType : _instance._poiTypes.values()) {
                poiTypes.add(poiType.toMap());
            }
            configMap.put("poiTypes", poiTypes);
            output = output.replace("#{poiTypes}#", yaml.dump(configMap));

            // adjust new lines to make the file human-readable according to
            // current system.
            output = output.replace("\n", System.getProperty("line.separator"));

            // create a file writer in "overwrite" mode
            // and use Yaml to dump the data into the file.
            writer = new FileWriter(dataFile, false);
            writer.write(output);
        }
        catch (Exception e) {
            _log.warning("PointsOfInterest: Failed to rewrite config file - trace to follow.");
            e.printStackTrace();

            // this doesn't warrant a failure -- we read everything in fine, just weren't able
            // to tidy up the config file.
        }
        finally {
            // Make a best-effort attempt to clean up resources.
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (Exception ignored) { }
        }
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
    private static void _processConfigMap(Map<String, Object> map, Logger log) throws PoiException {
        // see if we have any data to process
        if (map == null) {
            // no data, easy success
            return;
        }

        // Version control.
        // Translate < v1.0.2 key for dynamic maximum POI permissions.
        if (map.containsKey("poi.action.add.max")) {
            map.put("crussell52.poi.max", map.get("poi.action.add.max"));
            map.remove("poi.action.max.add");
        }

        _instance._isLocked              = _getBoolean(map, "lockDown", true);
        _instance._distanceThreshold     = _getInteger(map, "distanceThreshold", 2000);
        _instance._minPoiGap             = _getInteger(map, "minPoiGap", 50);
        _instance._maxSearchResults      = _getInteger(map, "maxSearchResults", 10);
        _instance._maxPlayerPoiPerWorld  = _getInteger(map, "maxPlayerPoiPerWorld", 10);
        _instance._worldBlackList        = _getStringList(map, "worldBlacklist");
        _instance._mapMarkerWorlds       = _getStringList(map, "mapMarkerWorlds");
        _instance._maxPoiMap             = _getIntegerMap(map, "crussell52.poi.max", false);
        _instance._defaultMapMarkerIcon  = _getString(map, "defaultMapMarkerIcon", "sign");

        // Read in the poi types.
        _instance._poiTypes = new HashMap<String, PoiType>();
        if (map.containsKey("poiTypes")) {
            try {
                @SuppressWarnings("unchecked")
                ArrayList<Map<String, Object>> poiTypeList =  (ArrayList<Map<String, Object>>)map.get("poiTypes");

                for (Map<String, Object> poiTypeConfig : poiTypeList) {
                    PoiType poiType = new PoiType(poiTypeConfig);
                    _instance._poiTypes.put(poiType.getID().toLowerCase(), poiType);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                throw new PoiException(PoiException.SYSTEM_ERROR, "Invalid configuration for poiTypes.");
            }
        }

        if (!_instance._poiTypes.containsKey("default")) {
            _instance._poiTypes.put("default", new PoiType("default", "POI"));
        }

        // if there is a version mismatch, then the version is not confirmed
        Integer configId = _getInteger(map, "configId", 0);
        if (!configId.equals(Config.CURRENT_ID)) {
            // go into lock-down.
            _instance._isLocked = true;

            // log the reason for the lock down
            log.warning("The plugin has been forced into \"lock down\" mode!");
            log.warning("Either the configuration id is not recognized or a recent update to the plugin has " +
                    "introduced a new configuration version which requires review.");
        }

    }

    private static Boolean _getBoolean(Map<String, Object> map, String configKey, Boolean defaultVal) throws PoiException {
        if (map.containsKey(configKey) && map.get(configKey) != null) {
            try {
                return (Boolean)map.get(configKey);
            }
            catch (Exception ex) {
                ex.printStackTrace();
                throw new PoiException(PoiException.SYSTEM_ERROR, "Bad configuration for " + configKey + ".");
            }
        }

        return defaultVal;
    }

    private static Integer _getInteger(Map<String, Object> map, String configKey, Integer defaultVal) throws PoiException {
        if (map.containsKey(configKey) && map.get(configKey) != null) {
            try {
                return (Integer)map.get(configKey);
            }
            catch (Exception ex) {
                ex.printStackTrace();
                throw new PoiException(PoiException.SYSTEM_ERROR, "Bad configuration for " + configKey + ".");
            }
        }

        return defaultVal;
    }

    private static String _getString(Map<String, Object> map, String configKey, String defaultVal) throws PoiException {
        if (map.containsKey(configKey) && map.get(configKey) != null) {
            try {
                return (String)map.get(configKey);
            }
            catch (Exception ex) {
                ex.printStackTrace();
                throw new PoiException(PoiException.SYSTEM_ERROR, "Bad configuration for " + configKey + ".");
            }
        }

        return defaultVal;
    }

    private static Map<String, Integer> _getIntegerMap(Map<String, Object> map, String configKey, boolean allowNullValues) throws PoiException {
        if (map.containsKey(configKey)) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Integer> returnVal = (Map<String, Integer>)map.get(configKey);

                // Null is a reasonable value, but we are going to force it to a
                // Map of appropriate type.
                if (returnVal == null) {
                    return new HashMap<String, Integer>();
                }

                // loop to make sure everything is what we expect it to be.
                for (Map.Entry<String, Integer> entry : returnVal.entrySet()) {
                    if (entry.getKey() == null || (!allowNullValues && entry.getValue() == null)) {
                        throw new Exception("Map values for " + configKey + " are not allowed to be null.");
                    }
                }

                return returnVal;
            }
            catch (Exception ex) {
                ex.printStackTrace();
                throw new PoiException(PoiException.SYSTEM_ERROR, "Bad configuration for " + configKey);
            }
        }

        return new HashMap<String, Integer>();
    }

    private static Map<String, String> _getStringMap_getStringMap(Map<String, Object> map, String configKey, boolean allowNullValues) throws PoiException {
        if (map.containsKey(configKey)) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, String> returnVal = (Map<String, String>)map.get(configKey);

                // Null is a reasonable value, but we are going to force it to a
                // Map of appropriate type.
                if (returnVal == null) {
                    return new HashMap<String, String>();
                }

                // loop to make sure everything is what we expect it to be.
                for (Map.Entry<String, String> entry : returnVal.entrySet()) {
                    if (entry.getKey() == null || (!allowNullValues && entry.getValue() == null)) {
                        throw new Exception("Map values for " + configKey + " are not allowed to be null.");
                    }
                }

                return returnVal;
            }
            catch (Exception ex) {
                ex.printStackTrace();
                throw new PoiException(PoiException.SYSTEM_ERROR, "Bad configuration for " + configKey);
            }
        }

        return new HashMap<String, String>();
    }

    private static List<String> _getStringList(Map<String, Object> map, String configKey) throws PoiException {
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
                ex.printStackTrace();
                throw new PoiException(PoiException.SYSTEM_ERROR, "Bad configuration for " + configKey + ".");
            }
        }

        return configValue;
    }
}
