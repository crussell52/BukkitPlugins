package crussell52.poi;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.util.Vector;

import crussell52.poi.api.IPoi;

/**
 * Represents a single Point of Interest
 */
public class Poi implements IPoi {

	/**
	 * X Coordinate of the POI
	 */
	private int _x;

	/**
	 * Y Coordinate of the POI
	 */
	private int _y;

	/**
	 * Z Coordinate of the POI
	 */
	private int _z;

	/**
	 * Player-defined name for the POI
	 */
	private String _name;

	/**
	 * System-generated, unique identifier for the POI
	 */
	private int _id;

	/**
	 * Name of the player who owns the POI.
	 */
	private String _owner;

	/**
	 * Name of the world the POI exists in.
	 */
	private String _world;

	/**
	 * Name of the world the POI exists in.
	 */
	public void setWorld(String world)
	{
		_world = world;
	}

	/**
	 * Name of the world the POI exists in.
	 */
	public String getWorld()
	{
		return _world;
	}

	/**
	 * X Coordinate of the POI
	 */
	public void setX(int x)
	{
		_x = x;
	}

	/**
	 * X Coordinate of the POI
	 */
	public int getX()
	{
		return _x;
	}

	/**
	 * Y Coordinate of the POI
	 */
	public void setY(int y)
	{
		_y = y;
	}

	/**
	 * Y Coordinate of the POI
	 */
	public int getY()
	{
		return _y;
	}

	/**
	 * Z Coordinate of the POI
	 */
	public void setZ(int z)
	{
		_z = z;
	}

	/**
	 * Z Coordinate of the POI
	 */
	public int getZ()
	{
		return _z;
	}

	/**
	 * System-generated, unique identifier for the POI
	 */
	public int getId()
	{
		return _id;
	}

	/**
	 * System-generated, unique identifier for the POI
	 */
	public void setId(int id)
	{
		_id = id;
	}

	/**
	 * Player-defined name for the POI.
	 */
	public String getName()
	{
		return _name;
	}

	/**
	 * Player-defined name for the POI.
	 */
	public void setName(String name)
	{
		_name = name;
	}

	/**
	 * Name of the player who owns the POI.
	 */
	public String getOwner()
	{
		return _owner;
	}

	/**
	 * Name of the player who owns the POI.
	 */
	public void setOwner(String owner)
	{
		_owner = owner;
	}

	/**
	 * Get the location of the POI as a vector
	 */
	public Vector getVector()
	{
		return new Vector(_x, _y, _z);
	}

	/**
	 * Get a one-line summary of the POI including name, owner, and id.
	 *
	 * @param colorCode The <code>ChatColor</code> to use for coloring the summary.
	 */
	public String getShortSummary(ChatColor colorCode)
	{
		return (colorCode + this.getName() + " (Owner: " + this.getOwner() + ", ID: " + this.getId() + ")");
	}

	/**
	 * Get a standard summary of the POI including name, owner, id, distance
	 * and directions (if within distance threshold).
	 *
	 * @param location Location which the distance is calculated against
	 * @param distanceThreshold Maximum distance at which directions are available; pass a value of -1 for "no threshold".
	 * @param colorCode The <code>ChatColor</code> to apply to each of the summary lines.
	 */
	public ArrayList<String> getSummary(Location location, int distanceThreshold, ChatColor colorCode)
	{
		ArrayList<String> summary = new ArrayList<String>();
		summary.add(this.getShortSummary(colorCode));
		summary.add(PointsOfInterest.getDirections(location.toVector(), this.getVector(), distanceThreshold, colorCode));
		return summary;
	}

    public Location toLocation(Server server)
    {
        World world = server.getWorld(this.getWorld());
        if (world == null) {
            return new Location(world, this.getX(), this.getY(), this.getZ());
        }

        return null;
    }

	/**
	 * {@inheritDoc}
	 */
    @Override
	public String toString() {
		return getVector() + "|" + _name + "|" + _owner + "|" + _id;
	}
}
