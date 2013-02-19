package crussell52.bukkit.escape;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.util.Vector;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: crussell
 * Date: 2/16/13
 * Time: 11:37 AM
 * To change this template use File | Settings | File Templates.
 */

@SerializableAs("escape")
public class EscapeLocation extends Vector implements ConfigurationSerializable
{

    private String _worldName = null;
    private Long _escapedFromAt = null;

    public String getWorldName()
    {
        return _worldName;
    }

    public Long getEscapedFromAt()
    {
        return _escapedFromAt;
    }

    public EscapeLocation(String worldName, double x, double y, double z, Long escapedFromAt)
    {
        super(x, y, z);
        _worldName = worldName;
        _escapedFromAt = escapedFromAt;
    }

    public EscapeLocation(String worldName, double x, double y, double z)
    {
        super(x, y, z);
        _worldName = worldName;
    }

    public EscapeLocation(Location location, Long escapedFromAt)
    {
        super(location.getX(), location.getY(), location.getZ());
        _worldName = location.getWorld().getName();
        _escapedFromAt = escapedFromAt;
    }

    public Location toLocation(Server server) throws EscapeException {
        World targetWorld = server.getWorld(getWorldName());
        if (targetWorld == null)
        {
            throw new EscapeException("Could not find world '" + getWorldName() + "' on server.");
        }

        return new Location(targetWorld, getX(), getY(), getZ());
    }


    @Override
    public Map<String, Object> serialize()
    {
        Map<String, Object> result = new LinkedHashMap<String, Object>();

        result.put("x", getX());
        result.put("y", getY());
        result.put("z", getZ());
        result.put("worldName", _worldName);

        if (_escapedFromAt != null)
        {
            result.put("escapedFromAt", _escapedFromAt);
        }

        return result;
    }

    public static EscapeLocation deserialize(Map<String, Object> args)
    {
        if (args.containsKey("x") && args.containsKey("y") &&
            args.containsKey("z") && args.containsKey("worldName"))
        {
            {
                String worldName = (String) args.get("worldName");
                double x = (Double) args.get("x");
                double y = (Double) args.get("y");
                double z = (Double) args.get("z");
                if (args.containsKey("escapedFromAt"))
                {
                    return new EscapeLocation(worldName, x, y, z, (Long) args.get("escapedFromAt"));
                }
                return new EscapeLocation(worldName, x, y, z);
            }
        }

        return null;
    }
}
