package crussell52.gifts;

import org.bukkit.configuration.file.FileConfiguration;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;

public class GiftManager 
{
    private String _server;
    private String _username;
    private String _password;
    private Gifts _plugin;
    
    private static String _table = "Gifts";
    
    public GiftManager(Gifts plugin)
    {
        this._plugin = plugin;
        try 
        {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        }
        catch (Exception ex) { } // do nothing about it. Connections will fail.
    }
    
    public boolean initialize(FileConfiguration config)
    {
        this._server = "jdbc:mysql://" + config.getString("server") + "/" + config.getString("database");
        this._username  = config.getString("username");
        this._password = config.getString("password");
        
        // try a test connection
        Connection conn = null;
        try
        {
            conn = _connect();
        }
        catch (Exception ex)
        {
            this._plugin.log.log(Level.SEVERE, ex.toString());
            this._plugin.log.log(Level.SEVERE, "Failed to establish connection to db. Bad configuration?");
            return false;
        }
        finally
        {
            try { conn.close(); }
            catch (Exception ex) { }
        }
        
        return true;
    }
    
    protected ResultSet _fetchPlayerGifts(String playerName, Connection conn) throws Exception
    {
        PreparedStatement st = null;
        ResultSet rs = null;
        try 
        {
            st = conn.prepareStatement("SELECT id, amount, collectedAmount FROM " + _table +
                     " WHERE collectedAmount < amount AND username = ? ORDER BY collectedAmount DESC, id ASC");
            st.setString(1, playerName.toLowerCase());
            rs = st.executeQuery();
            return rs;
        }
        catch (Exception e)
        {
            // NOTE: We only close these off on exception. Otherwise the calling
            //       code can't use the returned ResultSet.  It is up to the calling
            //       code to clean up!
            
            // Encountered a problem, make sure to clean up the ResultSet
            try { rs.close(); }
            catch (Exception ex) { }
            
            try { st.close(); }
            catch (Exception ex) { }

            
            // re-throw the exception
            throw e;
        }
    }
    
    public boolean collectXP(String playerName, int amount) throws Exception
    {
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement st = null;
        
        try 
        {
            conn = this._connect();
            conn.setAutoCommit(false);
            rs = this._fetchPlayerGifts(playerName, conn);
            if (this._totalAvailXP(rs) < amount) 
            {
                return false;
            }
            
            int previouslyCollected = 0;
            int collected = 0;
            st = conn.prepareStatement("UPDATE " + _table + " SET collectedAmount=? WHERE id=?");
            
            rs.beforeFirst();
            while (rs.next() && amount > 0) 
            {
                previouslyCollected = rs.getInt("collectedAmount");
                collected = Math.min(amount, (rs.getInt("amount") - previouslyCollected));
                amount -= collected;
                st.setInt(1, previouslyCollected + collected);
                st.setInt(2, rs.getInt("id"));
                if (st.executeUpdate() != 1) {
                    throw new Exception("Unexpected number of rows affected by update.");
                }
            }
            
            if (amount > 0)
            {
                throw new Exception("Unexpectedly ran out of available XP.");
            }
            
            conn.commit();
            return true;
        }
        catch (Exception ex)
        {
            
            try { conn.rollback(); }
            catch (Exception e){ };
            
            throw ex;
        }
        finally
        {
            try { st.close(); }
            catch (Exception ex) { }
            
            try { rs.close(); }
            catch (Exception ex) { }
            
            try { conn.setAutoCommit(true); conn.close(); }
            catch (Exception ex) { }
        }
    }
    
    protected int _totalAvailXP(ResultSet rs) throws Exception
    {
        int total = 0;
        rs.beforeFirst();
        while (rs.next()) {
            total += (rs.getInt("amount") - rs.getInt("collectedAmount"));
        }
        
        return total;
    }
    
    public int getAvailXP(String playerName) throws Exception
    {
        Connection conn = null;
        ResultSet rs = null;
        
        try 
        {
            conn = this._connect();
            rs = _fetchPlayerGifts(playerName, conn);
            return this._totalAvailXP(rs);
        }
        finally 
        {
            try { rs.getStatement().close(); }
            catch (Exception ex) { }
            
            try { rs.close(); }
            catch (Exception ex) { }
            
            try { conn.close(); }
            catch (Exception ex) { }
        }
    }
    
    protected Connection _connect() throws Exception
    {
        Connection conn = null;        
        try 
        {
            conn = DriverManager.getConnection(this._server, this._username, this._password);
        }
        catch (Exception ex)
        {
            try { conn.close(); }
            catch (Exception closeEx) { }
            
            throw ex;
        }
        
        return conn; 
    }
}
