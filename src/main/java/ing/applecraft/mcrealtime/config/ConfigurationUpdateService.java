package ing.applecraft.mcrealtime.config;

import ing.applecraft.mcrealtime.MCRealTimePlugin;
import ing.applecraft.mcrealtime.location.Place;
import ing.applecraft.mcrealtime.time.TimeMode;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;

public final class ConfigurationUpdateService {

    /*
     * Sentinel object used to distinguish between:
     *
     * - a configuration value that did not exist before
     * - a configuration value that existed
     *
     * This is needed for a proper rollback.
     */
    private static final Object NOT_SET = new Object();

    private final MCRealTimePlugin plugin;

    public ConfigurationUpdateService(MCRealTimePlugin plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException(
                    "plugin must not be null"
            );
        }

        this.plugin = plugin;
    }

    /**
     * Changes the configured time mode.
     *
     * If the new configuration cannot be loaded,
     * the previous configuration is restored.
     */
    public boolean setMode(TimeMode mode) {
        if (mode == null) {
            throw new IllegalArgumentException(
                    "mode must not be null"
            );
        }

        Map<String, Object> previousValues =
                new LinkedHashMap<String, Object>();

        remember(
                previousValues,
                "mode"
        );

        plugin.getConfig().set(
                "mode",
                mode.name()
        );

        return saveAndReload(previousValues);
    }

    /**
     * Stores a selected geographical place.
     *
     * The actual solar calculation only needs:
     *
     * - latitude
     * - longitude
     * - timezone
     *
     * Additional place information is stored as metadata
     * so that the user can see which place was selected.
     */
    public boolean setLocation(Place place) {
        if (place == null) {
            throw new IllegalArgumentException(
                    "place must not be null"
            );
        }

        Map<String, Object> previousValues =
                new LinkedHashMap<String, Object>();

        remember(
                previousValues,
                "timezone"
        );

        remember(
                previousValues,
                "solar.latitude"
        );

        remember(
                previousValues,
                "solar.longitude"
        );

        remember(
                previousValues,
                "solar.place.geoname-id"
        );

        remember(
                previousValues,
                "solar.place.name"
        );

        remember(
                previousValues,
                "solar.place.country-code"
        );

        FileConfiguration config =
                plugin.getConfig();

        config.set(
                "timezone",
                place.getZoneId().getId()
        );

        config.set(
                "solar.latitude",
                place.getCoordinates().getLatitude()
        );

        config.set(
                "solar.longitude",
                place.getCoordinates().getLongitude()
        );

        config.set(
                "solar.place.geoname-id",
                place.getGeonameId()
        );

        config.set(
                "solar.place.name",
                place.getName()
        );

        config.set(
                "solar.place.country-code",
                place.getCountryCode()
        );

        return saveAndReload(previousValues);
    }

    /**
     * Remembers a configuration value before changing it.
     */
    private void remember(
            Map<String, Object> previousValues,
            String path) {

        FileConfiguration config =
                plugin.getConfig();

        if (config.isSet(path)) {
            previousValues.put(
                    path,
                    config.get(path)
            );
        } else {
            previousValues.put(
                    path,
                    NOT_SET
            );
        }
    }

    /**
     * Saves the configuration and replaces the running
     * MCRealTime runtime.
     *
     * If something fails, the previous configuration
     * values are restored.
     */
    private boolean saveAndReload(
            Map<String, Object> previousValues) {

        try {
            plugin.saveConfig();

            if (plugin.reloadRuntime()) {
                return true;
            }
        } catch (RuntimeException exception) {
            plugin.getLogger().log(
                    Level.SEVERE,
                    "Could not save or reload "
                            + "the updated configuration.",
                    exception
            );
        }

        plugin.getLogger().warning(
                "Configuration update failed. "
                        + "Restoring previous values."
        );

        restore(previousValues);

        try {
            plugin.saveConfig();

            if (!plugin.reloadRuntime()) {
                plugin.getLogger().severe(
                        "Configuration rollback failed."
                );
            }
        } catch (RuntimeException exception) {
            plugin.getLogger().log(
                    Level.SEVERE,
                    "Could not restore "
                            + "the previous configuration.",
                    exception
            );
        }

        return false;
    }

    /**
     * Restores the values remembered before an update.
     */
    private void restore(
            Map<String, Object> previousValues) {

        FileConfiguration config =
                plugin.getConfig();

        for (Map.Entry<String, Object> entry
                : previousValues.entrySet()) {

            Object previousValue =
                    entry.getValue();

            if (previousValue == NOT_SET) {
                config.set(
                        entry.getKey(),
                        null
                );
            } else {
                config.set(
                        entry.getKey(),
                        previousValue
                );
            }
        }
    }
}