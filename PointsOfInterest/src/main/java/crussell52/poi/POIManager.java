package crussell52.poi;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.sqlite.Function;

public class POIManager {
	
	private Logger _log;
	
	private final Map<Player, Map<String, PagedPoiList>> _recentResults = new HashMap<Player, Map<String, PagedPoiList>>();
	
	private Map<Player, POI> _selectedPOIs = new HashMap<Player, POI>(); 
	
	public static final int MAX_NAME_LENGTH = 24;
	
	private static final String SELECT_BASE = "SELECT id, name, world, owner, x, y, z ";
	
	public boolean initialize()
	{
		_log = Logger.getLogger("Minecraft");
		Connection conn = null;
		Boolean success = false;
		try {
			conn = _getDBConn();
			_setupDB(conn);
			success = true;
		}
		catch (Exception ex) {
			System.out.println(ex);
		}
		finally {
			_closeConn(conn);
		}
		
		return success;
	}
	
	
	private Connection _getDBConn(){
		try {
			String file = "./plugins/PointsOfInterest/POI.db";
			Class.forName("org.sqlite.JDBC");
			Connection conn = DriverManager.getConnection("jdbc:sqlite:" + file);
			return conn;
		}
		catch (Exception ex) {
			System.out.println(ex);
		}
		
		return null;
    }
	
	public PagedPoiList getRecentResults(Player player)
	{
		try {
			return this._recentResults.get(player).get(player.getWorld().getName());
		}
		catch (Exception ex) {
			return null;
		}
	}
	
	public void setRecentResults(Player player, PagedPoiList results)
	{
		if (!this._recentResults.containsKey(player)) {
			this._recentResults.put(player, new HashMap<String, PagedPoiList>());
		}
		
		this._recentResults.get(player).put(player.getWorld().getName(), results);
	}
	
		
	public POI getSelectedPOI(Player player)
	{
		POI poi = this._selectedPOIs.get(player);
		if (poi != null && poi.getWorld().equalsIgnoreCase(player.getWorld().getName())) {
			return poi;
		}
		
		return null;
	}
	
	public void selectPOI(int id, Player player) throws POIException
	{
		Connection conn = _getDBConn();
		
		try {
			PreparedStatement sql = conn.prepareStatement(
				SELECT_BASE + 
				"FROM poi " +
				"WHERE id = ?;");
			
			sql.setInt(1, id);
			
			ArrayList<POI> list = _getPOIs(sql, conn);
			if (list.size() == 0) {
				throw new POIException(POIException.NO_POI_AT_ID, "No POI with specified id.");
			}
			
			// id selection always returns exactly one POI.
			POI poi = list.get(0);

			// make sure the POI is in the Player's current world
			if (!player.getWorld().getName().equalsIgnoreCase(poi.getWorld())) {
				// poi isn't in the same world as the player.
				throw new POIException(POIException.POI_OUT_OF_WORLD, "POI belongs to a different world.");
			}
			
			// if we made it this far, everything went okay, select the poi
			this._selectedPOIs.put(player, poi);
		}
		catch (POIException ex) {
			throw ex;
		}
		catch (Exception ex) {
			throw new POIException(POIException.SYSTEM_ERROR, ex);
		}
		finally {
			_closeConn(conn);
		}
	}
	
	public void addPOI(String name, Player player, int distanceThreshold) throws POIException
	{
		Connection conn = _getDBConn();
		ResultSet rs = null;
		Location location = player.getLocation();

		try {
			ArrayList<POI> list = new ArrayList<POI>();
			list = getNearby(location, distanceThreshold, 1);
			if (list.size() > 0) {
				throw new POIException(POIException.TOO_CLOSE_TO_ANOTHER_POI, "Player is too close to an existing POI threshold: " + distanceThreshold);
			}
			
			PreparedStatement sql = conn.prepareStatement("insert into poi (x, y, z, name, owner, world) values (?, ?, ?, ?, ?, ?);");
			sql.setInt(1, (int)location.getX());
			sql.setInt(2, (int)location.getY());
			sql.setInt(3, (int)location.getZ());
			sql.setString(4, name);
			sql.setString(5, player.getName());
			sql.setString(6, location.getWorld().getName());
			sql.executeUpdate();
		}
		catch (SQLException sqlEx) {
			throw new POIException(POIException.SYSTEM_ERROR, sqlEx);
		}
		finally {
			_closeConn(conn);
			_closeResultSet(rs);
		}
	}
	
