package ing.applecraft.mcrealtime.config;

import ing.applecraft.mcrealtime.MCRealTimePlugin;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ConfigMigrationService {

    public static final int CURRENT_CONFIG_VERSION = 2;

    /*
     * Configuration paths that still exist in the
     * current configuration schema.
     *
     * Only values that were actually present in the old
     * config.yml are preserved.
     */
    private static final List<String> CURRENT_PATHS =
            Arrays.asList(
                    "enabled",
                    "mode",
                    "timezone",

                    "solar.latitude",
                    "solar.longitude",
                    "solar.place.geoname-id",
                    "solar.place.name",
                    "solar.place.country-code",

                    "global",
                    "worlds",

                    "synchronization.interval-ticks",

                    "behavior.prevent-sleep",
                    "behavior.disable-insomnia"
            );

    private final MCRealTimePlugin plugin;

    public ConfigMigrationService(
            MCRealTimePlugin plugin) {

        if (plugin == null) {
            throw new IllegalArgumentException(
                    "plugin must not be null"
            );
        }

        this.plugin = plugin;
    }

    /**
     * Migrates an older config.yml to the current schema.
     *
     * Important:
     *
     * The existing config.yml is loaded directly from disk.
     * We deliberately do not use plugin.getConfig() here,
     * because Bukkit may expose values from the default
     * configuration bundled inside the JAR.
     */
    public void migrateIfNeeded()
            throws IOException {

        File configFile =
                new File(
                        plugin.getDataFolder(),
                        "config.yml"
                );

        if (!configFile.isFile()) {
            return;
        }

        YamlConfiguration oldConfig =
                loadRawConfiguration(
                        configFile
                );

        /*
         * A configuration without config-version is treated
         * as schema version 1.
         *
         * Because oldConfig was loaded directly from disk,
         * no bundled defaults can influence this check.
         */
        int oldVersion;

        if (oldConfig.contains("config-version")) {
            oldVersion =
                    oldConfig.getInt(
                            "config-version"
                    );
        } else {
            oldVersion = 1;
        }

        if (oldVersion == CURRENT_CONFIG_VERSION) {
            return;
        }

        if (oldVersion > CURRENT_CONFIG_VERSION) {

            plugin.getLogger().warning(
                    "Configuration version "
                            + oldVersion
                            + " is newer than the supported "
                            + "version "
                            + CURRENT_CONFIG_VERSION
                            + ". No migration will be performed."
            );

            return;
        }

        plugin.getLogger().info(
                "Migrating configuration from version "
                        + oldVersion
                        + " to version "
                        + CURRENT_CONFIG_VERSION
                        + "..."
        );

        Map<String, Object> preservedValues =
                collectPreservedValues(
                        oldConfig
                );

        /*
         * Known schema-1 rename:
         *
         * enable -> enabled
         *
         * If the newer key already exists, it takes priority.
         */
        if (!preservedValues.containsKey("enabled")
                && oldConfig.contains("enable")) {

            preservedValues.put(
                    "enabled",
                    oldConfig.get("enable")
            );
        }

        /*
         * The complete original file is archived BEFORE
         * config.yml is modified in any way.
         */
        File archive =
                createLegacyArchive(
                        configFile,
                        oldVersion
                );

        plugin.getLogger().info(
                "Legacy configuration archived at: "
                        + archive.getAbsolutePath()
        );

        try {

            /*
             * Replace config.yml with the current configuration
             * template bundled inside the plugin JAR.
             */
            plugin.saveResource(
                    "config.yml",
                    true
            );

            /*
             * Refresh Bukkit's cached configuration after the
             * physical file was replaced.
             */
            plugin.reloadConfig();

            FileConfiguration newConfig =
                    plugin.getConfig();

            /*
             * Restore user values that are still meaningful
             * in the current schema.
             */
            for (Map.Entry<String, Object> entry
                    : preservedValues.entrySet()) {

                newConfig.set(
                        entry.getKey(),
                        entry.getValue()
                );
            }

            newConfig.set(
                    "config-version",
                    CURRENT_CONFIG_VERSION
            );

            plugin.saveConfig();

            plugin.getLogger().info(
                    "Configuration migration completed "
                            + "successfully."
            );

        } catch (RuntimeException exception) {

            /*
             * If anything fails after the original file was
             * archived, restore that exact file.
             */
            restoreArchive(
                    archive,
                    configFile
            );

            throw new IOException(
                    "Configuration migration failed. "
                            + "The original configuration "
                            + "was restored.",
                    exception
            );
        }
    }

    /**
     * Loads only the contents physically stored in config.yml.
     *
     * No Bukkit/JAR defaults are attached to this object.
     */
    private YamlConfiguration loadRawConfiguration(
            File configFile)
            throws IOException {

        YamlConfiguration configuration =
                new YamlConfiguration();

        try {

            configuration.load(
                    configFile
            );

        } catch (InvalidConfigurationException exception) {

            throw new IOException(
                    "The existing config.yml is not valid YAML.",
                    exception
            );
        }

        return configuration;
    }

    /**
     * Collects values explicitly stored in the old config.
     */
    private Map<String, Object> collectPreservedValues(
            YamlConfiguration config) {

        Map<String, Object> values =
                new LinkedHashMap<String, Object>();

        for (String path : CURRENT_PATHS) {

            if (config.contains(path)) {

                values.put(
                        path,
                        config.get(path)
                );
            }
        }

        return values;
    }

    /**
     * Creates an untouched archive of the old config.yml.
     */
    private File createLegacyArchive(
            File configFile,
            int configVersion)
            throws IOException {

        File dataFolder =
                plugin.getDataFolder();

        File archive =
                findAvailableArchiveFile(
                        dataFolder,
                        configVersion
                );

        Files.copy(
                configFile.toPath(),
                archive.toPath()
        );

        /*
         * Extra safety check:
         * do not continue unless the archive really exists.
         */
        if (!archive.isFile()) {

            throw new IOException(
                    "Legacy configuration archive "
                            + "was not created successfully."
            );
        }

        return archive;
    }

    /**
     * Never overwrites an earlier legacy archive.
     */
    private File findAvailableArchiveFile(
            File dataFolder,
            int configVersion) {

        String baseName =
                "config-legacy-v"
                        + configVersion;

        File candidate =
                new File(
                        dataFolder,
                        baseName + ".yml"
                );

        if (!candidate.exists()) {
            return candidate;
        }

        int number = 1;

        while (true) {

            candidate =
                    new File(
                            dataFolder,
                            baseName
                                    + "-"
                                    + number
                                    + ".yml"
                    );

            if (!candidate.exists()) {
                return candidate;
            }

            number++;
        }
    }

    /**
     * Restores the exact archived configuration if
     * migration fails after replacing config.yml.
     */
    private void restoreArchive(
            File archive,
            File configFile)
            throws IOException {

        Files.copy(
                archive.toPath(),
                configFile.toPath(),
                StandardCopyOption.REPLACE_EXISTING
        );

        plugin.reloadConfig();
    }
}
