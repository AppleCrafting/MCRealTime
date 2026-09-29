package ing.applecraft.mcrealtime.command;

import ing.applecraft.mcrealtime.MCRealTimePlugin;
import ing.applecraft.mcrealtime.PluginRuntime;
import ing.applecraft.mcrealtime.config.ConfigurationUpdateService;
import ing.applecraft.mcrealtime.config.PluginSettings;
import ing.applecraft.mcrealtime.location.LocationSearchService;
import ing.applecraft.mcrealtime.location.Place;
import ing.applecraft.mcrealtime.time.TimeMode;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class MCRealTimeCommand
        implements CommandExecutor, TabCompleter {

    private static final String USE_PERMISSION =
            "mcrealtime.use";

    private static final String ADMIN_PERMISSION =
            "mcrealtime.admin";

    private final MCRealTimePlugin plugin;

    private final LocationSearchService
            locationSearchService;

    private final PlaceCommandCodec
            placeCommandCodec;

    private final ConfigurationUpdateService
            configurationUpdateService;

    public MCRealTimeCommand(
            MCRealTimePlugin plugin,
            LocationSearchService locationSearchService) {

        if (plugin == null) {
            throw new IllegalArgumentException(
                    "plugin must not be null"
            );
        }

        if (locationSearchService == null) {
            throw new IllegalArgumentException(
                    "locationSearchService must not be null"
            );
        }

        this.plugin = plugin;

        this.locationSearchService =
                locationSearchService;

        this.placeCommandCodec =
                new PlaceCommandCodec();

        this.configurationUpdateService =
                new ConfigurationUpdateService(plugin);
    }

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args) {

        if (args.length == 0) {
            sendHelp(
                    sender,
                    label
            );

            return true;
        }

        String subcommand =
                args[0];

        /*
         * /mcrt status
         * /mcrt info
         */
        if (subcommand.equalsIgnoreCase("status")
                || subcommand.equalsIgnoreCase("info")) {

            if (!sender.hasPermission(USE_PERMISSION)) {
                sendNoPermission(sender);
                return true;
            }

            sendStatus(sender);
            return true;
        }

        /*
         * /mcrt reload
         */
        if (subcommand.equalsIgnoreCase("reload")) {

            if (!sender.hasPermission(ADMIN_PERMISSION)) {
                sendNoPermission(sender);
                return true;
            }

            if (plugin.reloadRuntime()) {

                sender.sendMessage(
                        prefix()
                                + ChatColor.GREEN
                                + "Configuration reloaded successfully."
                );

            } else {

                sender.sendMessage(
                        prefix()
                                + ChatColor.RED
                                + "Reload failed. "
                                + "The previous runtime configuration "
                                + "is still active; check the console."
                );
            }

            return true;
        }

        /*
         * /mcrt mode ...
         */
        if (subcommand.equalsIgnoreCase("mode")) {

            if (!sender.hasPermission(ADMIN_PERMISSION)) {
                sendNoPermission(sender);
                return true;
            }

            return handleMode(
                    sender,
                    label,
                    args
            );
        }

        /*
         * /mcrt location ...
         */
        if (subcommand.equalsIgnoreCase("location")) {

            if (!sender.hasPermission(ADMIN_PERMISSION)) {
                sendNoPermission(sender);
                return true;
            }

            return handleLocation(
                    sender,
                    label,
                    args
            );
        }

        sendHelp(
                sender,
                label
        );

        return true;
    }

    /*
     * =========================================================
     * MODE
     * =========================================================
     */

    private boolean handleMode(
            CommandSender sender,
            String label,
            String[] args) {

        /*
         * /mcrt mode get
         */
        if (args.length == 2
                && args[1].equalsIgnoreCase("get")) {

            PluginRuntime runtime =
                    plugin.getRuntime();

            if (runtime == null) {
                sender.sendMessage(
                        prefix()
                                + ChatColor.RED
                                + "No runtime is active."
                );

                return true;
            }

            sender.sendMessage(
                    prefix()
                            + ChatColor.GRAY
                            + "Current mode: "
                            + ChatColor.WHITE
                            + runtime
                            .getSettings()
                            .getMode()
                            .name()
            );

            return true;
        }

        /*
         * /mcrt mode set clock
         * /mcrt mode set solar
         */
        if (args.length == 3
                && args[1].equalsIgnoreCase("set")) {

            final TimeMode mode;

            if (args[2].equalsIgnoreCase("clock")) {

                mode = TimeMode.CLOCK;

            } else if (args[2].equalsIgnoreCase("solar")) {

                mode = TimeMode.SOLAR;

            } else {

                sender.sendMessage(
                        prefix()
                                + ChatColor.RED
                                + "Unknown mode. "
                                + "Use CLOCK or SOLAR."
                );

                return true;
            }

            if (configurationUpdateService.setMode(mode)) {

                sender.sendMessage(
                        prefix()
                                + ChatColor.GREEN
                                + "Time mode changed to "
                                + mode.name()
                                + "."
                );

            } else {

                sender.sendMessage(
                        prefix()
                                + ChatColor.RED
                                + "Could not change the time mode. "
                                + "The previous configuration "
                                + "was restored."
                );
            }

            return true;
        }

        sender.sendMessage(
                ChatColor.YELLOW
                        + "/"
                        + label
                        + " mode get"
        );

        sender.sendMessage(
                ChatColor.YELLOW
                        + "/"
                        + label
                        + " mode set <clock|solar>"
        );

        return true;
    }

    /*
     * =========================================================
     * LOCATION
     * =========================================================
     */

    private boolean handleLocation(
            CommandSender sender,
            String label,
            String[] args) {

        /*
         * /mcrt location get
         */
        if (args.length == 2
                && args[1].equalsIgnoreCase("get")) {

            PluginRuntime runtime =
                    plugin.getRuntime();

            if (runtime == null) {

                sender.sendMessage(
                        prefix()
                                + ChatColor.RED
                                + "No runtime is active."
                );

                return true;
            }

            PluginSettings settings =
                    runtime.getSettings();

            String configuredPlaceName =
                    plugin.getConfig().getString(
                            "solar.place.name"
                    );

            String configuredCountryCode =
                    plugin.getConfig().getString(
                            "solar.place.country-code"
                    );

            /*
             * Display human-readable metadata when
             * the location was selected through GeoNames.
             */
            if (configuredPlaceName != null
                    && !configuredPlaceName
                    .trim()
                    .isEmpty()) {

                String displayName =
                        configuredPlaceName;

                if (configuredCountryCode != null
                        && !configuredCountryCode
                        .trim()
                        .isEmpty()) {

                    displayName +=
                            " ("
                                    + configuredCountryCode
                                    + ")";
                }

                sender.sendMessage(
                        prefix()
                                + ChatColor.GRAY
                                + "Location: "
                                + ChatColor.WHITE
                                + displayName
                );
            }

            sender.sendMessage(
                    prefix()
                            + ChatColor.GRAY
                            + "Latitude: "
                            + ChatColor.WHITE
                            + settings
                            .getSolarLocation()
                            .getLatitude()
            );

            sender.sendMessage(
                    prefix()
                            + ChatColor.GRAY
                            + "Longitude: "
                            + ChatColor.WHITE
                            + settings
                            .getSolarLocation()
                            .getLongitude()
            );

            sender.sendMessage(
                    prefix()
                            + ChatColor.GRAY
                            + "Timezone: "
                            + ChatColor.WHITE
                            + settings
                            .getZoneId()
                            .getId()
            );

            return true;
        }

        /*
         * /mcrt location set <place>
         */
        if (args.length == 3
                && args[1].equalsIgnoreCase("set")) {

            final long geonameId;

            try {

                geonameId =
                        placeCommandCodec
                                .decodeGeoNameId(
                                        args[2]
                                );

            } catch (IllegalArgumentException exception) {

                sender.sendMessage(
                        prefix()
                                + ChatColor.RED
                                + "Invalid location selection."
                );

                sender.sendMessage(
                        prefix()
                                + ChatColor.GRAY
                                + "Use tab completion to "
                                + "select a location."
                );

                return true;
            }

            Optional<Place> place =
                    locationSearchService
                            .findByGeoNameId(
                                    geonameId
                            );

            if (!place.isPresent()) {

                sender.sendMessage(
                        prefix()
                                + ChatColor.RED
                                + "Location was not found."
                );

                return true;
            }

            Place selected =
                    place.get();

            /*
             * Save the configuration first.
             *
             * ConfigurationUpdateService also reloads
             * the MCRealTime runtime.
             */
            if (!configurationUpdateService
                    .setLocation(selected)) {

                sender.sendMessage(
                        prefix()
                                + ChatColor.RED
                                + "Could not save the selected location. "
                                + "The previous configuration "
                                + "was restored."
                );

                return true;
            }

            sender.sendMessage(
                    prefix()
                            + ChatColor.GREEN
                            + "Location changed to "
                            + selected.getName()
                            + " ("
                            + selected.getCountryCode()
                            + ")."
            );

            sender.sendMessage(
                    ChatColor.GRAY
                            + "Coordinates: "
                            + selected
                            .getCoordinates()
                            .getLatitude()
                            + ", "
                            + selected
                            .getCoordinates()
                            .getLongitude()
            );

            sender.sendMessage(
                    ChatColor.GRAY
                            + "Timezone: "
                            + selected
                            .getZoneId()
                            .getId()
            );

            return true;
        }

        sender.sendMessage(
                ChatColor.YELLOW
                        + "/"
                        + label
                        + " location get"
        );

        sender.sendMessage(
                ChatColor.YELLOW
                        + "/"
                        + label
                        + " location set <location>"
        );

        return true;
    }

    /*
     * =========================================================
     * TAB COMPLETION
     * =========================================================
     */

    @Override
    public List<String> onTabComplete(
            CommandSender sender,
            Command command,
            String alias,
            String[] args) {

        /*
         * /mcrt <TAB>
         */
        if (args.length == 1) {

            List<String> candidates =
                    new ArrayList<String>();

            if (sender.hasPermission(USE_PERMISSION)) {
                candidates.add("status");
                candidates.add("info");
            }

            if (sender.hasPermission(ADMIN_PERMISSION)) {
                candidates.add("reload");
                candidates.add("mode");
                candidates.add("location");
            }

            return complete(
                    args[0],
                    candidates
            );
        }

        /*
         * No administrative tab completion
         * without the admin permission.
         */
        if (!sender.hasPermission(ADMIN_PERMISSION)) {
            return Collections.emptyList();
        }

        if (args[0].equalsIgnoreCase("mode")) {
            return completeMode(args);
        }

        if (args[0].equalsIgnoreCase("location")) {
            return completeLocation(args);
        }

        return Collections.emptyList();
    }

    private List<String> completeMode(
            String[] args) {

        /*
         * /mcrt mode <TAB>
         */
        if (args.length == 2) {

            List<String> candidates =
                    new ArrayList<String>();

            candidates.add("get");
            candidates.add("set");

            return complete(
                    args[1],
                    candidates
            );
        }

        /*
         * /mcrt mode set <TAB>
         */
        if (args.length == 3
                && args[1].equalsIgnoreCase("set")) {

            List<String> candidates =
                    new ArrayList<String>();

            candidates.add("clock");
            candidates.add("solar");

            return complete(
                    args[2],
                    candidates
            );
        }

        return Collections.emptyList();
    }

    private List<String> completeLocation(
            String[] args) {

        /*
         * /mcrt location <TAB>
         */
        if (args.length == 2) {

            List<String> candidates =
                    new ArrayList<String>();

            candidates.add("get");
            candidates.add("set");

            return complete(
                    args[1],
                    candidates
            );
        }

        /*
         * /mcrt location set <query><TAB>
         */
        if (args.length == 3
                && args[1].equalsIgnoreCase("set")) {

            String query =
                    args[2]
                            .replace('_', ' ');

            List<Place> places =
                    locationSearchService
                            .suggestedPlaces(query);

            List<String> suggestions =
                    new ArrayList<String>();

            for (Place place : places) {

                suggestions.add(
                        placeCommandCodec.encode(
                                place
                        )
                );
            }

            return suggestions;
        }

        return Collections.emptyList();
    }

    /**
     * Performs normal Bukkit partial-match completion
     * for small static command lists.
     */
    private List<String> complete(
            String current,
            List<String> candidates) {

        List<String> completions =
                new ArrayList<String>();

        StringUtil.copyPartialMatches(
                current,
                candidates,
                completions
        );

        Collections.sort(
                completions
        );

        return completions;
    }

    /*
     * =========================================================
     * HELP / STATUS
     * =========================================================
     */

    private void sendHelp(
            CommandSender sender,
            String label) {

        sender.sendMessage(
                prefix()
                        + ChatColor.GOLD
                        + "Commands:"
        );

        if (sender.hasPermission(USE_PERMISSION)) {

            sender.sendMessage(
                    ChatColor.YELLOW
                            + "/"
                            + label
                            + " status/info"
                            + ChatColor.GRAY
                            + " - show current synchronization state"
            );
        }

        if (sender.hasPermission(ADMIN_PERMISSION)) {

            sender.sendMessage(
                    ChatColor.YELLOW
                            + "/"
                            + label
                            + " reload"
                            + ChatColor.GRAY
                            + " - validate and reload configuration"
            );

            sender.sendMessage(
                    ChatColor.YELLOW
                            + "/"
                            + label
                            + " mode get"
                            + ChatColor.GRAY
                            + " - show the current time mode"
            );

            sender.sendMessage(
                    ChatColor.YELLOW
                            + "/"
                            + label
                            + " mode set <clock|solar>"
                            + ChatColor.GRAY
                            + " - change the time mode"
            );

            sender.sendMessage(
                    ChatColor.YELLOW
                            + "/"
                            + label
                            + " location get"
                            + ChatColor.GRAY
                            + " - show the solar location"
            );

            sender.sendMessage(
                    ChatColor.YELLOW
                            + "/"
                            + label
                            + " location set <location>"
                            + ChatColor.GRAY
                            + " - select a solar location"
            );
        }
    }

    private void sendStatus(
            CommandSender sender) {

        PluginRuntime runtime =
                plugin.getRuntime();

        if (runtime == null) {

            sender.sendMessage(
                    prefix()
                            + ChatColor.RED
                            + "No runtime is active."
            );

            return;
        }

        PluginSettings settings =
                runtime.getSettings();

        sender.sendMessage(
                prefix()
                        + ChatColor.GOLD
                        + "Status"
        );

        sender.sendMessage(
                ChatColor.GRAY
                        + "Enabled: "
                        + value(
                        settings.isEnabled()
                )
        );

        sender.sendMessage(
                ChatColor.GRAY
                        + "Mode: "
                        + ChatColor.WHITE
                        + settings
                        .getMode()
                        .name()
        );

        sender.sendMessage(
                ChatColor.GRAY
                        + "Timezone: "
                        + ChatColor.WHITE
                        + settings
                        .getZoneId()
                        .getId()
        );

        if (settings.getMode()
                == TimeMode.SOLAR) {

            sender.sendMessage(
                    ChatColor.GRAY
                            + "Location: "
                            + ChatColor.WHITE
                            + settings
                            .getSolarLocation()
                            .getLatitude()
                            + ", "
                            + settings
                            .getSolarLocation()
                            .getLongitude()
            );
        }

        sender.sendMessage(
                ChatColor.GRAY
                        + "Scope: "
                        + ChatColor.WHITE
                        + (
                        settings.isGlobal()
                                ? "all loaded worlds"
                                : settings
                                .getWorlds()
                                .toString()
                )
        );

        sender.sendMessage(
                ChatColor.GRAY
                        + "Sync interval: "
                        + ChatColor.WHITE
                        + settings
                        .getSynchronizationIntervalTicks()
                        + " ticks"
        );

        sender.sendMessage(
                ChatColor.GRAY
                        + "Current Minecraft time: "
                        + ChatColor.WHITE
                        + runtime
                        .getCurrentMinecraftTime()
        );
    }

    private String value(
            boolean enabled) {

        return (
                enabled
                        ? ChatColor.GREEN
                        : ChatColor.RED
        ) + Boolean.toString(enabled);
    }

    private void sendNoPermission(
            CommandSender sender) {

        sender.sendMessage(
                prefix()
                        + ChatColor.RED
                        + "You do not have permission "
                        + "to use this command."
        );
    }

    private String prefix() {

        return ChatColor.DARK_GREEN
                + "[MCRealTime] "
                + ChatColor.RESET;
    }
}