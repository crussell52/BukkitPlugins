package crussell52.poi.actions;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import crussell52.poi.PoiManager;
import crussell52.poi.commands.PoiCommand;

public class HelpAction extends ActionHandler {
	
	public HelpAction(PoiManager poiManager) {
		super(poiManager);
	}

	@Override
	public void handleAction(CommandSender sender, String action, String[] args) {
		this._generalHelp(sender);
	}
	
	
	private void _generalHelp(CommandSender sender) {
		ArrayList<String> alternation = new ArrayList<String>();
		alternation.add("this");
		alternation.add("that");
		alternation.add("other");
		
		sender.sendMessage(ChatColor.AQUA + "LEGEND: " + ChatColor.WHITE + "/poi action " + 
				this._required("required") +  this._optional("optional")  + this._alternation(alternation, true));
		sender.sendMessage("------------------------------");
		
		
		// set up alternations for search example
		alternation.clear();
		alternation.add("number");
		alternation.add("<<");
		alternation.add("<");
		alternation.add(">");
		alternation.add(">>");
		
		sender.sendMessage(this._action(PoiCommand.ACTION_SELECT) + this._required("id") + this._shortDescription("Select a POI."));
		sender.sendMessage(this._action(null) + this._optional(PoiCommand.ACTION_SUMMARY) + this._optional("id") + this._shortDescription("Get summary of selected POI."));
		sender.sendMessage(this._action(PoiCommand.ACTION_ADD) + this._required("name") + this._shortDescription("Create a POI."));
		sender.sendMessage(this._action(PoiCommand.ACTION_REMOVE) + this._required("id") + this._required("name") + this._shortDescription("Remove a POI."));
		sender.sendMessage(this._action(PoiCommand.ACTION_SEARCH) + this._shortDescription("Find nearby POIs."));
		sender.sendMessage(this._action(PoiCommand.ACTION_PAGE) + this._alternation(alternation, true) + this._shortDescription("Page through last search."));
		sender.sendMessage(this._action(PoiCommand.ACTION_HELP) + this._optional("action") + this._shortDescription("This page or help on an action."));
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
