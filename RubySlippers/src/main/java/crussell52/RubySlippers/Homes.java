package crussell52.RubySlippers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

public class Homes {
	/**
	 * Mapping of Worlds -> Users -> Home Coordinates
	 */
	private Map<String, Map<String, ArrayList<Integer>>> _homes = new HashMap<String, Map<String, ArrayList<Integer>>>();
	
	/**
	 * Yaml instance for handling saved data (.yml file)
	 */
	private static final Yaml _yaml = new Yaml(new SafeConstructor());
	
	/**
	 * reference to the data file which is used to persist home data.
	 */
	private  File _dataFile;
	
	/**
	 * Loads saved data from homes.yml under specified data folder, making it the active
	 * file for reading/writing home data.
	 * 
	 * @param dataFolder
	 * @throws IOException
	 * @throws ClassCastException
	 * @throws FileNotFoundException
	 */
	@SuppressWarnings("unchecked")
	public void load(File dataFolder) throws IOException, ClassCastException, FileNotFoundException {
		// keep a handle to the file so we don't need to re-instantiate everytime it is used.
		_dataFile = new File(dataFolder, "homes.yml");
		
		try {
			// parse the file into map property.
			_homes = (Map<String, Map<String, ArrayList<Integer>>>)_yaml.load(new FileInputStream(_dataFile));
		}
		finally {
			// regardless of error or not, make sure we have a map to store _homes
			if (_homes == null) {
				_homes = new HashMap<String, Map<String, ArrayList<Integer>>>();
			}
		}		
	}
	
	/**
	 * Writes current home data to file to persist across server sessions.
	 * 
	 * @throws IOException
	 */
	public void save() throws IOException {
	  // create a file writer in "overwrite" mode
	  // and use Yaml to dump the data into the file.
	  FileWriter writer = new FileWriter(_dataFile, false);
	  _yaml.dump(_homes, writer);	
	  writer.close();
	}
	
	/**
	 * Sets a player's home to their current location.
	 * 
	 * @param player
	 * @return
	 */
	public Location setHome(Player player) {
		
		// pull out the players location so we don't have to keep using the getter
		Location playerLoc = player.getLocation();
		
		// create an ArrayList of integers to represent the x/y/z of the player's new home.
		ArrayList<Integer> home = new ArrayList<Integer>();
		home.add((int)playerLoc.getX());
		home.add((int)playerLoc.getY());
		home.add((int)playerLoc.getZ());
		
		// make sure we have a Map to store data for the player's current world.
		if (!_homes.containsKey(player.getWorld().getName())) {
			_homes.put(player.getWorld().getName(), new HashMap<String, ArrayList<Integer>>());
		}
		
		// record the players new home and return it
		_homes.get(player.getWorld().getName()).put(player.getName(), home);
		return playerLoc;
	}
	
	/**
	 * Returns a players home.
	 * 
	 * If the player does not have a home set, then <code>null</code> is returned.
	 * 
	 * @param player
	 * @return
	 */
	public Location getHome(Player player) {
		try {
			ArrayList<Integer> coords = _homes.get(player.getWorld().getName()).get(player.getName());
			return new Location(player.getWorld(), coords.get(0), coords.get(1), coords.get(2));
		}
		catch (Exception e) {
			e.printStackTrace();
			// bad or non-existent data... 
			// do nothing about it... just report the error to the server
			// TODO: this is kind of lazy... should distinguish between missing data and bad data.
		}
		
		return null;
	}
}
