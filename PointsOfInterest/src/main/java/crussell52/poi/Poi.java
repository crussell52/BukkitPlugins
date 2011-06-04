package crussell52.poi;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public class Poi {
	
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
	
	public String getShortSummary(String colorCode)
	{
		return (colorCode + this.getName() + " (Owner: " + this.getOwner() + ", ID: " + this.getId() + ")");
	}
	
	
	public ArrayList<String> getSummary(Location location, int distanceThreshold, String colorCode)
	{
		ArrayList<String> summary = new ArrayList<String>();
		summary.add(this.getShortSummary(colorCode));
		
		int distance = (int)location.toVector().distance(this.getVector());
		String directions = colorCode + "    " + distance + " meters ";
		if (distanceThreshold < 0 || distance <= distanceThreshold) {
			int deltaX = (int)location.getX() - this.getX();
			int deltaY = (int)location.getY() - this.getY();
			int deltaZ = (int)location.getZ() - this.getZ();

			directions += (deltaX > 0 ? "North:" : "South:") + (int)Math.abs(deltaX) + ", ";
			directions += (deltaZ > 0 ? "East:" : "West:") + (int)Math.abs(deltaZ) + ", ";
			directions += (deltaY > 0 ? "Down:" : "UP:") + (int)Math.abs(deltaY) + ")";

		}
		else {
			directions += "(-- Out of Range --)";
		}
		
		summary.add(directions);
		
		return summary;
	}
	
	public String toString() {
		return getVector() + "|" + _name + "|" + _owner + "|" + _id;
	}
}
