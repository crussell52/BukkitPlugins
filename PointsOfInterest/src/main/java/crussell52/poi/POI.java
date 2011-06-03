package crussell52.PointsOfInterest;

import org.bukkit.util.Vector;

public class POI {
	
	private int _x;
	private int _y;
	private int _z;
	private String _name;
	private int _id;
	private String _owner;
	private String _world;
	
	public void setWorld(String world)
	{
		_world = world;
	}
	
	public String getWorld()
	{
		return _world;
	}

	public void setX(int x)
	{
		_x = x;
	}
	
	public int getX()
	{
		return _x;
	}
	
	public void setY(int y)
	{
		_y = y;
	}
	
	public int getY()
	{
		return _y;
	}
	
	public void setZ(int z)
	{
		_z = z;
	}
	
	public int getZ()
	{
		return _z;
	}
	
	public int getId()
	{
		return _id;
	}
	
	public void setId(int id)
	{
		_id = id;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public void setName(String name)
	{
		_name = name;
	}
	
	public String getOwner()
	{
		return _owner;
	}
	
	public void setOwner(String owner)
	{
		_owner = owner;
	}
	
	public Vector getVector()
	{
		return new Vector(_x, _y, _z);
	}
	
	public String toString() {
		return getVector() + "|" + _name + "|" + _owner + "|" + _id;
	}
}
