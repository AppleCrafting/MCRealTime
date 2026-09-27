package ing.applecraft.mcrealtime.world;

import ing.applecraft.mcrealtime.config.PluginSettings;
import org.bukkit.World;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Owns the Bukkit world state temporarily changed by MCRealTime and restores it
 * when the world leaves MCRealTime's control.
 */
public final class WorldStateManager {
    private static final String DAYLIGHT_CYCLE = "doDaylightCycle";
    private static final String INSOMNIA = "doInsomnia";

    private final PluginSettings settings;
    private final Map<UUID, WorldState> originalStates = new HashMap<UUID, WorldState>();

    public WorldStateManager(PluginSettings settings) {
        this.settings = settings;
    }

    public void prepare(World world) {
        UUID worldId = world.getUID();
        if (originalStates.containsKey(worldId)) {
            return;
        }

        String daylightCycle = world.isGameRule(DAYLIGHT_CYCLE)
                ? world.getGameRuleValue(DAYLIGHT_CYCLE)
                : null;
        String insomnia = settings.isDisableInsomnia() && world.isGameRule(INSOMNIA)
                ? world.getGameRuleValue(INSOMNIA)
                : null;

        originalStates.put(worldId, new WorldState(daylightCycle, insomnia));

        if (world.isGameRule(DAYLIGHT_CYCLE)) {
            world.setGameRuleValue(DAYLIGHT_CYCLE, "false");
        }
        if (settings.isDisableInsomnia() && world.isGameRule(INSOMNIA)) {
            world.setGameRuleValue(INSOMNIA, "false");
        }
    }

    public void restore(World world) {
        WorldState state = originalStates.remove(world.getUID());
        if (state == null) {
            return;
        }

        restoreRule(world, DAYLIGHT_CYCLE, state.daylightCycle);
        restoreRule(world, INSOMNIA, state.insomnia);
    }

    public void restoreAll(Collection<World> loadedWorlds) {
        for (World world : loadedWorlds) {
            restore(world);
        }
        originalStates.clear();
    }

    private void restoreRule(World world, String rule, String value) {
        if (value != null && world.isGameRule(rule)) {
            world.setGameRuleValue(rule, value);
        }
    }

    private static final class WorldState {
        private final String daylightCycle;
        private final String insomnia;

        private WorldState(String daylightCycle, String insomnia) {
            this.daylightCycle = daylightCycle;
            this.insomnia = insomnia;
        }
    }
}
