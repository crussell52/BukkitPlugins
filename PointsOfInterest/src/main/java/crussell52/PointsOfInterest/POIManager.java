package crussell52.PointsOfInterest;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.sqlite.Function;

public class POIManager {
	
	public boolean initialize()
	{
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
	
	public void getPOIs(String owner) {
		
	}
	
	public void addLocation(String name, Player player, int distanceThreshold) throws POIException
	{
		Connection conn = _getDBConn();
		ResultSet rs = null;
		Location location = player.getLocation();

		try {
			ArrayList<POI> list = new ArrayList<POI>();
			list = getPOIs(location, distanceThreshold, 1);
			if (list.size() > 0) {
				Iterator<POI> it = list.iterator();
				while (it.hasNext()) {
					System.out.println(it.next());
				}
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
	            	System.out.println(e);
	            	throw new SQLException("Unable to calculate distance - invalid parameters");
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
				list.add(poi);
			}
			
			return list;		
		}
		finally {
			_closeResultSet(rs);
		}
	}
	
	public ArrayList<POI> getPOIs(Location playerLoc, int maxDistance, int limit) throws POIException
    {
    	Connection conn = _getDBConn();
    	
		try {
			_createDistanceFunc(conn);
			PreparedStatement sql = conn.prepareStatement(
				"SELECT id, name, owner, x, y, z, distance(?, ?, ?, poi.x, poi.y, poi.z) AS distance " + 
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
			System.out.println("Failed to close Connection");
			System.out.println(ex);
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
			System.out.println("Failed to close ResultSet");
			System.out.println(ex);
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
	        		"`owner` STRING(16), " +
	        		"`world` STRING, " + 
	        		"`name` STRING(16) );"
	        		);
		}
		catch (SQLException sqlEx) {
			System.out.println("Failed to setup POI database");
			System.out.println(sqlEx);
		}
		finally {
			_closeConn(conn);
		}
    }
	
}
