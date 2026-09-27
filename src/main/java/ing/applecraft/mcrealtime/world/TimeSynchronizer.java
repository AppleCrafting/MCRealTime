package ing.applecraft.mcrealtime.world;

import ing.applecraft.mcrealtime.config.PluginSettings;
import ing.applecraft.mcrealtime.time.TimeProvider;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.time.Instant;
import java.util.Objects;

/** Applies one calculated Minecraft time to all configured Bukkit worlds. */
public final class TimeSynchronizer {
    private final JavaPlugin plugin;
    private final PluginSettings settings;
    private final TimeProvider timeProvider;
    private final WorldSelector worldSelector;
    private final WorldStateManager worldStateManager;

    private BukkitTask task;

    public TimeSynchronizer(JavaPlugin plugin,
                            PluginSettings settings,
                            TimeProvider timeProvider,
                            WorldSelector worldSelector,
                            WorldStateManager worldStateManager) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.settings = Objects.requireNonNull(settings, "settings");
        this.timeProvider = Objects.requireNonNull(timeProvider, "timeProvider");
        this.worldSelector = Objects.requireNonNull(worldSelector, "worldSelector");
        this.worldStateManager = Objects.requireNonNull(worldStateManager, "worldStateManager");
    }

    public void start() {
        if (!settings.isEnabled() || task != null) {
            return;
        }

        long interval = settings.getSynchronizationIntervalTicks();
        task = plugin.getServer().getScheduler().runTaskTimer(
                plugin,
                new Runnable() {
                    @Override
                    public void run() {
                        synchronizeNow();
                    }
                },
                0L,
                interval
        );
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    public void synchronizeNow() {
        long minecraftTime = timeProvider.getMinecraftTime(Instant.now());

        for (World world : plugin.getServer().getWorlds()) {
            if (!worldSelector.isAffected(world)) {
                continue;
            }

            worldStateManager.prepare(world);
            world.setTime(minecraftTime);
        }
    }
}
