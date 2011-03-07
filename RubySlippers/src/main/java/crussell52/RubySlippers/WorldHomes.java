package crussell52.RubySlippers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;


public class WorldHomes extends Properties {
	static final long serialVersionUID = 0L;
	protected World _world;
	protected File _file;
	
	public World getWorld() {
		return _world;
	}
	
	public void load(World world) throws IOException {
		_world = world;
		
		File worldNamesFile = new File(RubySlippers.dataDir + world.getName() + "_homes.properties");
		if (!worldNamesFile.exists()) {
			worldNamesFile.createNewFile();
		}
		
		_file = worldNamesFile;
		
		load(new FileInputStream(worldNamesFile));
	}
	
	public Location get(Player player) {
		String property = getProperty(player.getName());
		if (property != null) {
			String[] locationValues = property.split(":");
			if (locationValues.length == 3) {
				return new Location(_world, Float.parseFloat(locationValues[0]), Float.parseFloat(locationValues[1]), Float.parseFloat(locationValues[2]));
			}
		}		
		
		return _world.getSpawnLocation();
	}
	
	public Object put(Player player, Location location) {
		return super.put(player.getName(), Double.toString(location.getX()) + ":" +  Double.toString(location.getY()) + ":" + Double.toString(location.getZ()));	
	}
	
	public void store() throws IOException, FileNotFoundException  {
		try {
			store(new FileOutputStream(_file), null);
		} catch (FileNotFoundException fnfEx) {
			// try to recover from file not found by creating the file
			_file.createNewFile();
			store(new FileOutputStream(_file), null);
		}
	}
}
