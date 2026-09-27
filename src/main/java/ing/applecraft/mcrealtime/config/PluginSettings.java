package ing.applecraft.mcrealtime.config;

import ing.applecraft.mcrealtime.astronomy.GeoLocation;
import ing.applecraft.mcrealtime.time.TimeMode;

import java.time.ZoneId;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/** Immutable validated runtime configuration. */
public final class PluginSettings {
    private final boolean enabled;
    private final TimeMode mode;
    private final ZoneId zoneId;
    private final GeoLocation solarLocation;
    private final boolean global;
    private final Set<String> worlds;
    private final long synchronizationIntervalTicks;
    private final boolean preventSleep;
    private final boolean disableInsomnia;

    public PluginSettings(boolean enabled,
                          TimeMode mode,
                          ZoneId zoneId,
                          GeoLocation solarLocation,
                          boolean global,
                          Set<String> worlds,
                          long synchronizationIntervalTicks,
                          boolean preventSleep,
                          boolean disableInsomnia) {
        if (mode == null) {
            throw new IllegalArgumentException("mode must not be null");
        }
        if (zoneId == null) {
            throw new IllegalArgumentException("zoneId must not be null");
        }
        if (solarLocation == null) {
            throw new IllegalArgumentException("solarLocation must not be null");
        }
        if (synchronizationIntervalTicks < 1L) {
            throw new IllegalArgumentException("synchronization.interval-ticks must be at least 1.");
        }

        this.enabled = enabled;
        this.mode = mode;
        this.zoneId = zoneId;
        this.solarLocation = solarLocation;
        this.global = global;
        this.worlds = Collections.unmodifiableSet(new LinkedHashSet<String>(worlds));
        this.synchronizationIntervalTicks = synchronizationIntervalTicks;
        this.preventSleep = preventSleep;
        this.disableInsomnia = disableInsomnia;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public TimeMode getMode() {
        return mode;
    }

    public ZoneId getZoneId() {
        return zoneId;
    }

    public GeoLocation getSolarLocation() {
        return solarLocation;
    }

    public boolean isGlobal() {
        return global;
    }

    public Set<String> getWorlds() {
        return worlds;
    }

    public long getSynchronizationIntervalTicks() {
        return synchronizationIntervalTicks;
    }

    public boolean isPreventSleep() {
        return preventSleep;
    }

    public boolean isDisableInsomnia() {
        return disableInsomnia;
    }
}
