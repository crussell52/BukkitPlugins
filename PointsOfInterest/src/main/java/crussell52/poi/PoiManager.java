package crussell52.poi;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.sqlite.Function;

public class PoiManager {
	
	private Logger _log;
	
	private final Map<Player, Map<String, PagedPoiList>> _recentResults = new HashMap<Player, Map<String, PagedPoiList>>();
	
	private Map<Player, Poi> _selectedPOIs = new HashMap<Player, Poi>(); 
	
	public static final int MAX_NAME_LENGTH = 24;
	
	private static final String SELECT_BASE = "SELECT id, name, world, owner, x, y, z ";
	
	private String _dbPath;
	
	public boolean initialize(File pluginDataFolder)
	{
		_log = Logger.getLogger("Minecraft");
		Connection conn = null;
		Boolean success = false;
		try {
			File dbFolder = new File(pluginDataFolder, "db");
			dbFolder.mkdir();
			_dbPath = new File(dbFolder, "POI.db").getCanonicalPath();	
			
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
			Class.forName("org.sqlite.JDBC");
			Connection conn = DriverManager.getConnection("jdbc:sqlite:" + _dbPath);
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
	
	public void unselectPoi(Player player)
	{
		this._selectedPOIs.remove(player);
	}
	
		
	public Poi getSelectedPOI(Player player)
	{
		Poi poi = this._selectedPOIs.get(player);
		if (poi != null && poi.getWorld().equalsIgnoreCase(player.getWorld().getName())) {
			return poi;
		}
		
		return null;
	}
	
	private Poi _getPoi(int id, Connection conn) throws PoiException
	{
		boolean createdConnection = false;
		if (conn == null) {
			conn = _getDBConn();
			createdConnection = true;
		}
		
		try {
			PreparedStatement sql = conn.prepareStatement(
				SELECT_BASE + 
				"FROM poi " +
				"WHERE id = ?;");
			
			sql.setInt(1, id);
			
			ArrayList<Poi> list = _getPOIs(sql, conn);
			if (list.size() == 0) {
				throw new PoiException(PoiException.NO_POI_AT_ID, "No POI with specified id.");
			}
			
			// id selection always returns exactly one POI.
			return list.get(0);
		}
		catch (PoiException ex) {
			throw ex;
		}
		catch (Exception ex) {
			throw new PoiException(PoiException.SYSTEM_ERROR, ex);
		}
		finally {
			if (createdConnection) {
				_closeConn(conn);
			}
		}
	}
	
	public void selectPOI(int id, Player player) throws PoiException
	{
		// get the POI by id... the method create its own connection
		Poi poi = this._getPoi(id, null);
		
		// make sure the POI is in the Player's current world
		if (!player.getWorld().getName().equals(poi.getWorld())) {
			// poi isn't in the same world as the player.
			throw new PoiException(PoiException.POI_OUT_OF_WORLD, "POI belongs to a different world.");
		}
		
		// if we made it this far, everything went okay, select the poi
		this._selectedPOIs.put(player, poi);
	}
	
	public void removePOI(int id, String name, String owner, String world) throws PoiException
	{
		Connection conn = _getDBConn();
		Poi poi = this._getPoi(id, conn);
		
		// verify that the poi is in the expected world
		if (!world.equals(poi.getWorld())) {
			throw new PoiException(PoiException.POI_OUT_OF_WORLD, "POI belongs to a different world.");
		}
		
		if (!owner.equals(poi.getOwner())) {
			throw new PoiException(PoiException.POI_BELONGS_TO_SOMEONE_ELSE, "POI belongs to someone else.");
		}
		
		if (!name.equalsIgnoreCase(poi.getName())) {
			throw new PoiException(PoiException.POI_NAME_MISMATCH, "Name does not go with this Id.");
		}
		
		try {
			// we made it here, we can perform the DELETE on the database.
			PreparedStatement sql = conn.prepareStatement("DELETE FROM poi WHERE id = ?;");
			sql.setInt(1, id);
			sql.executeUpdate();
		}
		catch (Exception ex) {
			throw new PoiException(PoiException.SYSTEM_ERROR, ex);
		}
	}
	
	public void add(String name, Player player, int minPoiGap, int maxPlayerPoiPerWorld) throws PoiException
	{
		Connection conn = _getDBConn();
		ResultSet rs = null;
		Location location = player.getLocation();

		try {
			ArrayList<Poi> list = new ArrayList<Poi>();
			list = getNearby(location, minPoiGap, 1);
			if (list.size() > 0) {
				throw new PoiException(PoiException.TOO_CLOSE_TO_ANOTHER_POI, "Player is too close to an existing POI threshold: " + minPoiGap);
			}
			
			// check to see if the Player has reached their limit for this world
			PreparedStatement sql = conn.prepareStatement(
				"SELECT count(id) AS count " +
				"FROM poi " + 
				"WHERE owner = ? " +
				"AND world = ?;");
			
			sql.setString(1, player.getName());
			sql.setString(2, location.getWorld().getName());
			
			rs = sql.executeQuery();
			rs.next();
			if (rs.getInt("count") >= maxPlayerPoiPerWorld) {
				throw new PoiException(PoiException.MAX_PLAYER_POI_EXCEEDED);
			}
			_closeResultSet(rs);
			
			sql = conn.prepareStatement("insert into poi (x, y, z, name, owner, world) values (?, ?, ?, ?, ?, ?);");
			sql.setInt(1, (int)location.getX());
			sql.setInt(2, (int)location.getY());
			sql.setInt(3, (int)location.getZ());
			sql.setString(4, name);
			sql.setString(5, player.getName());
			sql.setString(6, location.getWorld().getName());
			sql.executeUpdate();
		}
		catch (PoiException ex) {
			throw ex;
		}
		catch (Exception ex) {
			throw new PoiException(PoiException.SYSTEM_ERROR, ex);
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
	
	private ArrayList<Poi> _getPOIs(PreparedStatement sql, Connection conn) throws SQLException
	{
		ResultSet rs = null;
		Poi poi = null;
		ArrayList<Poi> list = new ArrayList<Poi>();
		
		try {  	
			rs = sql.executeQuery();
			while (rs.next()) {
				poi = new Poi();
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
	
	public ArrayList<Poi> getNearby(Location playerLoc, int maxDistance, int limit) throws PoiException
    {
    	Connection conn = _getDBConn();
 _log.info("" + maxDistance + "|" + limit);   	
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
			throw new PoiException(PoiException.SYSTEM_ERROR, sqlEx);
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
