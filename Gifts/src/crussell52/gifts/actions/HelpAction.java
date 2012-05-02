package crussell52.gifts.actions;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import crussell52.gifts.GiftManager;
import crussell52.gifts.commands.GiftsCommand;

public class HelpAction extends ActionHandler 
{
	/**
	 * {@inheritDoc}
	 * 
	 * @param poiManager
	 */
	public HelpAction(GiftManager giftManager) 
	{
		super(giftManager);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleAction(CommandSender sender, String action, String[] args) 
	{
		if (!this._canExecute(sender)){
			return;
		}
		
		// create a list to hold the messages that will be sent to the sender.
		ArrayList<String> messages;
		
		// if there is no action specified, then just treat it as if the help action was specified.
		// (which will output general help)
		String targetAction = (args.length > 0) ? args[0] : GiftsCommand.ACTION_HELP;
		
		// try to output help for a specific action.
		if (targetAction.equalsIgnoreCase(GiftsCommand.ACTION_LIST)) 
		{
			messages = this._list(false);
		}
		else if (targetAction.equalsIgnoreCase(GiftsCommand.ACTION_XP_COLLECT)) 
		{
			messages = this._xpCollect(false);
		}
		else 
		{
			// specified action does not have specific help
			// or is unrecognized, output general help.
			messages = this._generalHelp(sender.isOp());
		}
		
		// output a blank line to improve readability and then output the messages.
		sender.sendMessage("");
		for (String message : messages) {
			sender.sendMessage(message);
		}
	}
		
	/**
	 * Returns a list of messages which form the general help.
	 * @return
	 */
	private ArrayList<String> _generalHelp(boolean isOp) 
	{
		ArrayList<String> messages = new ArrayList<String>();
		
		messages.add(ChatColor.AQUA + "LEGEND: " + ChatColor.WHITE + "/gifts action " + 
                this._required("required") +  this._optional("optional"));
        messages.add("------------------------------");
		
		// provide short usage for every action.		
		messages.add(this._list(true).get(0));
		messages.add(this._xpCollect(true).get(0));
		messages.add(this._action(GiftsCommand.ACTION_HELP) + this._optional("action") + this._shortDescription("This page or help on an action"));
	
		return messages;
	}
	
	/**
	 * Returns help for the <code>PoiCommand.LIST</code> action.
	 * 
	 * @param isShort
	 * @return
	 */
	private ArrayList<String> _list(boolean isShort)	
	{
		ArrayList<String> messages = new ArrayList<String>();
		String basic = this._action(null) + this._optional(GiftsCommand.ACTION_LIST);
		if (isShort) {
			basic += this._shortDescription("Use this action to list all Gifts available to you");
			messages.add(basic);
			return messages;
		}

		messages.add(basic);
		messages.add(ChatColor.GREEN + "------------");
		messages.add(ChatColor.YELLOW + "Use this action to list all Gifts available to you.");
		messages.add(ChatColor.YELLOW + "This will show the total number of gifted XP Levels");
		messages.add(ChatColor.YELLOW + "which are available to you. Use " + this._actionXRef(GiftsCommand.ACTION_XP_COLLECT) + " to collect");
		messages.add(ChatColor.YELLOW + "your gifted XP.");
		
		return messages;
	}
	
	/**
     * Returns help for the <code>PoiCommand.LIST</code> action.
     * 
     * @param isShort
     * @return
     */
    private ArrayList<String> _xpCollect(boolean isShort)    
    {
        ArrayList<String> messages = new ArrayList<String>();
        String basic = this._action(GiftsCommand.ACTION_XP_COLLECT) + this._required("amount");
        if (isShort) {
            basic += this._shortDescription("Use this action to collect gifted XP Levels");
            messages.add(basic);
            return messages;
        }

        messages.add(basic);
        messages.add(ChatColor.GREEN + "------------");
        messages.add(ChatColor.YELLOW + "Use this action to collect gifted XP Levels.");
        messages.add(ChatColor.YELLOW + "Use " + this._actionXRef(GiftsCommand.ACTION_LIST) + " to see how many XP Levels you have");
        messages.add(ChatColor.YELLOW + "available.");
        
        return messages;
    }
		
	/**
	 * Formats an action for use as a cross-referenced action in help text.
	 * 
	 * @param action
	 * @return
	 */
	private String _actionXRef(String action) 
	{
		return ChatColor.YELLOW + "\"" + ChatColor.GOLD + "/gifts " + action + ChatColor.YELLOW + "\"";
	}
	
	/**
	 * Formats a token for use as an action in usage example text.
	 * @param token
	 * @return
	 */
	private String _action(String token) 
	{
		return ChatColor.WHITE + "/gifts" + (token != null ? " " + token : "");
	}
	
	/**
	 * Formats a token for use as short description of an action.
	 * 
	 * @param token
	 * @return
	 */
	private String _shortDescription(String token) 
	{
		return ChatColor.GRAY + " (" + token + ")";
	}
	
	/**
	 * Formats a token for use as a required argument of an action.
	 * 
	 * @param token
	 * @return
	 */
	private String _required(String token) 
	{
		return ChatColor.GOLD + " <" + ChatColor.WHITE + token + ChatColor.GOLD + ">";
	}
	
	/**
	 * Formats a token for use as an optional argument of an action.
	 * 
	 * @param token
	 * @return
	 */
	private String _optional(String token) 
	{
		return ChatColor.GREEN + " [" + ChatColor.WHITE + token + ChatColor.GREEN + "]";
	}
}