package crussell52.gifts.actions;

import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import crussell52.gifts.GiftManager;

/**
 * Subclasses are responsible for handling specific actions.
 */
public abstract class ActionHandler {
        
    /**
     * Used for logging as necessary throughout this class.
     * 
     * Exception stack traces are still output to the standard error out.
     */
    protected static Logger _log = Logger.getLogger("Minecraft");
    
    /**
     * Handles all of the "heavy lifting" for Gift interactions.
     */
    protected GiftManager _giftManager;
    
    /**
     * Indicates whether the action can be executed from the
     * console.
     */
    protected boolean _fromConsole = false;
    
    /**
     * Indicates whether the action can be executed from
     * an in-game player.
     */
    protected boolean _fromInGame  = true;
    
    /**
     * Indicates the required permission to execute the action.
     * 
     * A value of <code>null</code> indicates no permission needed.
     * 
     * Does not factor into execution of actions from the console.
     */
    protected String _relatedPermission = null;
    
    /**
     * Creates a new instance.
     * 
     * @param giftManager Used for all Gift interactions.
     */
    public ActionHandler(GiftManager giftManager) 
    {
        this._giftManager = giftManager;
    }
    
    /**
     * Performs necessary tasks related to the action.
     * 
     * @param sender Who sent the action
     * @param action the action which was requested by the sender
     * @param args the arguments for the action execution.
     */
    public abstract void handleAction(CommandSender sender, String action, String[] args);
    
    
    /**
     * Ensures that the sender can actually execute the action
     * based on where it was invoked from and the related permission
     * 
     * @param sender
     * @return
     */
    protected boolean _canExecute(CommandSender sender)
    {
        if (sender instanceof Player) {
            if (!this._fromInGame) {
                sender.sendMessage("This action can not be performed from in game.");
                return false;
            }
            
            // cast sender as a player for permission checks.
            Player player = (Player)sender;
        
            // make sure player has necessary permission
            if (this._relatedPermission != null && !player.hasPermission(this._relatedPermission)) {
                sender.sendMessage("You do not have permission to perform this action.");
                return false;
            }
        }
        else if (!this._fromConsole) {
            sender.sendMessage("This action can not be performed from the console.");
            return false;
        }
        
        // all checks passed... okay to execute
        return true;
    }
    
    /**
     * Sends the recipient a multi-line message letting them know that they have used the action
     * incorrectly and provides them with instructions on getting help.
     * 
     * @param recipient
     * @param messages
     * @param action
     */
    protected void _actionUsageError(CommandSender recipient, String message, String action) {
        recipient.sendMessage(ChatColor.RED + message);
        recipient.sendMessage(ChatColor.RED + "Use " + ChatColor.YELLOW + "\"/gifts help\" " + ChatColor.RED + "for guidance."); 
    }
    
    
}

