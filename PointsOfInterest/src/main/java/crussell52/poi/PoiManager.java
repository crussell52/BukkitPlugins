package crussell52.poi;

import crussell52.poi.api.PoiEvent;
import crussell52.poi.markers.MarkerManager;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.sqlite.Function;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@SuppressWarnings({"JavaDoc", "FieldCanBeLocal"})
public class PoiManager {

    /**
     * Used for logging as necessary throughout this class.
     *
     * Exception stack traces are still output to the standard error out.
     */
    private Logger _log;

    /**
     * Keeps track of the most recent, paged results of a player.
     */
    private final Map<Player, Map<String, PagedPoiList>> _pagedResults = new HashMap<Player, Map<String, PagedPoiList>>();

    /**
     * Keeps track of the most recent, unpaged area results of each player.
     */
    private final Map<Player, PoiResults> _areaSearchCache = new HashMap<Player, PoiResults>();

    /**
     * Keeps track of which POI each player has selected.
     */
    private Map<Player, Poi> _selectedPOIs = new HashMap<Player, Poi>();

    /**
     * Maximum number of characters allowed for a POI name
     */
    public static final int MAX_NAME_LENGTH = 31;

    /**
     * SQL SELECT statement fragment used as a basis for all SELECT statements.
     */
    private static final String SELECT_BASE = "SELECT id, name, world, owner, x, y, z ";

    /**
     * The canonical path to the database which stores POIs.
     */
    private String _dbPath;

    /**
     * The most recent database version -- this is the version of the database
     * which is compatible with this version of the plugin.
     */
    private final int LATEST_DB_VERSION = 2;

    /**
     * This is the current version of the database according to
     * PRAGMA user_version.
     *
     * This is used to detect whether database alterations are necessary
     * or database incompatibilities.
     */
    private int _currentDBVersion;

    private MarkerManager _markerManager;

    /**
     * Attempts to make necessary preparations for reading/writing POIs and returns a
     * boolean indicator of success.
     *
     * @param pluginDataFolder the main data folder of the plugin
     */
    public boolean initialize(File pluginDataFolder, Logger logger)
    {
        // Capture the logger
        _log = logger;

        // attempt to get the database ready
        Connection conn = null;
        Boolean success = false;
        try {

            // make sure we have a folder for the database and get
            // the canonical path to the db.
            File dbFolder = new File(pluginDataFolder, "db");
            if (!dbFolder.exists() && !dbFolder.mkdir()) {
                throw new Exception("Failed to create db directory.");
            }
            _dbPath = new File(dbFolder, "POI.db").getCanonicalPath();

            // make a connection (which will create the database if necessary)
            conn = _getDBConn();

            // perform setup operations on the database
            success =_setupDB(conn);
        }
        catch (Exception ex) {
            // something went wrong, output the exception stacktrace.
            ex.printStackTrace();
        }
        finally {
            // success or failure, don't leave a dangling
            // db connection
            _closeConn(conn);
        }

        return success;
    }

    public void setMarkerManager(MarkerManager markerManager) {
        _markerManager = markerManager;
    }

    /**
     * Makes and returns a database connection.
     */
    private Connection _getDBConn(){
        try {
            Class.forName("org.sqlite.JDBC");
            return DriverManager.getConnection("jdbc:sqlite:" + _dbPath);
        }
        catch (Exception ex) {
            // something went wrong, output the stack trace
            ex.printStackTrace();
        }

        return null;
    }

    /**
     * Get the recent paged results for a given player.
     *
     * @param player
     * @return
     */
    public PagedPoiList getPagedResults(Player player)
    {
        try {
            return this._pagedResults.get(player).get(player.getWorld().getName());
        }
        catch (Exception ex) {
            return null;
        }
    }

    /**
     * Set the most recent paged results for a player.
     *
     * @param player
     * @param results
     */
    public void setPagedResults(Player player, PagedPoiList results)
    {
        // see if we already have a place to store results for this player
        if (!this._pagedResults.containsKey(player)) {
            // we do not, create one.
            this._pagedResults.put(player, new HashMap<String, PagedPoiList>());
        }

        // key the results by player and world.
        this._pagedResults.get(player).put(player.getWorld().getName(), results);
    }

    /**
     * Unselect the currently selected POI for a given player.
     *
     * @param player
     */
    public void unselectPoi(Player player)
    {
        this._selectedPOIs.remove(player);

        // tell the plugin to notify listeners of the unselect.
        PointsOfInterest.notifyListeners(PoiEvent.unselectEvent(player));
    }

