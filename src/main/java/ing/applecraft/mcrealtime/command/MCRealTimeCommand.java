package ing.applecraft.mcrealtime.command;

import ing.applecraft.mcrealtime.MCRealTimePlugin;
import ing.applecraft.mcrealtime.PluginRuntime;
import ing.applecraft.mcrealtime.config.PluginSettings;
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

public final class MCRealTimeCommand implements CommandExecutor, TabCompleter {
    private static final String USE_PERMISSION = "mcrealtime.use";
    private static final String ADMIN_PERMISSION = "mcrealtime.admin";

    private final MCRealTimePlugin plugin;

    public MCRealTimeCommand(MCRealTimePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender, label);
            return true;
        }

        String subcommand = args[0];
        if (subcommand.equalsIgnoreCase("status") || subcommand.equalsIgnoreCase("info")) {
            if (!sender.hasPermission(USE_PERMISSION)) {
                sendNoPermission(sender);
                return true;
            }
            sendStatus(sender);
            return true;
        }

        if (subcommand.equalsIgnoreCase("reload")) {
            if (!sender.hasPermission(ADMIN_PERMISSION)) {
                sendNoPermission(sender);
                return true;
            }

            if (plugin.reloadRuntime()) {
                sender.sendMessage(prefix() + ChatColor.GREEN + "Configuration reloaded successfully.");
            } else {
                sender.sendMessage(prefix() + ChatColor.RED
                        + "Reload failed. The previous runtime configuration is still active; check the console.");
            }
            return true;
        }

        sendHelp(sender, label);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length != 1) {
            return Collections.emptyList();
        }

        List<String> candidates = new ArrayList<>();
        if (sender.hasPermission(USE_PERMISSION)) {
            candidates.add("status");
            candidates.add("info");
        }
        if (sender.hasPermission(ADMIN_PERMISSION)) {
            candidates.add("reload");
        }

        List<String> completions = new ArrayList<>();
        StringUtil.copyPartialMatches(args[0], candidates, completions);
        Collections.sort(completions);
        return completions;
    }

    private void sendHelp(CommandSender sender, String label) {
        sender.sendMessage(prefix() + ChatColor.GOLD + "Commands:");
        if (sender.hasPermission(USE_PERMISSION)) {
            sender.sendMessage(ChatColor.YELLOW + "/" + label + " status/info"
                    + ChatColor.GRAY + " - show current synchronization state");
        }
        if (sender.hasPermission(ADMIN_PERMISSION)) {
            sender.sendMessage(ChatColor.YELLOW + "/" + label + " reload"
                    + ChatColor.GRAY + " - validate and reload configuration");
        }
    }

    private void sendStatus(CommandSender sender) {
        PluginRuntime runtime = plugin.getRuntime();
        if (runtime == null) {
            sender.sendMessage(prefix() + ChatColor.RED + "No runtime is active.");
            return;
        }

        PluginSettings settings = runtime.getSettings();
        sender.sendMessage(prefix() + ChatColor.GOLD + "Status");
        sender.sendMessage(ChatColor.GRAY + "Enabled: " + value(settings.isEnabled()));
        sender.sendMessage(ChatColor.GRAY + "Mode: " + ChatColor.WHITE + settings.getMode().name());
        sender.sendMessage(ChatColor.GRAY + "Timezone: " + ChatColor.WHITE + settings.getZoneId().getId());

        if (settings.getMode() == TimeMode.SOLAR) {
            sender.sendMessage(ChatColor.GRAY + "Location: " + ChatColor.WHITE
                    + settings.getSolarLocation().getLatitude() + ", "
                    + settings.getSolarLocation().getLongitude());
        }

        sender.sendMessage(ChatColor.GRAY + "Scope: " + ChatColor.WHITE
                + (settings.isGlobal() ? "all loaded worlds" : settings.getWorlds().toString()));
        sender.sendMessage(ChatColor.GRAY + "Sync interval: " + ChatColor.WHITE
                + settings.getSynchronizationIntervalTicks() + " ticks");
        sender.sendMessage(ChatColor.GRAY + "Current Minecraft time: " + ChatColor.WHITE
                + runtime.getCurrentMinecraftTime());
    }

    private String value(boolean enabled) {
        return (enabled ? ChatColor.GREEN : ChatColor.RED) + Boolean.toString(enabled);
    }

    private void sendNoPermission(CommandSender sender) {
        sender.sendMessage(prefix() + ChatColor.RED + "You do not have permission to use this command.");
    }

    private String prefix() {
        return ChatColor.DARK_GREEN + "[MCRealTime] " + ChatColor.RESET;
    }
}
