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

    /*
     * Minecraft 26.x renamed several game rules.
     *
     * We deliberately resolve them by name so that MCRealTime can still
     * compile against the old Bukkit API while supporting modern servers.
     */
    private static final String[] DAYLIGHT_CYCLE_RULES = {
            "advance_time",
            "doDaylightCycle"
    };

    private static final String[] INSOMNIA_RULES = {
            "spawn_phantoms",
            "doInsomnia"
    };

    private final PluginSettings settings;

    private final Map<UUID, WorldState> originalStates =
            new HashMap<UUID, WorldState>();

    public WorldStateManager(PluginSettings settings) {
        this.settings = settings;
    }

    public void prepare(World world) {

        UUID worldId =
                world.getUID();

        if (originalStates.containsKey(worldId)) {
            return;
        }

        RuleState daylightCycle =
                captureRule(
                        world,
                        DAYLIGHT_CYCLE_RULES
                );

        RuleState insomnia = null;

        if (settings.isDisableInsomnia()) {
            insomnia =
                    captureRule(
                            world,
                            INSOMNIA_RULES
                    );
        }

        originalStates.put(
                worldId,
                new WorldState(
                        daylightCycle,
                        insomnia
                )
        );

        disableRule(
                world,
                daylightCycle
        );

        disableRule(
                world,
                insomnia
        );
    }

    public void restore(World world) {

        WorldState state =
                originalStates.remove(
                        world.getUID()
                );

        if (state == null) {
            return;
        }

        restoreRule(
                world,
                state.daylightCycle
        );

        restoreRule(
                world,
                state.insomnia
        );
    }

    public void restoreAll(
            Collection<World> loadedWorlds) {

        for (World world : loadedWorlds) {
            restore(world);
        }

        originalStates.clear();
    }

    private RuleState captureRule(
            World world,
            String[] candidates) {

        for (String candidate : candidates) {

            if (!world.isGameRule(candidate)) {
                continue;
            }

            return new RuleState(
                    candidate,
                    world.getGameRuleValue(
                            candidate
                    )
            );
        }

        return null;
    }

    private void disableRule(
            World world,
            RuleState state) {

        if (state == null) {
            return;
        }

        if (world.isGameRule(state.rule)) {
            world.setGameRuleValue(
                    state.rule,
                    "false"
            );
        }
    }

    private void restoreRule(
            World world,
            RuleState state) {

        if (state == null
                || state.value == null) {
            return;
        }

        if (world.isGameRule(state.rule)) {
            world.setGameRuleValue(
                    state.rule,
                    state.value
            );
        }
    }

    private static final class WorldState {

        private final RuleState daylightCycle;
        private final RuleState insomnia;

        private WorldState(
                RuleState daylightCycle,
                RuleState insomnia) {

            this.daylightCycle =
                    daylightCycle;

            this.insomnia =
                    insomnia;
        }
    }

    private static final class RuleState {

        private final String rule;
        private final String value;

        private RuleState(
                String rule,
                String value) {

            this.rule =
                    rule;

            this.value =
                    value;
        }
    }
}
