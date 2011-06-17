package crussell52.poi.api;

import org.bukkit.util.Vector;

/**
 * Represents a single Point of Interest
 */
public interface IPoi {
			
	/**
	 * Name of the world the POI exists in.
	 */
	public String getWorld();
			
	/**
	 * X Coordinate of the POI
	 */
	public int getX();
			
	/**
	 * Y Coordinate of the POI
	 */
	public int getY();
			
	/**
	 * Z Coordinate of the POI
	 */
	public int getZ();
	
	/**
	 * System-generated, unique identifier for the POI
	 */
	public int getId();
			
	/**
	 * Name (label) of the POI.
	 */
	public String getName();

	
	/**
	 * Name of the player who owns the POI.
	 */
	public String getOwner();
			
	/**
	 * Get the location of the POI as a vector
	 */
	public Vector getVector();
		
}
