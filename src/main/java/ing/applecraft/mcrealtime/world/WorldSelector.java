package ing.applecraft.mcrealtime.world;

import ing.applecraft.mcrealtime.config.PluginSettings;
import org.bukkit.World;

import java.util.Objects;

/** Determines whether a Bukkit world belongs to the configured synchronization scope. */
public final class WorldSelector {
    private final PluginSettings settings;

    public WorldSelector(PluginSettings settings) {
        this.settings = Objects.requireNonNull(settings, "settings");
    }

    public boolean isAffected(World world) {
        Objects.requireNonNull(world, "world");
        return settings.isGlobal() || settings.getWorlds().contains(world.getName());
    }
}
