package ing.applecraft.mcrealtime;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;

import java.util.List;

public class NoEnterBedEvent implements Listener {

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onEnterBed(PlayerBedEnterEvent e) {
        if(MCRealTime.config.getBoolean("enable", true)) {

            if(MCRealTime.config.getBoolean("global", true)) {

                e.setCancelled(true);
            } else {
                List<String> affectedworlds = MCRealTime.config.getStringList("worlds");
                World playerWorld = e.getPlayer().getWorld();

                e.setCancelled(affectedworlds.contains(playerWorld.getName()));
            }
        }
    }
}

