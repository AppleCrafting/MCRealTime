package ing.applecraft.mcrealtime;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import static org.bukkit.ChatColor.*;

public class UpdateNotifyJoinEvent implements Listener{

    public static boolean autoupdate;
    public static boolean update;

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        if(MCRealTime.config.getBoolean("auto_update", true)){
            autoupdate = MCRealTime.autoupdater.getResult() == Updater.UpdateResult.UPDATE_AVAILABLE;
            if(p.hasPermission(MCRealTime.config.getString("Permissions.admin")) && autoupdate){
                p.sendMessage("__________________________________________________");
                p.sendMessage(MCRealTime.prefix + " " + GREEN + "by " + MCRealTime.authors);
                p.sendMessage(GOLD + "The Update " + MCRealTime.name + ", Type: " + MCRealTime.type + " for " + MCRealTime.version + "(+) was updated successfully!");
                p.sendMessage(RED + "Please restart your minecraft server!");
                p.sendMessage("__________________________________________________");
            }
        }else{
            update = MCRealTime.updater.getResult() == Updater.UpdateResult.UPDATE_AVAILABLE;
            if(p.hasPermission(MCRealTime.config.getString("Permissions.admin")) && update){
                p.sendMessage("__________________________________________________");
                p.sendMessage(MCRealTime.prefix + " " + GREEN + "by " + MCRealTime.authors);
                p.sendMessage(GOLD + "An update is available: " + MCRealTime.name + ", Type: " + MCRealTime.type + " for " + MCRealTime.version + "(+)" + " available at " + MCRealTime.link);
                p.sendMessage(GOLD + "Type /mcrealtime update if you would like to automatically update.");
                p.sendMessage(RED + "Before you run the command, please open your server console.");
                p.sendMessage(RED + "After the update finish, please restart your minecraft server!");
                p.sendMessage("__________________________________________________");
            }
        }
    }
}