    /**
     * Unselect the currently selected POI for a given player if it matches the given id.
     *
     * @param player
     */
    public void unselectPoi(Player player, int poiID)
    {
        if (_selectedPOIs.containsKey(player) && _selectedPOIs.get(player).getId() == poiID) {
            unselectPoi(player);
        }
    }

    /**
     * Return the currently selected POI for a given player.
     *
     * If the player does not have a selected POI, or the currently selected POI
     * belongs to a world other than the player's current world, null will be returned.
     *
     * @param player
     * @return
     */
    public Poi getSelectedPoi(Player player)
    {
        // get the selected POI, and make sure it is in the player's current world.
        Poi poi = this._selectedPOIs.get(player);
        if (poi != null && poi.getWorld().equals(player.getWorld().getName())) {
            return poi;
        }

        return null;
    }

    public List<Poi> getAll() throws PoiException {
        Connection conn = _getDBConn();
        ResultSet rs = null;
        List<Poi> results = new ArrayList<Poi>();

        try {
            PreparedStatement sql = conn.prepareStatement(SELECT_BASE + "FROM poi;");
            _getPOIs(sql, results);
            return results;
        }
        catch (Exception ex) {
            throw new PoiException(PoiException.SYSTEM_ERROR, ex);
        }
        finally {
            _closeConn(conn);
            _closeResultSet(rs);
        }
    }

    public List<Poi> getChunkPoi(Chunk chunk) throws PoiException {
        Connection conn = _getDBConn();
        ResultSet rs = null;
        List<Poi> results = new ArrayList<Poi>();

        try {
            PreparedStatement sql = conn.prepareStatement(SELECT_BASE +
                    "FROM poi " +
                    "WHERE x >= ? AND x <= ? " +
                    "AND z >= ? AND z <= ?;"
                    );

            int minX = chunk.getX() << 4;
            int minZ = chunk.getZ() << 4;

            sql.setInt(1, minX);
            sql.setInt(2, minX + 15);
            sql.setInt(3, minZ);
            sql.setInt(4, minZ + 15);

            _getPOIs(sql, results);

            return results;
        }
        catch (Exception ex) {
            throw new PoiException(PoiException.SYSTEM_ERROR, ex);
        }
        finally {
            _closeConn(conn);
            _closeResultSet(rs);
        }
    }

    /**
     * Internal helper method for getting a POI by id.
     *
     * @param id
     * @param conn
     *
     * @throws PoiException
     */
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

            PoiResults results = new PoiResults();
            _getPOIs(sql, results);
            if (results.size() == 0) {
                throw new PoiException(PoiException.NO_POI_AT_ID, "No POI with specified id.");
            }

            // id selection always returns exactly one POI.
            return results.get(0);
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

    public Poi getPoi(int id) {
        // get the POI by id... Let the method create its own connection.
        try {
            return this._getPoi(id, null);
        } catch (PoiException e) {
            return null;
        }
    }

    /**
     * Finds the POI with the given id and makes it the given player's selected POI.
     *
     * @param id
     * @param player
     *
     * @throws PoiException
     */
    public void selectPOI(int id, Player player) throws PoiException
    {
        // get the POI by id... the method create its own connection
        Poi poi = this._getPoi(id, null);

        // make sure the POI is in the Player's current world
        World poiWorld = player.getServer().getWorld(poi.getWorld());
        if (poiWorld == null || poiWorld != player.getWorld()) {
            // poi isn't in the same world as the player.
            throw new PoiException(PoiException.POI_OUT_OF_WORLD, "POI belongs to a different world.");
        }

        selectPOI(poi, player);
    }

    public void selectPOI(Poi poi, Player player)
    {
        // if we made it this far, everything went okay, select the poi
        this._selectedPOIs.put(player, poi);

        // tell the plugin to notify listeners of the select.
        PointsOfInterest.notifyListeners(PoiEvent.selectEvent(player, poi));
    }

    /**
     * Deletes a POI from the database by id.
     *
     * @param id
     * @param conn
     * @throws PoiException
     */
    private void _deletePOI(int id, Connection conn) throws PoiException
    {
        // attempt to perform the delete against the given connection
        try {
            PreparedStatement sql = conn.prepareStatement("DELETE FROM poi WHERE id = ?;");
            sql.setInt(1, id);
            sql.executeUpdate();
        }
        catch (Exception ex) {
            throw new PoiException(PoiException.SYSTEM_ERROR, ex);
        }
    }

