package crussell52.bukkit.melopiaTweaks.status;

import crussell52.bukkit.common.CustomConfig;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class StatusManager extends BukkitRunnable implements Listener {

    private Plugin _plugin;
    private CustomConfig _data;

    public StatusManager(Plugin plugin) {
        _plugin = plugin;
        _data = new CustomConfig(plugin, "status.yml");

        OfflinePlayer[] offlinePlayers = _plugin.getServer().getOfflinePlayers();
        _data.getConfig().createSection("players");
        Map<String, Object> playerData;

        for (OfflinePlayer offlinePlayer : offlinePlayers) {
            _recordPlayer(offlinePlayer, false);
        }

        _data.saveConfig();
    }

    @Override
    public void run() {
        File file = new File(_plugin.getDataFolder(), "status.yml");
        if (!file.setLastModified(System.currentTimeMillis())) {
            _plugin.getLogger().log(Level.SEVERE, "Unable to update modified-time of status data file.");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLogin(PlayerLoginEvent event) {
        _recordPlayer(event.getPlayer(), true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        _recordPlayer(event.getPlayer(), true);
    }

    private void _recordPlayer(OfflinePlayer offlinePlayer, boolean doSave) {
        HashMap<String, Object> playerData = new HashMap<String, Object>();
        playerData.put("lastPlayed", offlinePlayer.getLastPlayed() / 1000L);
        playerData.put("isOnline", offlinePlayer.isOnline());
        playerData.put("name", offlinePlayer.getName());
        playerData.put("world", offlinePlayer.isOnline() ? offlinePlayer.getPlayer().getWorld().getName() : null);
        _data.getConfig().getConfigurationSection("players").set(offlinePlayer.getName(), playerData);

        if (doSave) {
            _data.saveConfig();
        }
    }
}
