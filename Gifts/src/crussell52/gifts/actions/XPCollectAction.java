package crussell52.gifts.actions;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import crussell52.gifts.GiftManager;


public class XPCollectAction extends ActionHandler {
    
    /**
     * {@inheritDoc}
     */
    public XPCollectAction(GiftManager giftManager) 
    {
        super(giftManager);
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public void handleAction(CommandSender sender, String action, String[] args) 
    {
        // make this command can be executed by the sender
        if (!this._canExecute(sender)) {
            return;
        }
        
        if (args.length == 0)
        {
            this._actionUsageError(sender, "You must specify how much XP to collect.", action);
            return;
        }

        // Only one argument is supported.
        if (args.length > 1) 
        {
            this._actionUsageError(sender, "XP Amount is the only argument for the " + action + " action.", action);
            return;
        }
        
        int amount;
        
        try 
        {
            amount = Integer.parseInt(args[0]);
        }
        catch (Exception ex)
        {
            this._actionUsageError(sender, "XP Amount should be a number.", action);
            return;
        }
        
        try 
        {
            Player player = (Player)sender;
            if (this._giftManager.collectXP(sender.getName(), amount))
            {
                player.setLevel(player.getLevel() + amount);
                sender.sendMessage(ChatColor.GREEN + "Congrats! You have been given " + String.valueOf(amount) + " Levels of XP!");
            }
            else
            {
                sender.sendMessage(ChatColor.RED + "I'm sorry, you don't appear to have that much available.");
            }
            
        }
        catch (Exception ex) 
        {
            _log.severe("There was an unexpected error while trying to collect XP.");
            ex.printStackTrace();
            sender.sendMessage(ChatColor.RED + "I'm sorry there was a problem collecting XP, right now.");
            sender.sendMessage("If this problem continues, please notify the admin.");
        }
    }

}