    /**
     * Remove the POI which has the given id, and name.
     *
     * @param id
     * @param name
     * @throws PoiException
     *
     * @return A handle to the removed POI.
     */
    public Poi removePOI(int id, String name) throws PoiException
    {
        Connection conn = null;
        try {
            conn = _getDBConn();
            Poi poi = this._getPoi(id, conn);

            if (!name.equalsIgnoreCase(poi.getName())) {
                throw new PoiException(PoiException.POI_NAME_MISMATCH, "Name does not go with this Id.");
            }

            this._deletePOI(id, conn);
            if (_markerManager != null) {
                _markerManager.removeMarker(poi);
            }
            return poi;
        }
        finally {
            // no matter what, close the connection after we execute.
            this._closeConn(conn);
        }
    }

    /**
     * Remove the POI which has the given id, name, owner, and world.
     *
     * @param id
     * @param name
     * @param owner
     * @param world
     * @throws PoiException
     */
    public void removePOI(int id, String name, String owner, String world) throws PoiException
    {
        Connection conn = null;

        try {
            conn = _getDBConn();
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

            this._deletePOI(id, conn);
        }
        finally {
            // no matter what, close the connection after we execute.
            this._closeConn(conn);
        }
    }

    public Poi add(String name, String playerName, Location location, int minPoiGap, int maxPlayerPoiPerWorld) throws PoiException
    {
        Connection conn = _getDBConn();
        ResultSet rs = null;
        ResultSet keys = null;

        try {
            ArrayList<Poi> list = getNearby(location, minPoiGap, 1);
            if (list.size() > 0) {
                throw new PoiException(PoiException.TOO_CLOSE_TO_ANOTHER_POI, "Player is too close to an existing POI threshold: " + minPoiGap);
            }

            // check to see if the Player has reached their limit for this world
            PreparedStatement sql = conn.prepareStatement(
                    "SELECT count(id) AS count " +
                            "FROM poi " +
                            "WHERE owner = ? " +
                            "AND world = ?;");

            sql.setString(1, playerName);
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
            sql.setString(5, playerName);
            sql.setString(6, location.getWorld().getName());
            sql.executeUpdate();
            keys = sql.getGeneratedKeys();
            keys.next();
            Poi newPoi = _getPoi(keys.getInt(1), conn);
            if (_markerManager != null) {
                _markerManager.addMarker(newPoi);
            }

            return newPoi;
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
            _closeResultSet(keys);
        }
    }

    /**
     * Create a distance function in the database for performing distance queries.
     *
     * @param conn
     * @throws SQLException
     */
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

