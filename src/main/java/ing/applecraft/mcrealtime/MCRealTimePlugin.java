package ing.applecraft.mcrealtime;

import ing.applecraft.mcrealtime.command.MCRealTimeCommand;
import ing.applecraft.mcrealtime.config.ConfigMigrationService;
import ing.applecraft.mcrealtime.config.PluginSettings;
import ing.applecraft.mcrealtime.config.SettingsLoader;
import ing.applecraft.mcrealtime.listener.SleepListener;
import ing.applecraft.mcrealtime.listener.WorldUnloadListener;
import ing.applecraft.mcrealtime.location.GeoNamesPlaceLoader;
import ing.applecraft.mcrealtime.location.GeoNamesPlaceParser;
import ing.applecraft.mcrealtime.location.GeoNamesPlaceRepository;
import ing.applecraft.mcrealtime.location.LocationSearchService;
import ing.applecraft.mcrealtime.location.Place;
import ing.applecraft.mcrealtime.update.*;
import ing.applecraft.mcrealtime.update.PluginFileNamePolicy;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;

/**
 * Bukkit entry point.
 *
 * Domain logic lives outside this class.
 */
public final class MCRealTimePlugin extends JavaPlugin {

    private final SettingsLoader settingsLoader =
            new SettingsLoader();

    private PluginRuntime runtime;

    private AsyncUpdateManager updateManager;

    private LocationSearchService
            locationSearchService;

    @Override
    public void onEnable() {

        saveDefaultConfig();

        ConfigMigrationService migrationService =
                new ConfigMigrationService(this);

        try {
            migrationService.migrateIfNeeded();
        } catch (IOException exception) {

            getLogger().log(
                    Level.SEVERE,
                    "Could not migrate the MCRealTime configuration.",
                    exception
            );

            getServer()
                    .getPluginManager()
                    .disablePlugin(this);

            return;
        }

        checkPluginFileName();

        /*
         * Load and index GeoNames exactly once.
         *
         * Normal tab completion later only searches
         * the in-memory index.
         */
        try {

            locationSearchService =
                    loadLocationSearchService();

        } catch (IOException exception) {

            getLogger().log(
                    Level.SEVERE,
                    "Could not load the bundled "
                            + "GeoNames location database.",
                    exception
            );

            getServer()
                    .getPluginManager()
                    .disablePlugin(this);

            return;

        } catch (RuntimeException exception) {

            getLogger().log(
                    Level.SEVERE,
                    "Could not initialize "
                            + "the location database.",
                    exception
            );

            getServer()
                    .getPluginManager()
                    .disablePlugin(this);

            return;
        }

        registerCommands();
        registerListeners();

        if (!replaceRuntime(false)) {

            getLogger().severe(
                    "MCRealTime could not start "
                            + "because the configuration is invalid."
            );

            getServer()
                    .getPluginManager()
                    .disablePlugin(this);

            return;
        }

        updateManager =
                new AsyncUpdateManager(
                        this,
                        new UpdateService(
                                new GitHubReleaseClient(),
                                new UpdateChecker()
                        ),
                        new UpdateDownloader(
                                new HttpDownloadTransport()
                        ),
                        new UpdaterSettingsLoader()
                );

        updateManager.checkOnStart();

        getLogger().info(
                "MCRealTime "
                        + getDescription().getVersion()
                        + " enabled."
        );
    }

    @Override
    public void onDisable() {

        PluginRuntime current =
                runtime;

        runtime =
                null;

        if (current != null) {
            current.stop();
        }

        getLogger().info(
                "MCRealTime disabled; "
                        + "owned world state restored."
        );
    }

    public PluginRuntime getRuntime() {

        return runtime;
    }

    private void checkPluginFileName() {

        PluginFileNamePolicy fileNamePolicy =
                new PluginFileNamePolicy();

        if (fileNamePolicy.isCanonical(
                getFile()
        )) {
            return;
        }

        getLogger().warning(
                "MCRealTime is installed as '"
                        + getFile().getName()
                        + "'. Please rename the plugin JAR to '"
                        + fileNamePolicy.getCanonicalFileName()
                        + "' while the server is stopped. "
                        + "The updater will continue to work "
                        + "with the current file name until then."
        );
    }

    public AsyncUpdateManager getUpdateManager() {
        return updateManager;
    }

