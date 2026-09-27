package ing.applecraft.mcrealtime.config;

import ing.applecraft.mcrealtime.astronomy.GeoLocation;
import ing.applecraft.mcrealtime.time.TimeMode;
import org.bukkit.configuration.file.FileConfiguration;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Converts Bukkit YAML configuration into validated domain settings. */
public final class SettingsLoader {

    public PluginSettings load(FileConfiguration config) {
        if (config == null) {
            throw new IllegalArgumentException("config must not be null");
        }

        boolean enabled = readEnabled(config);
        TimeMode mode = parseMode(config.getString("mode", "CLOCK"));
        ZoneId zoneId = parseZoneId(config.getString("timezone", "UTC"));

        double latitude = config.getDouble("solar.latitude", 0.0);
        double longitude = config.getDouble("solar.longitude", 0.0);
        GeoLocation location = new GeoLocation(latitude, longitude);

        boolean global = config.getBoolean("global", true);
        Set<String> worlds = sanitizeWorlds(config.getStringList("worlds"));
        long intervalTicks = config.getLong("synchronization.interval-ticks", 20L);
        boolean preventSleep = config.getBoolean("behavior.prevent-sleep", true);
        boolean disableInsomnia = config.getBoolean("behavior.disable-insomnia", true);

        if (!global && worlds.isEmpty()) {
            throw new IllegalArgumentException(
                    "global is false, but no world names are configured in worlds."
            );
        }

        return new PluginSettings(
                enabled,
                mode,
                zoneId,
                location,
                global,
                worlds,
                intervalTicks,
                preventSleep,
                disableInsomnia
        );
    }

    private boolean readEnabled(FileConfiguration config) {
        // Compatibility with MCRealTime 4.x, where this key was named "enable".
        if (config.isSet("enabled")) {
            return config.getBoolean("enabled");
        }
        if (config.isSet("enable")) {
            return config.getBoolean("enable");
        }
        return config.getBoolean("enabled", true);
    }

    private TimeMode parseMode(String raw) {
        String normalized = raw == null ? "CLOCK" : raw.trim().toUpperCase(Locale.ROOT);
        try {
            return TimeMode.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Unknown time mode '" + raw + "'. Supported values: CLOCK, SOLAR."
            );
        }
    }

    private ZoneId parseZoneId(String raw) {
        String value = raw == null ? "UTC" : raw.trim();
        try {
            return ZoneId.of(value);
        } catch (DateTimeException ignored) {
            // MCRealTime 4.x documentation also suggested legacy abbreviations such as PST.
            // java.time intentionally treats those as aliases rather than primary ZoneIds.
            try {
                return ZoneId.of(value, ZoneId.SHORT_IDS);
            } catch (DateTimeException ignoredAgain) {
                // CEST is not part of ZoneId.SHORT_IDS. Preserve old configurations as a
                // fixed +02:00 offset, while new configurations should use Europe/Berlin.
                if ("CEST".equalsIgnoreCase(value)) {
                    return ZoneId.of("+02:00");
                }
                throw new IllegalArgumentException(
                        "Invalid timezone '" + raw + "'. Use an IANA ZoneId such as UTC or Europe/Berlin."
                );
            }
        }
    }

    private Set<String> sanitizeWorlds(List<String> configuredWorlds) {
        Set<String> worlds = new LinkedHashSet<>();
        for (String world : configuredWorlds) {
            if (world == null) {
                continue;
            }
            String trimmed = world.trim();
            if (!trimmed.isEmpty() && !"...".equals(trimmed)) {
                worlds.add(trimmed);
            }
        }
        return worlds;
    }
}
