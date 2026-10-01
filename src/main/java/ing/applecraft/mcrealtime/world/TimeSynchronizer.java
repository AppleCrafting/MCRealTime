package ing.applecraft.mcrealtime.world;

import ing.applecraft.mcrealtime.config.PluginSettings;
import ing.applecraft.mcrealtime.time.TimeProvider;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.Bukkit;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;
import java.time.Instant;
import java.util.Objects;

/** Applies one calculated Minecraft time to all configured Bukkit worlds. */
public final class TimeSynchronizer {
    private final JavaPlugin plugin;
    private final PluginSettings settings;
    private final Set<UUID> unsupportedClockWorlds =
            new HashSet<UUID>();
    private final Map<UUID, Long> lastAppliedTimes =
            new HashMap<UUID, Long>();
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

    private void setWorldTimeIfSupported(
            World world,
            long minecraftTime) {

        UUID worldId =
                world.getUID();

        if (unsupportedClockWorlds.contains(
                worldId
        )) {
            return;
        }

        Long lastAppliedTime =
                lastAppliedTimes.get(
                        worldId
                );

        /*
         * Do not force another time synchronization
         * when the calculated Minecraft tick has not
         * actually changed.
         *
         * This avoids unnecessary client time
         * corrections on modern Minecraft versions.
         */
        if (lastAppliedTime != null
                && lastAppliedTime.longValue()
                == minecraftTime) {

            return;
        }

        try {

            world.setTime(
                    minecraftTime
            );

            lastAppliedTimes.put(
                    worldId,
                    minecraftTime
            );

        } catch (IllegalArgumentException exception) {

            if (!isMissingWorldClockException(
                    exception
            )) {
                throw exception;
            }

            unsupportedClockWorlds.add(
                    worldId
            );

            lastAppliedTimes.remove(
                    worldId
            );

            Bukkit.getLogger().info(
                    "[MCRealTime] Skipping time synchronization "
                            + "for world '"
                            + world.getName()
                            + "' because this dimension "
                            + "does not provide a world clock."
            );
        }
    }

    private boolean isMissingWorldClockException(
            IllegalArgumentException exception) {

        return "Cannot set time in world without world clock"
                .equals(
                        exception.getMessage()
                );
    }

    public void synchronizeNow() {
        long minecraftTime = timeProvider.getMinecraftTime(Instant.now());

        for (World world : plugin.getServer().getWorlds()) {
            if (!worldSelector.isAffected(world)) {
                continue;
            }

            worldStateManager.prepare(world);
            setWorldTimeIfSupported(
                    world,
                    minecraftTime
            );
        }
    }
}
