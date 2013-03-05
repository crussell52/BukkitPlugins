package crussell52.bukkit.escape;

import org.bukkit.Location;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.Map;


/**
 * Represents information about an available escape location or a location from which a player
 * escaped.
 */
@SerializableAs("escapeDetails")
public class EscapeDetails extends EscapeLocation
{
    /**
     * The time the user escaped from this location.
     */
    private Long _escapedFromAt = null;

    /**
     * The time the user escaped from this location.
     */
    public Long getEscapedFromAt()
    {
        return _escapedFromAt;
    }

    /**
     * Construct a new instance by providing all of the information as discreet parameters.
     *
     * @param worldName The world which was escaped.
     * @param x The x coordinate which was escaped.
     * @param y The y coordinate which was escaped.
     * @param z The z coordinate which was escaped.
     * @param escapedFromAt The time the player escaped.
     */
    public EscapeDetails(String worldName, double x, double y, double z, Long escapedFromAt)
    {
        super(worldName, x, y, z);
        _escapedFromAt = escapedFromAt;
    }

    /**
     * Construct a new instance based on a bukkit Location.
     *
     * @param location A Location instance representing the place from which the player escaped.
     * @param escapedFromAt The time the player escaped.
     */
    public EscapeDetails(Location location, Long escapedFromAt)
    {
        super(location.getWorld().getName(), location.getX(), location.getY(), location.getZ());
        _escapedFromAt = escapedFromAt;
    }

    /**
     * Construct a new instance based on the values within a Map instance.
     *
     * This facilitates the deserialization requirement of the ConfigurationSerializable interface
     * and should not need to be called directly.
     *
     * @see #serialize()
     *
     * @param args A Map containing all the data necessary to construct an instance.
     * @throws Exception
     */
    @SuppressWarnings("UnusedDeclaration")
    public EscapeDetails(Map<String, Object> args) throws Exception
    {
        super(args);
        if (!args.containsKey("escapedFromAt"))
        {
            throw new Exception("Can not construct from Map - incomplete data.");
        }

        _escapedFromAt = (Long) args.get("escapedFromAt");
    }

    /**
     * Converts this instance into a Map of data.
     *
     * This facilitates the serialization requirement of the ConfigurationSerializable interface
     * and should not need to be called directly.
     *
     * @see #EscapeDetails(java.util.Map)
     *
     * @return A Map containing all the data necessary to construct an instance.
     */
    @Override
    public Map<String, Object> serialize()
    {
        Map<String, Object> result = super.serialize();
        result.put("escapedFromAt", _escapedFromAt);
        return result;
    }
}
