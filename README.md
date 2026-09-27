# MCRealTime 5

MCRealTime synchronizes the Minecraft day/night cycle with real-world time.
Version 5 is a clean rewrite of the original plugin with two independent time models:

- **CLOCK** — the original idea: civil wall-clock time maps linearly to the 24,000-tick Minecraft day.
- **SOLAR** — astronomical mode: local sunrise maps to tick `0`, solar noon to `6000`, sunset to `12000`, and the night is interpolated until the next sunrise.

## Design goals

1. Keep the Minecraft-facing API surface deliberately small.
2. Compile the plugin to **Java 8 bytecode** for broad legacy-server compatibility.
3. Avoid NMS/CraftBukkit internals and Paper-only APIs in the universal build.
4. Keep astronomical and time calculations independent from Bukkit so they can be tested without a Minecraft server.
5. Restore world state changed by the plugin instead of assuming the administrator's previous gamerule values.

## Project structure

```text
src/main/java/ing/applecraft/mcrealtime
├── MCRealTimePlugin.java       Bukkit lifecycle only
├── PluginRuntime.java          one validated runtime configuration
├── astronomy/
│   ├── GeoLocation.java
│   ├── SolarCalculator.java
│   └── SolarDay.java
├── config/
│   ├── PluginSettings.java
│   └── SettingsLoader.java
├── time/
│   ├── TimeProvider.java
│   ├── TimeProviderFactory.java
│   ├── ClockTimeProvider.java
│   ├── SolarTimeProvider.java
│   └── TimeMode.java
├── world/
│   ├── TimeSynchronizer.java
│   ├── WorldSelector.java
│   └── WorldStateManager.java
├── listener/
│   ├── SleepListener.java
│   └── WorldUnloadListener.java
└── command/
    └── MCRealTimeCommand.java
```

The dependency direction is intentional:

```text
Bukkit/Paper server
      |
      v
MCRealTimePlugin -> PluginRuntime -> TimeProvider
                                  /             \
                         ClockTimeProvider   SolarTimeProvider
                                                   |
                                                   v
                                             SolarCalculator
```

`SolarCalculator`, `SolarDay`, `GeoLocation`, and both time providers contain no Bukkit dependency.

## Configuration

```yaml
enabled: true
mode: CLOCK

timezone: "Europe/Berlin"

solar:
  latitude: 53.1435
  longitude: 8.2146

global: true
worlds:
  - world

synchronization:
  interval-ticks: 20

behavior:
  prevent-sleep: true
  disable-insomnia: true
```

### CLOCK mode

The mapping is linear across a real 24-hour day:

| Civil time | Minecraft tick |
|---|---:|
| 00:00 | 18000 |
| 06:00 | 0 |
| 12:00 | 6000 |
| 18:00 | 12000 |

Unlike MCRealTime 4.x, minutes and seconds are calculated with floating-point arithmetic instead of integer division.

### SOLAR mode

The configured coordinates determine the astronomical day:

| Astronomical event | Minecraft tick |
|---|---:|
| sunrise | 0 |
| solar noon | 6000 |
| sunset | 12000 |
| next sunrise | 24000 / 0 |

This means that summer days are genuinely longer than winter days in real elapsed time.
The timezone is still used to decide which local calendar date belongs to an instant; the sunrise/sunset calculation itself uses latitude and longitude.

The implementation uses the NOAA/Meeus solar equations and a 90.833° apparent sunrise/sunset zenith.

### Polar regions

During polar day or polar night there is no ordinary sunrise/sunset pair. The current implementation intentionally falls back to CLOCK mapping for those dates. A dedicated polar-sky model can be added later without changing the Bukkit layer.

## Compatibility strategy

The universal build intentionally:

- targets Java 8 bytecode;
- compiles against Spigot API `1.8.8-R0.1-SNAPSHOT`;
- does not use NMS;
- does not use Paper-only API;
- does not declare a modern `api-version` in `plugin.yml`.

This maximizes compatibility across classic Bukkit/Spigot/Paper-style servers. It does **not** make the JAR a Fabric, Forge/NeoForge, BungeeCord, or Velocity plugin; those are different platforms. On proxy networks, install MCRealTime on the backend Bukkit-compatible servers whose worlds it should control.

## Upgrade notes from 4.x

The rewrite understands the old `enable` key as a fallback for the new `enabled` key. Existing top-level `timezone`, `global`, and `worlds` settings remain conceptually compatible.

The old embedded BukkitDev/Curse updater was deliberately removed from the runtime core. Update distribution should be handled separately from time synchronization.

The following old informational commands were also not carried into the core rewrite: `contact`, `changelog`, `uninstall`, and `update`.

Current commands:

```text
/mcrealtime status
/mcrealtime reload
```

`/mcrealtime info` is accepted as an alias of `status`.

## Build and test

Use a current JDK and Maven; the compiler is configured with `--release 8` so accidental use of post-Java-8 JDK APIs is rejected.

```bash
mvn clean verify
```

The resulting plugin JAR is placed in `target/`.

## Test coverage currently included

- classic clock mapping at midnight, sunrise, noon and sunset;
- minute/second precision that catches the old integer-division bug;
- Oldenburg solar times near the 2026 summer and winter solstices;
- canonical solar-event-to-Minecraft-tick mapping;
- seasonal day-length behavior;
- polar day/night detection.

## Next development steps

1. Run the Maven test suite with the real Spigot 1.8.8 API dependency.
2. Load-test the JAR on representative server generations (for example 1.8.8, 1.12.2, 1.16.5, 1.20.x, 1.21.x/current).
3. Add a compatibility test matrix and CI.
4. Decide whether sleeping should remain prevented by default or be allowed while MCRealTime immediately corrects vanilla time skipping.
5. Add a dedicated polar-day/polar-night mapping instead of CLOCK fallback.
6. Add optional update *notification* as a separate service, without automatic runtime downloading.
