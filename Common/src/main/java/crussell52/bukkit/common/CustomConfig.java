package crussell52.bukkit.common;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

/**
 * Created with IntelliJ IDEA.
 * User: crussell
 * Date: 2/15/13
 * Time: 7:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class CustomConfig
{

    private FileConfiguration _customConfig = null;
    private File _customConfigFile = null;
    private String _fileName = null;
    private Plugin _plugin = null;

    public CustomConfig(Plugin plugin, String fileName)
    {
        _plugin = plugin;
        _fileName = fileName;
    }

    public void reloadConfig()
    {
        if (_customConfigFile == null)
        {
            _customConfigFile = new File(_plugin.getDataFolder(), _fileName);
        }

        _customConfig = YamlConfiguration.loadConfiguration(_customConfigFile);

        // Look for defaults in the jar
        InputStream defConfigStream = _plugin.getResource(_fileName);
        if (defConfigStream != null)
        {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            _customConfig.setDefaults(defConfig);
        }
    }

    public FileConfiguration getConfig()
    {
        if (_customConfig == null)
        {
            reloadConfig();
        }

        return _customConfig;
    }

    public void saveConfig()
    {
        if (_customConfig == null || _customConfigFile == null)
        {
            return;
        }
        try
        {
            getConfig().save(_customConfigFile);
        }
        catch (IOException ex)
        {
            _plugin.getLogger().log(Level.SEVERE, "Could not save config to " + _customConfigFile, ex);
        }
    }

    public void saveDefaultConfig()
    {
        if (_customConfigFile == null)
        {
            _customConfigFile = new File(_plugin.getDataFolder(), _fileName);
        }
        if (!_customConfigFile.exists())
        {
            _plugin.saveResource(_fileName, false);
        }
    }
}
