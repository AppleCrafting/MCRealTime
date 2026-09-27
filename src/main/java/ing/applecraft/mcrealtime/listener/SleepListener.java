package ing.applecraft.mcrealtime.listener;

import ing.applecraft.mcrealtime.MCRealTimePlugin;
import ing.applecraft.mcrealtime.PluginRuntime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;

/** Prevents vanilla sleeping only when configured for an affected world. */
public final class SleepListener implements Listener {
    private final MCRealTimePlugin plugin;

    public SleepListener(MCRealTimePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        PluginRuntime runtime = plugin.getRuntime();
        if (runtime == null || !runtime.getSettings().isPreventSleep()) {
            return;
        }

        if (runtime.isAffected(event.getPlayer().getWorld())) {
            event.setCancelled(true);
        }
    }
}
