package ing.applecraft.mcrealtime.listener;

import ing.applecraft.mcrealtime.MCRealTimePlugin;
import ing.applecraft.mcrealtime.PluginRuntime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldUnloadEvent;

/** Restores temporarily owned gamerules before a controlled world is unloaded. */
public final class WorldUnloadListener implements Listener {
    private final MCRealTimePlugin plugin;

    public WorldUnloadListener(MCRealTimePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event) {
        PluginRuntime runtime = plugin.getRuntime();
        if (runtime != null) {
            runtime.onWorldUnload(event.getWorld());
        }
    }
}