    /**
     * Reloads config.yml and validates it.
     *
     * The running runtime is only replaced when
     * the new configuration is valid.
     */
    public boolean reloadRuntime() {

        reloadConfig();

        return replaceRuntime(true);
    }

    /**
     * Loads the embedded GeoNames cities database
     * and creates the searchable in-memory repository.
     */
    private LocationSearchService
    loadLocationSearchService()
            throws IOException {

        InputStream inputStream =
                getResource(
                        "geonames/cities5000.zip"
                );

        if (inputStream == null) {

            throw new IOException(
                    "Bundled GeoNames database "
                            + "geonames/cities5000.zip "
                            + "is missing."
            );
        }

        try {

            GeoNamesPlaceLoader loader =
                    new GeoNamesPlaceLoader(
                            new GeoNamesPlaceParser()
                    );

            List<Place> places =
                    loader.load(
                            inputStream
                    );

            getLogger().info(
                    "Loaded "
                            + places.size()
                            + " GeoNames locations."
            );

            GeoNamesPlaceRepository repository =
                    new GeoNamesPlaceRepository(
                            places
                    );

            return new LocationSearchService(
                    repository
            );

        } finally {

            try {
                inputStream.close();
            } catch (IOException exception) {

                getLogger().log(
                        Level.WARNING,
                        "Could not close "
                                + "the GeoNames resource stream.",
                        exception
                );
            }
        }
    }

    /**
     * Creates a new runtime from the current configuration.
     *
     * If requested, the previous runtime is restored
     * when the new one cannot be started.
     */
    private boolean replaceRuntime(
            boolean preservePreviousOnFailure) {

        final PluginSettings settings;
        final PluginRuntime candidate;

        try {

            settings =
                    settingsLoader.load(
                            getConfig()
                    );

            candidate =
                    new PluginRuntime(
                            this,
                            settings
                    );

        } catch (RuntimeException exception) {

            getLogger().log(
                    Level.SEVERE,
                    "Invalid MCRealTime configuration: "
                            + exception.getMessage(),
                    exception
            );

            return false;
        }

        PluginRuntime previous =
                runtime;

        if (previous != null) {
            previous.stop();
        }

        try {

            candidate.start();

            runtime =
                    candidate;

            logRuntime(settings);

            return true;

        } catch (RuntimeException exception) {

            getLogger().log(
                    Level.SEVERE,
                    "Could not start "
                            + "the new MCRealTime runtime.",
                    exception
            );

            if (preservePreviousOnFailure
                    && previous != null) {

                try {

                    previous.start();

                    runtime =
                            previous;

                    getLogger().warning(
                            "Previous MCRealTime runtime "
                                    + "was restored."
                    );

                } catch (RuntimeException restoreFailure) {

                    runtime =
                            null;

                    getLogger().log(
                            Level.SEVERE,
                            "Previous runtime "
                                    + "could not be restored.",
                            restoreFailure
                    );
                }
            }

            return false;
        }
    }

    private void registerCommands() {

        PluginCommand command =
                getCommand(
                        "mcrealtime"
                );

        if (command == null) {

            throw new IllegalStateException(
                    "Command 'mcrealtime' "
                            + "is missing from plugin.yml"
            );
        }

        MCRealTimeCommand handler =
                new MCRealTimeCommand(
                        this,
                        locationSearchService
                );

        command.setExecutor(
                handler
        );

        command.setTabCompleter(
                handler
        );
    }

    private void registerListeners() {

        getServer()
                .getPluginManager()
                .registerEvents(
                        new SleepListener(this),
                        this
                );

        getServer()
                .getPluginManager()
                .registerEvents(
                        new WorldUnloadListener(this),
                        this
                );
    }

    public String getPluginFileName() {

        return getFile().getName();
    }

    private void logRuntime(
            PluginSettings settings) {

        if (!settings.isEnabled()) {

            getLogger().info(
                    "Synchronization is disabled "
                            + "by configuration."
            );

            return;
        }

        String scope =
                settings.isGlobal()
                        ? "all loaded worlds"
                        : settings
                        .getWorlds()
                        .toString();

        getLogger().info(
                "Mode="
                        + settings.getMode()
                        + ", timezone="
                        + settings
                        .getZoneId()
                        .getId()
                        + ", scope="
                        + scope
                        + ", interval="
                        + settings
                        .getSynchronizationIntervalTicks()
                        + " ticks"
        );
    }
}