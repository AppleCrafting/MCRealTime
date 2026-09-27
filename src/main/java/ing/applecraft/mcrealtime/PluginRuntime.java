package ing.applecraft.mcrealtime;

import ing.applecraft.mcrealtime.config.PluginSettings;
import ing.applecraft.mcrealtime.time.TimeProvider;
import ing.applecraft.mcrealtime.time.TimeProviderFactory;
import ing.applecraft.mcrealtime.world.TimeSynchronizer;
import ing.applecraft.mcrealtime.world.WorldSelector;
import ing.applecraft.mcrealtime.world.WorldStateManager;
import org.bukkit.World;

import java.time.Instant;

/** One immutable configuration generation of the running plugin. */
public final class PluginRuntime {
    private final MCRealTimePlugin plugin;
    private final PluginSettings settings;
    private final TimeProvider timeProvider;
    private final WorldSelector worldSelector;
    private final WorldStateManager worldStateManager;
    private final TimeSynchronizer synchronizer;

    public PluginRuntime(MCRealTimePlugin plugin, PluginSettings settings) {
        this.plugin = plugin;
        this.settings = settings;
        this.timeProvider = TimeProviderFactory.create(settings);
        this.worldSelector = new WorldSelector(settings);
        this.worldStateManager = new WorldStateManager(settings);
        this.synchronizer = new TimeSynchronizer(
                plugin,
                settings,
                timeProvider,
                worldSelector,
                worldStateManager
        );
    }

    public void start() {
        synchronizer.start();
    }

    public void stop() {
        synchronizer.stop();
        worldStateManager.restoreAll(plugin.getServer().getWorlds());
    }

    public PluginSettings getSettings() {
        return settings;
    }

    public long getCurrentMinecraftTime() {
        return timeProvider.getMinecraftTime(Instant.now());
    }

    public boolean isAffected(World world) {
        return settings.isEnabled() && worldSelector.isAffected(world);
    }

    public void onWorldUnload(World world) {
        worldStateManager.restore(world);
    }
}
