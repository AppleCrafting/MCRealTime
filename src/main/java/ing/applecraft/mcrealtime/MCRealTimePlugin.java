package ing.applecraft.mcrealtime;

import ing.applecraft.mcrealtime.command.MCRealTimeCommand;
import ing.applecraft.mcrealtime.config.PluginSettings;
import ing.applecraft.mcrealtime.config.SettingsLoader;
import ing.applecraft.mcrealtime.listener.SleepListener;
import ing.applecraft.mcrealtime.listener.WorldUnloadListener;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

/** Bukkit entry point. Domain logic lives outside this class. */
public final class MCRealTimePlugin extends JavaPlugin {
    private final SettingsLoader settingsLoader = new SettingsLoader();
    private PluginRuntime runtime;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        registerCommands();
        registerListeners();

        if (!replaceRuntime(false)) {
            getLogger().severe("MCRealTime could not start because the configuration is invalid.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getLogger().info("MCRealTime " + getDescription().getVersion() + " enabled.");
    }

    @Override
    public void onDisable() {
        PluginRuntime current = runtime;
        runtime = null;
        if (current != null) {
            current.stop();
        }
        getLogger().info("MCRealTime disabled; owned world state restored.");
    }

    public PluginRuntime getRuntime() {
        return runtime;
    }

    /**
     * Reloads YAML, validates it first, and only replaces the running instance
     * if the new configuration can be constructed successfully.
     */
    public boolean reloadRuntime() {
        reloadConfig();
        return replaceRuntime(true);
    }

    private boolean replaceRuntime(boolean preservePreviousOnFailure) {
        final PluginSettings settings;
        final PluginRuntime candidate;

        try {
            settings = settingsLoader.load(getConfig());
            candidate = new PluginRuntime(this, settings);
        } catch (RuntimeException ex) {
            getLogger().log(Level.SEVERE, "Invalid MCRealTime configuration: " + ex.getMessage(), ex);
            return false;
        }

        PluginRuntime previous = runtime;
        if (previous != null) {
            previous.stop();
        }

        try {
            candidate.start();
            runtime = candidate;
            logRuntime(settings);
            return true;
        } catch (RuntimeException ex) {
            getLogger().log(Level.SEVERE, "Could not start the new MCRealTime runtime.", ex);

            if (preservePreviousOnFailure && previous != null) {
                try {
                    previous.start();
                    runtime = previous;
                    getLogger().warning("Previous MCRealTime runtime was restored.");
                } catch (RuntimeException restoreFailure) {
                    runtime = null;
                    getLogger().log(Level.SEVERE, "Previous runtime could not be restored.", restoreFailure);
                }
            }
            return false;
        }
    }

    private void registerCommands() {
        PluginCommand command = getCommand("mcrealtime");
        if (command == null) {
            throw new IllegalStateException("Command 'mcrealtime' is missing from plugin.yml");
        }

        MCRealTimeCommand handler = new MCRealTimeCommand(this);
        command.setExecutor(handler);
        command.setTabCompleter(handler);
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new SleepListener(this), this);
        getServer().getPluginManager().registerEvents(new WorldUnloadListener(this), this);
    }

    private void logRuntime(PluginSettings settings) {
        if (!settings.isEnabled()) {
            getLogger().info("Synchronization is disabled by configuration.");
            return;
        }

        String scope = settings.isGlobal()
                ? "all loaded worlds"
                : settings.getWorlds().toString();

        getLogger().info("Mode=" + settings.getMode()
                + ", timezone=" + settings.getZoneId().getId()
                + ", scope=" + scope
                + ", interval=" + settings.getSynchronizationIntervalTicks() + " ticks");
    }
}
