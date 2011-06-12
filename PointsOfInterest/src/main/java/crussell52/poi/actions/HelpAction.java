package crussell52.poi.actions;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import crussell52.poi.Config;
import crussell52.poi.PoiManager;
import crussell52.poi.commands.PoiCommand;

public class HelpAction extends ActionHandler {
	
	public HelpAction(PoiManager poiManager) {
		super(poiManager);
	}

	@Override
	public void handleAction(CommandSender sender, String action, String[] args) {
		
		ArrayList<String> messages;
		String targetAction = (args.length > 0) ? args[0] : PoiCommand.ACTION_HELP;
		if (targetAction.equalsIgnoreCase(PoiCommand.ACTION_ADD)) {
			messages = this._add(false);
		}
		else if (targetAction.equalsIgnoreCase(PoiCommand.ACTION_LIST)) {
			messages = this._list(false);
		}
		else if (targetAction.equalsIgnoreCase(PoiCommand.ACTION_PAGE)) {
			messages = this._page(false);
		}
		else if (targetAction.equalsIgnoreCase(PoiCommand.ACTION_REMOVE)) {
			messages = this._remove(false);
		}
		else if (targetAction.equalsIgnoreCase(PoiCommand.ACTION_SEARCH)) {
			// another pass
			messages = this._search(false);
		}
		else if (targetAction.equalsIgnoreCase(PoiCommand.ACTION_SELECT)) {
			// good
			messages = this._select(false);
		}
		else if (targetAction.equalsIgnoreCase(PoiCommand.ACTION_SUMMARY)) {
			// good
			messages = this._summary(false);
		}
		else {
			messages = this._generalHelp();
		}
		
		sender.sendMessage("");
		for (String message : messages) {
			sender.sendMessage(message);
		}
	}
		
	private ArrayList<String> _generalHelp() {
		ArrayList<String> messages = new ArrayList<String>();
		
		// set up alternation options for the legend
		ArrayList<String> alternation = new ArrayList<String>();
		alternation.add("this");
		alternation.add("that");
		alternation.add("other");
		
		messages.add(ChatColor.AQUA + "LEGEND: " + ChatColor.WHITE + "/poi action " + 
				this._required("required") +  this._optional("optional")  + this._alternation(alternation, true));
		messages.add("------------------------------");
		
		// provide short usage for every action.		
		messages.add(this._select(true).get(0));
		messages.add(this._summary(true).get(0));
		messages.add(this._add(true).get(0));
		messages.add(this._remove(true).get(0));
		messages.add(this._search(true).get(0));
		messages.add(this._list(true).get(0));
		messages.add(this._page(true).get(0));
		messages.add(this._action(PoiCommand.ACTION_HELP) + this._optional("action") + this._shortDescription("This page or help on an action"));
	
		return messages;
	}
	
	private ArrayList<String> _page(boolean isShort)	{
		ArrayList<String> messages = new ArrayList<String>();
		
		// set up different possibilities for the argument
		ArrayList<String> alternation = new ArrayList<String>();
		alternation.add("number");
		alternation.add("<<");
		alternation.add("<");
		alternation.add(">");
		alternation.add(">>");
		
		String basic = this._action(PoiCommand.ACTION_PAGE) + this._alternation(alternation, true);
		if (isShort) {
			basic += this._shortDescription("Page through last results.");
			messages.add(basic);
			return messages;
		}
		
		messages.add(basic);
		messages.add(ChatColor.GREEN + "------------");
		messages.add(ChatColor.YELLOW + "Use this action to page through your last list of Points of");
		messages.add(ChatColor.YELLOW + "Interest within the current world. If you provide a number,");
		messages.add(ChatColor.YELLOW + "that page will be displayed. You can also use <<, <, >, or >>");
		messages.add(ChatColor.YELLOW + "to view the first, previous, next, or last page. If you do not");
		messages.add(ChatColor.YELLOW + "provide anything, the current page will be redisplayed.");
		
		return messages;
	}
	
	private ArrayList<String> _list(boolean isShort)	{
		ArrayList<String> messages = new ArrayList<String>();
		String basic = this._action(PoiCommand.ACTION_LIST) + this._optional("playerName");
		if (isShort) {
			basic += this._shortDescription("List all POIs belonging to a player");
			messages.add(basic);
			return messages;
		}

		messages.add(basic);
		messages.add(ChatColor.GREEN + "------------");
		messages.add(ChatColor.YELLOW + "Use this action to see all Points of Interest within your");
		messages.add(ChatColor.YELLOW + "current world that belong to a specific player. The first page");
		messages.add(ChatColor.YELLOW + "of results will be shown and " + this._actionXRef(PoiCommand.ACTION_PAGE) + " can be used");
		messages.add(ChatColor.YELLOW + "to see the rest.  The results will contain an id for");
		messages.add(ChatColor.YELLOW + "each POI which can be used to interact further with it.");
		
		return messages;
	}
	
