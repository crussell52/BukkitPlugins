package crussell52.gifts.actions;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import crussell52.gifts.GiftManager;


public class ListAction extends ActionHandler {
    
    /**
     * {@inheritDoc}
     */
    public ListAction(GiftManager giftManager) 
    {
        super(giftManager);
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public void handleAction(CommandSender sender, String action, String[] args) {
        // make this command can be executed by the sender
        if (!this._canExecute(sender)) {
            return;
        }

        // No arguments are supported.
        if (args.length > 0) {
            this._actionUsageError(sender, "No arguments are expected for the " + action + " action.", action);
            return;
        }
        
        try 
        {
            int availableXP = this._giftManager.getAvailXP(sender.getName());
            sender.sendMessage(ChatColor.GREEN + "You have " +  availableXP + " Levels of gifted XP available.");
        }
        catch (Exception ex) 
        {
            _log.severe("There was an unexpected error while trying to list available gifts.");
            ex.printStackTrace();
            sender.sendMessage(ChatColor.RED + "I'm sorry there was a problem looking that up, right now.");
            sender.sendMessage("If this problem continues, please notify the admin.");
        }
    }

}