    /**
     * Internal helper method for getting a list of <code>Poi</code> instances using a given
     * <code>PreparedStatement</code>.
     *
     * @param sql
     *
     * @throws SQLException
     */
    private void _getPOIs(PreparedStatement sql, List<Poi> results) throws SQLException
    {
        ResultSet rs = null;
        Poi poi;

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
                results.add(poi);
            }
        }
        finally {
            _closeResultSet(rs);
        }
    }

    /**
     * Return a list of POIs belonging to a specific owner within a given world.
     *
     * @param currentWorld
     * @param owner
     * @return
     * @throws PoiException
     */
    public ArrayList<Poi> getOwnedBy(World currentWorld, String owner) throws PoiException
    {
        Connection conn = _getDBConn();
        try {
            PreparedStatement sql = conn.prepareStatement(
                SELECT_BASE +
                "FROM poi " +
                "WHERE owner like ? " +   // case insensitive search
                "AND world = ? ");

            sql.setString(1, owner);
            sql.setString(2, currentWorld.getName());

            PoiResults results = new PoiResults();
            _getPOIs(sql, results);
            return results;
        }
        catch (SQLException sqlEx) {
            throw new PoiException(PoiException.SYSTEM_ERROR, sqlEx);
        }
        finally {
            _closeConn(conn);
        }
    }

    /**
     * Returns a list of POIs within a given distance of a specified player.
     *
     * Results from this method are cached.
     *
     * @param player Player which distance is calculated against
     *
     * @throws PoiException
     */
    public PoiResults getNearby(Player player) throws PoiException
    {
        PoiResults results;
        if (this._areaSearchCache.containsKey(player)) {
            // Only use the cached value if it isn't too old has a similar enough center point.
            results = this._areaSearchCache.get(player);
            if ((System.currentTimeMillis() - results.getCreated()) <= 10000 &&
                    results.getSearchCenter().distance(player.getLocation()) <= 5) {
                // Cache is valid, return it.
                return results;
            }
        }

        // If we're here, then there was no cache.
        results = getNearby(player.getLocation(), Config.getDistanceThreshold(), Config.getMaxSearchResults());
        this._areaSearchCache.put(player, results);
        return results;
    }

    /**
     * Returns a list of POIs within a given distance of a specified location.
     *
     * @param location Location which distance is calculated against
     * @param maxDistance maximum distance to look
     * @param limit maximum number of POIs to find - closest will be returned
     *
     * @throws PoiException
     */
    public PoiResults getNearby(Location location, int maxDistance, int limit) throws PoiException
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

            sql.setInt(1, (int)location.getX());
            sql.setInt(2, (int)location.getY());
            sql.setInt(3, (int)location.getZ());
            sql.setInt(4, maxDistance);
            sql.setString(5, location.getWorld().getName());
            sql.setInt(6, limit);

            PoiResults results = new PoiResults(location);
            _getPOIs(sql, results);
            return results;
        }
        catch (SQLException sqlEx) {
            throw new PoiException(PoiException.SYSTEM_ERROR, sqlEx);
        }
        finally {
            _closeConn(conn);
        }
    }

    /**
     * Exception-tolerant method for closing a <code>Connection</code>.
     *
     * @param conn
     */
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

    /**
     * Exception-tolerant method for closing a <code>ResultSet<code>.
     *
     * @param rs
     */
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

    /**
     * Performs database setup as needed.
     *
     * @param conn
     * @return
     */
    private boolean _setupDB(Connection conn) {
        ResultSet rs;

        try {
            Statement sql = conn.createStatement();

            // get the current database version
            rs = sql.executeQuery("PRAGMA user_version;");
            rs.next();
            this._currentDBVersion = rs.getInt(1);

            // see if the poi table exists
            rs = sql.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='poi';");
            if (!rs.next()) {
                // we don't have a poi table.

                // clean up and start a transaction
                _closeResultSet(rs);
                conn.setAutoCommit(false);

                // set to the latest db version
                sql.executeUpdate("PRAGMA user_version = " + LATEST_DB_VERSION + ";");

                _createTable(conn);

                 conn.setAutoCommit(true);

                 // no reason to query for the db version... we just set it.
                 this._currentDBVersion = LATEST_DB_VERSION;
            }
            else {
                // Clean up previous result set.
                _closeResultSet(rs);

                // the poi table exists -- see if we need to do any migration work.
                if (this._currentDBVersion > LATEST_DB_VERSION) {
                    _log.severe("PointsOfInterest: Database is a later version than expected! " +
                        "Can not safely modify database. Update plugin, restore database backup or " +
                        "delete the database file (all POIs will be lost)."
                        );
                    return false;
                }
                else if (this._currentDBVersion < LATEST_DB_VERSION) {
                    // Everything can be directly migrated to version 2.
                    _migrateToV2DB(conn);
                }
            }
        }
        catch (SQLException sqlEx) {
            _log.severe("Failed to setup POI database");
            sqlEx.printStackTrace();
            return false;
        }
        finally {
            _closeConn(conn);
        }

        return true;
    }

    private void _migrateToV2DB(Connection conn) throws SQLException {
        _log.info("Migrating database from version " + _currentDBVersion + " to version 2.");

        try {
            conn.setAutoCommit(false);
            Statement sql = conn.createStatement();
            sql.executeUpdate("DROP TABLE IF EXISTS `poi-old`");
            sql.executeUpdate("CREATE TABLE `poi-old` as select * from `poi`;");
            _log.info("Dropping old.");
            sql.executeUpdate("DROP TABLE `poi`");
            _createTable(conn);
            _log.info("Copying data into new.");
            sql.executeUpdate("INSERT INTO `poi` SELECT * FROM `poi-old`;");
            _log.info("Updating DB version.");
            sql.executeUpdate("PRAGMA user_version = " + LATEST_DB_VERSION + ";");
            conn.commit();
            conn.setAutoCommit(true);
        }
        catch (SQLException ex) {
            _log.severe("Database migration failed! Exception to follow.");
            _log.severe(ex.toString());
            conn.rollback();
            throw ex;
        }

        _log.info("Database Migration complete.");
    }

    private void _createTable(Connection conn) throws SQLException {
        Statement sql = conn.createStatement();
        _log.info("Creating new table.");
        // the poi table doesn't exist... we need to create it.
        sql.executeUpdate("CREATE TABLE `poi` " +
                "(`id` INTEGER PRIMARY KEY , " +
                "`x` INTEGER NOT NULL ," +
                "`y` INTEGER NOT NULL ," +
                "`z` INTEGER NOT NULL ," +
                "`owner` STRING(16) NOT NULL, " +
                "`world` STRING NOT NULL, " +
                "`name` STRING(" + PoiManager.MAX_NAME_LENGTH +") NOT NULL);"
        );
    }

    public Poi getPoiAt(Location location) {
        try {
            PoiResults results = getNearby(location, 0, 1);
            if (results.size() > 0) {
                return results.get(0);
            }
        }
        catch (Exception ignored) {}

        return null;
    }
}