	private ArrayList<String> _search(boolean isShort)	{
		ArrayList<String> messages = new ArrayList<String>();
		String basic = this._action(PoiCommand.ACTION_SEARCH);
		if (isShort) {
			basic += this._shortDescription("Find nearby POIs");
			messages.add(basic);
			return messages;
		}

		messages.add(basic);
		messages.add(ChatColor.GREEN + "------------");
		messages.add(ChatColor.YELLOW + "Use this action to see the " + Config.getMaxSearchResults() + " closest Points of Interest");
		if (Config.getDistanceThreshold() >= 0) {
			messages.add(ChatColor.YELLOW + "within a " + Config.getDistanceThreshold() + " meter (block) radius. ");
		}
		else {
			messages.add(ChatColor.YELLOW + "within your current world.");
		}
		
		messages.add(ChatColor.YELLOW + "The first page of results will be displayed and");
		messages.add(ChatColor.YELLOW + this._actionXRef(PoiCommand.ACTION_PAGE) + " can be used to view the rest. The results ");
		messages.add(ChatColor.YELLOW + "will contain an id for each POI which can be used to");
		messages.add(ChatColor.YELLOW + "further interact with it.");
		
		return messages;
	}
	
	private ArrayList<String> _remove(boolean isShort)	{
		ArrayList<String> messages = new ArrayList<String>();
		String basic = this._action(PoiCommand.ACTION_REMOVE) + this._required("id") + this._required("name");
		if (isShort) {
			basic += this._shortDescription("Remove a POI");
			messages.add(basic);
			return messages;
		}

		messages.add(basic);
		messages.add(ChatColor.GREEN + "------------");
		messages.add(ChatColor.YELLOW + "Use this action to remove a Point of Interest in your");
		messages.add(ChatColor.YELLOW + "current world. To prevent accidental removals, you must");
		messages.add(ChatColor.YELLOW + "provide the id *and* name.  You can only remove a POI");
		messages.add(ChatColor.YELLOW + "that you own.");
		
		return messages;
	}
	
	private ArrayList<String> _summary(boolean isShort)	{
		ArrayList<String> messages = new ArrayList<String>();
		String basic = this._action(null) + this._optional(PoiCommand.ACTION_SUMMARY) + this._optional("id");
		if (isShort) {
			basic += this._shortDescription("Get summary of selected POI");
			messages.add(basic);
			return messages;
		}

		messages.add(basic);
		messages.add(ChatColor.GREEN + "------------");
		messages.add(ChatColor.YELLOW + "Use this action to get a summary and directions of a Point");
		messages.add(ChatColor.YELLOW + "of Interest.  If you provide an id, the related POI will ");
		messages.add(ChatColor.YELLOW + "become your selected POI.  If you do not provide an id, then");
		messages.add(ChatColor.YELLOW + "you will see a summary of your selected POI.");
		
		return messages;
	}
	
	private ArrayList<String> _add(boolean isShort)	{
		ArrayList<String> messages = new ArrayList<String>();
		String basic = this._action(PoiCommand.ACTION_ADD) + this._required("name");
		if (isShort) {
			basic += this._shortDescription("Create a POI");
			messages.add(basic);
			return messages;
		}

		messages.add(basic);
		messages.add(ChatColor.GREEN + "------------");
		messages.add(ChatColor.YELLOW + "Use this action to create a Point of Interest in the");
		messages.add(ChatColor.YELLOW + "current world. You must provide a name which has no spaces");
		messages.add(ChatColor.YELLOW + "and is less than 24 characters long.  Names do not");
		messages.add(ChatColor.YELLOW + "need to be unique, but try to use a name which is");
		messages.add(ChatColor.YELLOW + "different from nearby Points of Interest.");
		
		return messages;
	}

	private ArrayList<String> _select(boolean isShort)	{
		ArrayList<String> messages = new ArrayList<String>();
		String basic = this._action(PoiCommand.ACTION_SELECT) + this._required("id");
		if (isShort) {
			basic += this._shortDescription("Select a POI");
			messages.add(basic);
			return messages;
		}

		messages.add(basic);
		messages.add(ChatColor.GREEN + "------------");
		messages.add(ChatColor.YELLOW + "Use this action to select a Point of Interest using");
		messages.add(ChatColor.YELLOW + "its id. Once you have selected a POI you can use");
		messages.add(ChatColor.YELLOW + this._actionXRef(PoiCommand.ACTION_SUMMARY) + " to view a summary and directions.");
		messages.add(ChatColor.YELLOW + "You can only select a POI that is in your current world.");
		
		return messages;
	}
	
	private String _actionXRef(String action) {
		return ChatColor.YELLOW + "\"" + ChatColor.GOLD + "/poi " + action + ChatColor.YELLOW + "\"";
	}
	
	
	private String _action(String token) {
		return ChatColor.WHITE + "/poi" + (token != null ? " " + token : "");
	}
	
	private String _shortDescription(String token) {
		return ChatColor.GRAY + " (" + token + ")";
	}
	
	private String _required(String token) {
		return ChatColor.GOLD + " <" + ChatColor.WHITE + token + ChatColor.GOLD + ">";
	}
	
	private String _optional(String token) {
		return ChatColor.GREEN + " [" + ChatColor.WHITE + token + ChatColor.GREEN + "]";
	}
	
	private String _alternation(ArrayList<String> tokens, boolean isOptional) {
		String alternation = "";
		int numTokens = tokens.size();
		
		for (int i = 0; i < numTokens - 1; i++) {
			alternation += ChatColor.DARK_AQUA + tokens.get(i) + ChatColor.AQUA + " | ";
		}
		
		alternation += ChatColor.DARK_AQUA + tokens.get(numTokens - 1);
		
		return isOptional ? this._optional(alternation) : this._required(alternation);
	}

}
