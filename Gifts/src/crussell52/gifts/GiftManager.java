package crussell52.gifts;

import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Level;

public class GiftManager 
{
    private String _server;
    private String _username;
    private String _password;
    private Gifts _plugin;
    
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
    
    protected Connection _connect() throws Exception
    {
        Connection conn = null;
        Statement st = null;
        ResultSet rs = null;
        
        try 
        {
            conn = DriverManager.getConnection(this._server, this._username, this._password);
            st = conn.createStatement();
            rs = st.executeQuery("SELECT * FROM Gifts");
            
            while (rs.next()) {
                System.out.println(rs.getString("username") + ":" + rs.getString("amount") + ":" + rs.getString("Memo"));
            }
        }
        catch (Exception ex)
        {
            try { conn.close(); }
            catch (Exception closeEx) { }
            
            throw ex;
        }
        finally {
            
            try { rs.close(); }
            catch (Exception ex) { }
            
            try { st.close(); }
            catch (Exception ex) { }
        }
        
        return conn; 
    }
}