	private void _createDistanceFunc(Connection conn) throws SQLException
	{
		Function.create(conn, "distance", new Function() {
            protected void xFunc() throws SQLException {
            	try {
            		int x1 = value_int(0);
            		int y1 = value_int(1);
            		int z1 = value_int(2);
            		int x2 = value_int(3);
            		int y2 = value_int(4);
            		int z2 = value_int(5);

            		result(Math.abs(Math.sqrt(Math.pow((x2 - x1), 2) + Math.pow((y2 - y1), 2) + Math.pow((z2 - z1), 2))));
            	}
	            catch (Exception e) {
	            	throw new SQLException("Unable to calculate distance - invalid parameters", e);
	            }
            }
        });
	}
	
	private ArrayList<POI> _getPOIs(PreparedStatement sql, Connection conn) throws SQLException
	{
		ResultSet rs = null;
		POI poi = null;
		ArrayList<POI> list = new ArrayList<POI>();
		
		try {  	
			rs = sql.executeQuery();
			while (rs.next()) {
				poi = new POI();
				poi.setX(rs.getInt("x"));
				poi.setY(rs.getInt("y"));
				poi.setZ(rs.getInt("z"));
				poi.setId(rs.getInt("id"));
				poi.setName(rs.getString("name"));
				poi.setOwner(rs.getString("owner"));
				poi.setWorld(rs.getString("world"));
				list.add(poi);
			}
			
			return list;		
		}
		finally {
			_closeResultSet(rs);
		}
	}
	
	public ArrayList<POI> getNearby(Location playerLoc, int maxDistance, int limit) throws POIException
    {
    	Connection conn = _getDBConn();
    	
		try {
			_createDistanceFunc(conn);
			PreparedStatement sql = conn.prepareStatement(
				SELECT_BASE + ", distance(?, ?, ?, poi.x, poi.y, poi.z) AS distance " + 
				"FROM poi " +
				"WHERE distance <= ? " +
				"AND world = ? " +
				"ORDER BY distance ASC " +
				"LIMIT ?;");

			sql.setInt(1, (int)playerLoc.getX());
			sql.setInt(2, (int)playerLoc.getY());
			sql.setInt(3, (int)playerLoc.getZ());
			sql.setInt(4, maxDistance);
			sql.setString(5, playerLoc.getWorld().getName());
			sql.setInt(6, limit);
			
			return _getPOIs(sql, conn);
		}
		catch (SQLException sqlEx) {
			throw new POIException(POIException.SYSTEM_ERROR, sqlEx);
		}
		finally {
			_closeConn(conn);	
		}
    }
    
    private void _closeConn(Connection conn)
    {
    	try {
			if (conn != null) {
				conn.close();
			}
		}
		catch(Exception ex) {
			_log.info("Failed to close Connection: " + ex);
		}
    }
    
    private void _closeResultSet(ResultSet rs)
    {
    	try {
			if (rs != null) {
				rs.close();
			}
		}
		catch(Exception ex) {
			_log.info("Failed to close ResultSet: " + ex);
		}
    }
    
    private void _setupDB(Connection conn) {
		try {  	
	    	Statement sql = conn.createStatement();
	        sql.executeUpdate("CREATE TABLE IF NOT EXISTS `poi` " +
	        		"(`id` INTEGER PRIMARY KEY , " +
	        		"`x` INTEGER NOT NULL ," +
	        		"`y` INTEGER NOT NULL ," +
	        		"`z` INTEGER NOT NULL ," + 
	        		"`owner` STRING(16) NOT NULL, " +
	        		"`world` STRING NOT NULL, " + 
	        		"`name` STRING(24) NOT NULL);"
	        		);
		}
		catch (SQLException sqlEx) {
			_log.severe("Failed to setup POI database");
			_log.severe(sqlEx.toString());
		}
		finally {
			_closeConn(conn);
		}
    }
    
    
	
}
