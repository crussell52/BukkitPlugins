package crussell52.bukkit.multiworldBeds;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.util.Vector;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents information about an available escape location.
 */
@SerializableAs("bedSpawn")
public class BedSpawn extends Vector implements ConfigurationSerializable
{
    /**
     * The name of the world from which the user escaped.
     */
    private String _worldName = null;

    /**
     * The name of the world associated with this location.
     */
    public String getWorldName()
    {
        return _worldName;
    }

    /**
     * Construct a new instance by passing all necessary data as discreet values.
     *
     * @param worldName The name of the world in which this location exists.
     * @param x The x coordinate of this location.
     * @param y The y coordinate of this location.
     * @param z The z coordinate of this location.
     */
    public BedSpawn(String worldName, double x, double y, double z)
    {
        super(x, y, z);
        _worldName = worldName;
    }

    /**
     * Construct a new instance by passing a Map containing all the data necessary.
     *
     * This fulfills the deseralization needs of the ConfigurationSerializable interface and should
     * not need to be called directly.
     *
     * @param args The Map containing all data necessary for construction.
     * @throws Exception
     */
    public BedSpawn(Map<String, Object> args) throws Exception
    {
        // Do a sanity check on the data.
        if (args.containsKey("x") && args.containsKey("y") &&
                args.containsKey("z") && args.containsKey("worldName"))
        {
            {
                _worldName = (String) args.get("worldName");
                x = (Double) args.get("x");
                y = (Double) args.get("y");
                z = (Double) args.get("z");
            }
        }
        else
        {
            // Some of the data is missing. Can not construct.
            throw new Exception("Can not construct from Map - incomplete data.");
        }
    }

    /**
     * Converts this instance to an org.bukkit.Location with the help of an org.bukkit.Server.
     *
     * @param server The server that the plugin is running on.
     *
     * @return An org.bukkit.Location with the same coordinates and world as this instance.
     *
     * @throws Exception
     */
    public Location toLocation(Server server) throws Exception
    {
        // Attempt to get the actual world instance from the server.
        World targetWorld = server.getWorld(getWorldName());
        if (targetWorld == null)
        {
            // Unable to get a world instance. Throw an EscapeException.
            throw new Exception("Could not find world '" + getWorldName() + "' on server.");
        }

        // Construct the Location and return it.
        return new Location(targetWorld, getX(), getY(), getZ());
    }


    /**
     * Converts this instance into a Map of data.
     *
     * This facilitates the serialization requirement of the ConfigurationSerializable interface
     * and should not need to be called directly.
     *
     * @return A Map containing all the data necessary to construct an instance.
     */
    @Override
    public Map<String, Object> serialize()
    {
        Map<String, Object> result = new LinkedHashMap<String, Object>();

        result.put("x", getX());
        result.put("y", getY());
        result.put("z", getZ());
        result.put("worldName", _worldName);

        return result;
    }
}
