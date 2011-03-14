package crussell52.RubySlippers;
 
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class CostManager {
	/**
	 * Mapping of Material -> Cost
	 */
	private final Map<Material, Double> _materialCosts = new HashMap<Material, Double>();
	
	/**
	 * Uses the received map to set all material costs.
	 * 
	 * @param map
	 */
	public void loadMaterialCosts(Map<String, Object> map) {
		// remove all previously recorded costs.
		_materialCosts.clear();
		
		// create a local var for working with the cost before we write it to 
		// the final map
		Double cost = 0d;
		
		// loop over the received map
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			
			// attempt to get a Material using the provided key
			Material material = Material.matchMaterial(entry.getKey());
			if (material == null) {
				System.out.println("no name match on " + entry.getKey() + "; trying by id");
				
				// we failed to get a Material, so the the key was unrecognized.
				// maybe it was the material id... 
				try {
					material = Material.getMaterial(Integer.parseInt(entry.getKey()));
				}
				catch (NumberFormatException nfe) {
					// failed to parse into an integer
					// make sure material has a null value
					material = null;
				}
				finally {
					// success or failure, see if we have a material now.
					if (material == null) {
						// still no material... nothing left to do but report the unrecogized material
						// amd move on to the next one.
						System.out.println("RubySlippers: Skipping unrecognized material: " + entry.getKey());
						continue;
					}
				}
			}
			
			// try to read in the cost as a double first
			try {
				cost = (Double)entry.getValue();
			}
			catch (ClassCastException ex) {
				// failed reading it in as a double... 
				// it might be written as an int
				try {
					cost = (double)(Integer)entry.getValue();
				}
				catch (ClassCastException ex2) {
					// Also not configured as an int... it is misconfigured.
					// notify the console and move to the next material.
					System.out.println("RubySlippers: Skipped invalid cost for: " + entry.getKey());
					continue;
				}
			}
			
			// whole number represents flat amount.
			// flat amounts don't support decimal, so use floor to make sure
			// we only capture the whole value.
			if (cost >= 1) {
				cost = (double) Math.floor(cost);
			}
			
			// push the material and the cost into the list
			_materialCosts.put(material, cost);
		} 
	}
	
	/**
	 * Returns a mapping of Material->Cost indicating how many of
	 * each material the player would lose if they teleported home 
	 * right now.
	 * 
	 * @param player
	 * @return
	 */
	public Map<Material, Integer> getCosts(Player player) {
		
		// set up some aggregate vars
		int total = 0;
    	int totalRemove = 0;
    	double materialCost = 0d;
    	
    	// we're going to be working with the player inventory a lot,
    	// so pull it out once and assign it locally
    	PlayerInventory inv = player.getInventory();
    	
    	// we need a variable to hold stacks of materials as we pull them
    	// out of the player's inventory.
    	Map<Integer, ? extends ItemStack> materialStacks;
    	
    	// Create the map which will be returned as output from this 
    	// method.
    	Map<Material, Integer> cost = new HashMap<Material, Integer>();
    	
    	// loop over each material with a configured cost
    	for (Map.Entry<Material, Double> entry : _materialCosts.entrySet()) {
    		// see if the player has any of this material
    		if (!inv.contains(entry.getKey())) {
    			// material not present, move on to next one.
    			continue;
    		}
    		
    		// player has some of the material...
    		// reset aggregate vars
    		total = 0;
    		totalRemove = 0;
    		materialCost = 0d;

    		// we know that we have some, get all stacks of given material
    		materialStacks = inv.all(entry.getKey());
    		
    		// pull out the configured cost
    		materialCost = entry.getValue();
    		
    		// find out how many of the material the user has
    		// across all stacks.
			for (ItemStack value : materialStacks.values()) {
        		total += value.getAmount();
        	}

    		// see if we are working with a flat rate, or a percent.
    		if (materialCost < 1) {
    			// less than 1 means a percent... we need to do some math.
    			totalRemove = (int)Math.ceil(total * materialCost);
    		}
    		else {

    			// at least one, so this is a flat amount.
    			// we remove as much as we can up to the flat amount
    			totalRemove = (int)Math.min(materialCost, (double)total);
    		}
    		
    		// record the calculated cost
    		cost.put(entry.getKey(), totalRemove);
		}
    	
    	// return the mapping of Material->Cost
    	return cost;
	}
}
