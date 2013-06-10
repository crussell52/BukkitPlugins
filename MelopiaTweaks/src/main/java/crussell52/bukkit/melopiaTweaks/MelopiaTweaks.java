package crussell52.bukkit.melopiaTweaks;

import crussell52.bukkit.melopiaTweaks.deathXP.DeathXPListener;
import crussell52.bukkit.melopiaTweaks.status.StatusManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class MelopiaTweaks extends JavaPlugin implements Listener
{

    /**
     * @{inheritDoc}
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if(cmd.getName().equalsIgnoreCase("wtime")){
            // Don't allow the console to use this command!
            if (!(sender instanceof Player)) {
                sender.sendMessage("This command can not originate from the console.");
                return true;
            }

            Player player = (Player)sender;

            // Make sure the player has permissions for this command.
            if (!player.hasPermission("crussell52.commands.wtime")) {
                sender.sendMessage(ChatColor.YELLOW + "You can not adjust the time of this world.");
                return true;
            }


            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Incorrect usage. Correct usage:");
                sender.sendMessage(ChatColor.RED + "/wtime set <value>");
                sender.sendMessage(ChatColor.RED + "/wtime add <value>");
                return true;
            }

            Integer timeValue;
            if (args[0].equalsIgnoreCase("add")) {
                try {
                    timeValue = Integer.parseInt(args[1]);
                }
                catch (Exception e) {
                    timeValue = 0;
                }

                player.getWorld().setFullTime(player.getWorld().getFullTime() + timeValue);
                sender.sendMessage("Added " + timeValue.toString() + " to time");
            } else if (args[0].equalsIgnoreCase("set")) {
                try {
                    timeValue = Integer.parseInt(args[1]);
                }
                catch (Exception e) {
                    timeValue = args[1].equalsIgnoreCase("night") ? 12500 : 0;
                }

                player.getWorld().setFullTime(timeValue);
                sender.sendMessage("Set time to " + timeValue.toString());
            } else {
                sender.sendMessage(ChatColor.RED + "Incorrect usage. Correct usage:");
                sender.sendMessage(ChatColor.RED + "/wtime set <value>");
                sender.sendMessage(ChatColor.RED + "/wtime add <value>" + ChatColor.WHITE);
            }

            return true;
        }

        // Unhandled command.
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onEnable()
    {
        this.getServer().getPluginManager().registerEvents(new DeathXPListener(), this);

        // 20 ticks per second, run every 15 seconds.
        new StatusManager(this).runTaskTimer(this, 0, 15 * 20);
    }


}