package ing.applecraft.mcrealtime;

import ing.applecraft.mcrealtime.Updater.ReleaseType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public final class MCRealTime extends JavaPlugin implements CommandExecutor{

    public static FileConfiguration config;

    public static String prefix;
    public static List<String> authors;
    public static String website;
    public static String description;
    public static String dversion;

    public static String timezoneconfig;

    public static int x_update = 0;
    public static Updater updater;
    public static boolean update;
    public static Updater autoupdater;
    public static boolean autoupdate;
    public static String name;
    public static ReleaseType type;
    public static String version;
    public static String link;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = getConfig();
        config.options().copyDefaults(true);

        timezoneconfig = config.getString("timezone");
        dversion = getDescription().getVersion();
        authors = getDescription().getAuthors();
        website = getDescription().getWebsite();
        description = getDescription().getDescription();
        prefix = ChatColor.DARK_GREEN + "♦ MCRealTime" + ChatColor.DARK_GREEN + " v" + dversion + ChatColor.GREEN + " ♦ ";


        if(config.getBoolean("auto_update", true)){
            autoupdater = new Updater(this, 286270, getFile(), Updater.UpdateType.DEFAULT, true);
            autoupdate = autoupdater.getResult() == Updater.UpdateResult.UPDATE_AVAILABLE;

            if(autoupdate) {
                Bukkit.getConsoleSender().sendMessage("__________________________________________________");
                Bukkit.getConsoleSender().sendMessage(prefix + " " + ChatColor.GREEN + "by " + authors);
                Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "The Update " + name + ", Type: " + type + " for " + version + "(+) was updated successfully!");
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Please restart your minecraft server!");
                Bukkit.getConsoleSender().sendMessage("__________________________________________________");
            }
        }else {
            updater = new Updater(this, 286270, getFile(), Updater.UpdateType.NO_DOWNLOAD, false);
            update = updater.getResult() == Updater.UpdateResult.UPDATE_AVAILABLE;

            if(update){
                Bukkit.getConsoleSender().sendMessage("__________________________________________________");
                Bukkit.getConsoleSender().sendMessage(prefix + " " + ChatColor.GREEN + "by " + MCRealTime.authors);
                Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "An update is avaible: " + MCRealTime.name + ", Type: " + MCRealTime.type + " for " + MCRealTime.version + "(+)" + " avaible at " + MCRealTime.link);
                Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "Type /mcrealtime update if you would like to automatically update.");
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Before you run the command, please open your server console.");
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "After the update finsish, please restart your minecraft server!");
                Bukkit.getConsoleSender().sendMessage("__________________________________________________");
            }
        }
        Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.GREEN + "TimeServer started!");
        updater = new Updater(this, 286270, getFile(), Updater.UpdateType.NO_DOWNLOAD, false);
        name = updater.getLatestName();
        version = updater.getLatestGameVersion();
        type = updater.getLatestType();
        link = updater.getLatestFileLink();

        getCommand("mcrealtime").setExecutor(this);
        getCommand("mcrealtime").setTabCompleter(new ConstructorTabCompleter());

        Bukkit.getPluginManager().registerEvents(new NoEnterBedEvent(), this);
        Bukkit.getPluginManager().registerEvents(new UpdateNotifyJoinEvent(), this);

        if(config.getBoolean("enable", true)) {

            if(config.getBoolean("global", true)) {
                new BukkitRunnable() {

                    @SuppressWarnings("deprecation")
                    @Override
                    public void run() {
                        for(World mcworlds : Bukkit.getWorlds()) {
                            mcworlds.setTime(getTime());
                            mcworlds.setGameRuleValue("doDaylightCycle", "false");
                            mcworlds.setGameRuleValue("doInsomnia", "false");
                        }
                    }
                }.runTaskTimer(this, 0, 1);

            } else {

                new BukkitRunnable() {

                    @SuppressWarnings("deprecation")
                    @Override
                    public void run() {

                        List<String> arrayworlds = config.getStringList("worlds");
                        for(String mcworlds : arrayworlds) {
                            Bukkit.getWorld(mcworlds).setTime(getTime());
                            Bukkit.getWorld(mcworlds).setGameRuleValue("doDaylightCycle", "false");
                            Bukkit.getWorld(mcworlds).setGameRuleValue("doInsomnia", "false");
                        }
                    }
                }.runTaskTimer(this, 0, 1);
            }
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.RED + "TimeServer stopped!");

        for(World mcworlds : Bukkit.getWorlds()) {
            mcworlds.setGameRuleValue("doDaylightCycle", "true");
            mcworlds.setGameRuleValue("doInsomnia", "true");
        }
    }

    private static Integer getTime() {
        SimpleDateFormat time = new SimpleDateFormat("HH:mm:ss");
        TimeZone tz = TimeZone.getTimeZone(timezoneconfig);
        time.setTimeZone(tz);
        String[] args = time.format(new Date()).split(":");
        int hours = Integer.parseInt(args[0])*1000; //1 h = 1000 ticks
        int minutes = Integer.parseInt(args[1])*(1000/60); //1 min. = 16,66...7 ticks
        int seconds = Integer.parseInt(args[2])*(1000/60)/60; //1 sec = 0,277...8 ticks

        return hours + minutes + seconds + 18000; //Zenit = 18000 ticks
    }

    @SuppressWarnings("unused")
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player p = (Player)sender;

        if(args.length != 1) {
            p.sendMessage("");
            p.sendMessage("");
            p.sendMessage("");
            p.sendMessage("");
            p.sendMessage("");
            p.sendMessage("__________________________________________________");
            p.sendMessage(prefix);
            p.sendMessage("");
            p.sendMessage(ChatColor.GOLD + "Here is a list with all avaible commands: ");
            p.sendMessage(ChatColor.RED + "/mcrealtime info " + ChatColor.WHITE + "- " + ChatColor.GRAY + "You'll get important information to this plugin");
            p.sendMessage(ChatColor.RED + "/mcrealtime contact " + ChatColor.WHITE + "- " + ChatColor.GRAY + "You'll get my contact information");
            p.sendMessage(ChatColor.RED + "/mcrealtime update " + ChatColor.WHITE + "- " + ChatColor.GRAY + "ATTENTION! BEFORE RUN, TAKE A LOOK IN YOUR SERVER CONSOLE! Running an auto update.");
            p.sendMessage(ChatColor.RED + "/mcrealtime changelog " + ChatColor.WHITE + "- " + ChatColor.GRAY + " You'll get all changes about the last update");
            p.sendMessage(ChatColor.RED + "/mcrealtime uninstall " + ChatColor.WHITE + "- " + ChatColor.GRAY + "Prepare MCRealTime for uninstallation");
            p.sendMessage(ChatColor.RED + "/mcrealtime reload " + ChatColor.WHITE + "- " + ChatColor.GRAY + "Command is not in use. Just for error prevention (github issue #5)");
            p.sendMessage("__________________________________________________");
            p.sendMessage("");
            p.sendMessage("");
            p.sendMessage("");
            p.sendMessage("");
            p.sendMessage("");
        }else if(args[0].equalsIgnoreCase("info")) {
            if(!p.hasPermission(config.getString("Permissions.info"))) {
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("__________________________________________________");
                p.sendMessage(prefix);
                p.sendMessage("");
                p.sendMessage(ChatColor.RED + "You do not have permissions to perform this command !");
                p.sendMessage("__________________________________________________");
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("");
            }else {
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("__________________________________________________");
                p.sendMessage(prefix);
                p.sendMessage("");
                p.sendMessage(ChatColor.GOLD + "Authors: " + ChatColor.GREEN + authors);
                p.sendMessage(ChatColor.GOLD + "Website: " + ChatColor.GREEN + website);
                p.sendMessage(ChatColor.GOLD + "You will get more information with this command:");
                p.sendMessage(ChatColor.AQUA + "/mcrealtime contact");
                p.sendMessage(ChatColor.RED + "You can not type this command: /time set <time> !");
                p.sendMessage(ChatColor.AQUA + "This plugin is an OpenSource project !");
                p.sendMessage(ChatColor.AQUA + "It means, that you have all rights reserved !");
                p.sendMessage(ChatColor.AQUA + "You have all rights reserved in case of the MIT License !");
                p.sendMessage(ChatColor.GOLD + "I wish you much fun with that. Yours Faithfuly Dev_Gabriel_M ! :)");
                p.sendMessage("");
                p.sendMessage(ChatColor.AQUA + "PS: Take a look in the config file for more settings.");
                p.sendMessage("__________________________________________________");
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("");
            }
        }else if(args[0].equalsIgnoreCase("reload")) {
            if(!p.hasPermission(config.getString("Permissions.reload"))) {
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("__________________________________________________");
                p.sendMessage(prefix);
                p.sendMessage("");
                p.sendMessage(ChatColor.RED + "You do not have permissions to perform this command !");
                p.sendMessage("__________________________________________________");
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("");
            }else {
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("__________________________________________________");
                p.sendMessage(prefix);
                p.sendMessage("");
                p.sendMessage(ChatColor.RED + "Command is not in use. Just for error prevention (github issue #5)");
                p.sendMessage("__________________________________________________");
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("");
            }
        }else if(args[0].equalsIgnoreCase("contact")) {
            if(!p.hasPermission(config.getString("Permissions.contact"))) {
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("__________________________________________________");
                p.sendMessage(prefix);
                p.sendMessage("");
                p.sendMessage(ChatColor.RED + "You do not have permissions to perform this command !");
                p.sendMessage("__________________________________________________");
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("");
            }else {
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("__________________________________________________");
                p.sendMessage(prefix);
                p.sendMessage("");
                p.sendMessage(ChatColor.GOLD + "You have some problems or you want post a feedback ?");
                p.sendMessage(ChatColor.GOLD + "No problem ! You could me send your feedback on the issue site of:");
                p.sendMessage(ChatColor.GOLD + "https://dev.bukkit.org/projects/mcrealtime/issues");
                p.sendMessage("");
                p.sendMessage(ChatColor.GOLD + "If you have some problems, you can contact me under:");
                p.sendMessage(ChatColor.GOLD + "E-Mail: " + ChatColor.GREEN + "contact@applecraft.ing");
                p.sendMessage(ChatColor.GOLD + "Thanks for understanding !");
                p.sendMessage(ChatColor.GOLD + "See ya around :)");
                p.sendMessage("__________________________________________________");
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("");
            }
        }else if(args[0].equalsIgnoreCase("uninstall")) {
            if(!p.hasPermission(config.getString("Permissions.uninstall"))) {
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("__________________________________________________");
                p.sendMessage(prefix);
                p.sendMessage("");
                p.sendMessage(ChatColor.RED + "You do not have permissions to perform this command !");
                p.sendMessage("__________________________________________________");
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("");
            }else {
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("__________________________________________________");
                p.sendMessage(prefix);
                p.sendMessage(ChatColor.GOLD + "Made by " + ChatColor.GREEN + authors);
                p.sendMessage(ChatColor.GOLD + "Description of the plugin: " + ChatColor.GREEN + description);
                p.sendMessage("");
                p.sendMessage(ChatColor.GOLD + "Uninstall:");
                p.sendMessage(ChatColor.RED + "1. Shutdown the local server.");
                p.sendMessage(ChatColor.RED + "2. Remove MCRealTime from the plugins folder of your server.");
                p.sendMessage(ChatColor.RED + "3. That's it ! The game rules DoDayLightCycle and doInsomnia are automatically set on true again !");
                p.sendMessage("");
                p.sendMessage(ChatColor.GOLD + "Thank you for download and please give me your opportunity as feedback at https://dev.bukkit.org/projects/mcrealtime for permitting me to get better.");
                p.sendMessage("__________________________________________________");
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("");
            }
        }else if(args[0].equalsIgnoreCase("changelog")) {
            if(!p.hasPermission(config.getString("Permissions.changelog"))) {
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("__________________________________________________");
                p.sendMessage(prefix);
                p.sendMessage("");
                p.sendMessage(ChatColor.RED + "You do not have permissions to perform this command !");
                p.sendMessage("__________________________________________________");
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("");
            }else {
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("__________________________________________________");
                p.sendMessage(prefix);
                p.sendMessage(ChatColor.GOLD + "Made by " + ChatColor.GREEN + authors);
                p.sendMessage(ChatColor.GOLD + "Description of the plugin: " + ChatColor.GREEN + description);
                p.sendMessage("");
                p.sendMessage(ChatColor.GOLD + "Changelogs:");
                p.sendMessage(ChatColor.GREEN + "+ Added version support 1.8 - supported latest by maven versioning.");
                p.sendMessage(ChatColor.GREEN + "+ Added second as time value support.");
                p.sendMessage(ChatColor.GREEN + "+ Solved github issue #5.");
                p.sendMessage("__________________________________________________");
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("");
            }
        }else if(args[0].equalsIgnoreCase("update")) {
            if(!p.hasPermission(config.getString("Permissions.update"))) {
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("__________________________________________________");
                p.sendMessage(prefix);
                p.sendMessage("");
                p.sendMessage(ChatColor.RED + "You do not have permissions to perform this command !");
                p.sendMessage("__________________________________________________");
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("");
                p.sendMessage("");
            }else {
                if(update = updater.getResult() == Updater.UpdateResult.UPDATE_AVAILABLE) {
                    if(config.getBoolean("auto_update", true)){
                        p.sendMessage("");
                        p.sendMessage("");
                        p.sendMessage("");
                        p.sendMessage("");
                        p.sendMessage("");
                        p.sendMessage("__________________________________________________");
                        p.sendMessage(prefix);
                        p.sendMessage(ChatColor.GOLD + "Made by §a" + authors);
                        p.sendMessage(ChatColor.RED + "You can not do that!");
                        p.sendMessage(ChatColor.RED + "Auto update is activated!");
                        p.sendMessage("__________________________________________________");
                        p.sendMessage("");
                        p.sendMessage("");
                        p.sendMessage("");
                        p.sendMessage("");
                        p.sendMessage("");
                    }else if(x_update == 0){
                        x_update = 1;
                        Updater updater = new Updater(this, 286270, getFile(), Updater.UpdateType.NO_VERSION_CHECK, true);
                        p.sendMessage("");
                        p.sendMessage("");
                        p.sendMessage("");
                        p.sendMessage("");
                        p.sendMessage("");
                        p.sendMessage("__________________________________________________");
                        p.sendMessage(prefix);
                        p.sendMessage(ChatColor.GOLD + "Made by " + ChatColor.GREEN + authors);
                        p.sendMessage(ChatColor.GOLD + "The update is running!");
                        p.sendMessage(ChatColor.GOLD + "Look in your console. If the update is finished, please restart your minecraft server.");
                        p.sendMessage("__________________________________________________");
                        p.sendMessage("");
                        p.sendMessage("");
                        p.sendMessage("");
                        p.sendMessage("");
                        p.sendMessage("");
                    } else {
                        p.sendMessage("");
                        p.sendMessage("");
                        p.sendMessage("");
                        p.sendMessage("");
                        p.sendMessage("");
                        p.sendMessage("__________________________________________________");
                        p.sendMessage(prefix);
                        p.sendMessage(ChatColor.GOLD + "Made by " + ChatColor.GREEN + authors);
                        p.sendMessage(ChatColor.GREEN + "Update is running... Check your console!");
                        p.sendMessage("__________________________________________________");
                        p.sendMessage("");
                        p.sendMessage("");
                        p.sendMessage("");
                        p.sendMessage("");
                        p.sendMessage("");
                    }
                } else {
                    p.sendMessage("");
                    p.sendMessage("");
                    p.sendMessage("");
                    p.sendMessage("");
                    p.sendMessage("");
                    p.sendMessage("__________________________________________________");
                    p.sendMessage(prefix);
                    p.sendMessage(ChatColor.GOLD + "Made by " + ChatColor.GREEN + authors);
                    p.sendMessage(ChatColor.GREEN + "MCRealTime is up to date!");
                    p.sendMessage("__________________________________________________");
                    p.sendMessage("");
                    p.sendMessage("");
                    p.sendMessage("");
                    p.sendMessage("");
                    p.sendMessage("");
                }
            }
        }
        return true;
    }
}
