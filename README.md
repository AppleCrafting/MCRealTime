# MCRealTime

Synchronize Minecraft time with real-world time — either by local clock time or by the actual solar day at a selected location.

MCRealTime started from a simple idea: if it is evening in the real world, it should also feel like evening in Minecraft. Version 4.8 is a major rewrite of the original plugin with a cleaner architecture, location-based solar time, safe configuration migration, and a new GitHub-based updater.

## Features

- **CLOCK mode** — synchronizes Minecraft time with the configured real-world time zone.
- **SOLAR mode** — maps the real sunrise, solar noon, sunset, and night cycle of a selected location to the Minecraft day.
- **Location search** — choose a place with `/mcrt location set <location>` and tab completion.
- **Bundled GeoNames database** — location lookup works locally; no external geocoding service is required at runtime.
- **Global or per-world synchronization**.
- **Configurable synchronization interval**.
- **Safe world-state handling** — MCRealTime restores world state it owns when the plugin is disabled.
- **Sleep / insomnia handling** to prevent normal Minecraft mechanics from breaking synchronized time.
- **Safe configuration migration** with backups of older configuration files.
- **Asynchronous update checks** against GitHub Releases.
- **Optional update download** into Bukkit's update directory for installation on the next server restart.
- **No hot-swapping** of the running plugin JAR.

## Time modes

### CLOCK

CLOCK mode follows civil time in the configured time zone.

Minecraft's normal 24,000-tick day is aligned with the real clock:

- approximately 06:00 -> sunrise / Minecraft time `0`
- approximately 12:00 -> noon / Minecraft time `6000`
- approximately 18:00 -> sunset / Minecraft time `12000`
- approximately 00:00 -> midnight / Minecraft time `18000`

The configured IANA time-zone ID is used, for example:

```yaml
timezone: Europe/Berlin
```

Daylight-saving-time changes are therefore handled by Java's time-zone database.

### SOLAR

SOLAR mode uses the selected place's coordinates and time zone to calculate the solar day.

The mapping is based on:

- sunrise -> Minecraft `0`
- solar noon -> Minecraft `6000`
- sunset -> Minecraft `12000`
- next sunrise -> Minecraft `24000` / `0`

The periods between these points are interpolated so that Minecraft follows the local solar day rather than a fixed 12-hour day/night split.

For unusual polar conditions where a normal sunrise/sunset cycle cannot be calculated, MCRealTime falls back to CLOCK behavior.

## Installation

1. Stop the Minecraft server.
2. Download the MCRealTime JAR from **GitHub Releases** or **BukkitDev**.
3. Remove any older MCRealTime JAR from the `plugins/` directory.
4. Rename the downloaded file to:

```text
MCRealTime.jar
```

5. Place it in:

```text
plugins/MCRealTime.jar
```

6. Start the server.
7. Configure MCRealTime or use the `/mcrt` commands.

The release asset itself may contain the version in its filename, for example:

```text
MCRealTime-4.8.jar
```

The recommended installed filename is deliberately version-independent:

```text
MCRealTime.jar
```

This avoids situations where a future MCRealTime 6.0 is still running from a file named `MCRealTime-5.0.jar`.

If MCRealTime detects another filename, it will warn the administrator at startup and in `/mcrt status`. The plugin and updater will continue to work with the existing filename until the administrator stops the server and renames the JAR.

> **Important:** Do not leave two MCRealTime JAR files in the `plugins/` directory. Always stop the server and replace the old JAR instead.

## Updating from an older MCRealTime version

MCRealTime 4.8 uses a versioned configuration format.

When an older configuration needs migration, MCRealTime:

1. reads the existing on-disk configuration,
2. preserves settings that still belong to the current schema,
3. archives the complete old configuration,
4. writes the new configuration format,
5. restores the preserved values.

Legacy configuration files are stored with names such as:

```text
config-legacy-v1.yml
config-legacy-v1-1.yml
```

Values that are no longer used by the current version are **not silently destroyed**; they remain available in the legacy archive.

A failed migration attempts to restore the previous configuration.

## Commands

Main command:

```text
/mcrt
```

| Command | Description |
| --- | --- |
| `/mcrt` | Show the available MCRealTime commands |
| `/mcrt status` | Show the current synchronization state |
| `/mcrt info` | Alias of `/mcrt status` |
| `/mcrt reload` | Validate and reload MCRealTime's configuration/runtime |
| `/mcrt mode get` | Show the current time mode |
| `/mcrt mode set clock` | Switch to CLOCK mode |
| `/mcrt mode set solar` | Switch to SOLAR mode |
| `/mcrt location get` | Show the configured solar location |
| `/mcrt location set <location>` | Select a solar location using tab completion |
| `/mcrt update` | Check GitHub for a newer release |
| `/mcrt update download` | Download an available update for installation on the next restart |

`/mcrt reload` reloads **MCRealTime only**. It is not Bukkit's global `/reload` command.

## Permissions

| Permission | Purpose |
| --- | --- |
| `mcrealtime.use` | Access to user/status commands such as `/mcrt status` and `/mcrt info` |
| `mcrealtime.admin` | Administrative commands such as reload, mode, location, and updater operations |

The exact permission defaults are defined in MCRealTime's `plugin.yml`.

## Configuration

Example configuration:

```yaml
config-version: 3

enabled: true

mode: SOLAR

timezone: Europe/Berlin

solar:
  latitude: 52.5200
  longitude: 13.4050
  place:
    geoname-id: 0
    name: Berlin
    country-code: DE

global: true

worlds:
  - world

synchronization:
  interval-ticks: 20

behavior:
  prevent-sleep: true
  disable-insomnia: true

updater:
  enabled: true
  check-on-start: true
  auto-download: false
```

### General settings

#### `enabled`

Enables or disables MCRealTime's synchronization runtime.

#### `mode`

Available values:

```text
CLOCK
SOLAR
```

#### `timezone`

IANA time-zone ID used by MCRealTime, for example:

```text
Europe/Berlin
America/New_York
Asia/Tokyo
```

### Solar settings

#### `solar.latitude` / `solar.longitude`

Coordinates used for solar calculations.

Normally you do not need to edit these manually. Use:

```text
/mcrt location set <location>
```

MCRealTime will update the coordinates and time zone for the selected GeoNames place.

#### `solar.place.*`

Stores metadata about the selected place:

```yaml
solar:
  place:
    geoname-id: ...
    name: ...
    country-code: ...
```

This allows commands such as `/mcrt status` and `/mcrt location get` to display a readable place name instead of only coordinates.

### World scope

#### `global: true`

Synchronize all loaded worlds managed by MCRealTime.

#### `global: false`

Only worlds listed under `worlds` are synchronized.

Example:

```yaml
global: false

worlds:
  - world
  - survival
```

### Synchronization

#### `synchronization.interval-ticks`

Controls how often MCRealTime reapplies synchronized time.

Example:

```yaml
synchronization:
  interval-ticks: 20
```

Twenty Minecraft ticks are normally approximately one second.

### Behavior

#### `behavior.prevent-sleep`

Controls MCRealTime's sleep-related protection so normal sleep mechanics do not break synchronized time.

#### `behavior.disable-insomnia`

Controls insomnia handling where the running server version supports the relevant gamerule.

MCRealTime uses capability detection for version-dependent gamerules and restores world state that it owns when it is disabled.

### Updater

```yaml
updater:
  enabled: true
  check-on-start: true
  auto-download: false
```

#### `updater.enabled`

Enables MCRealTime's updater functionality.

#### `updater.check-on-start`

Checks GitHub Releases asynchronously when MCRealTime starts.

The HTTP request is not performed on Minecraft's main server thread.

#### `updater.auto-download`

When enabled together with `check-on-start`, MCRealTime downloads an available update into Bukkit's update directory.

The downloaded JAR is validated before being staged. MCRealTime first downloads to a temporary `.part` file and only moves it into place after the download has completed successfully.

The update becomes active only after a normal server restart.

Default/recommended setting:

```yaml
auto-download: false
```

You can always check or download manually:

```text
/mcrt update
/mcrt update download
```

### Update source

MCRealTime may be distributed through both **GitHub Releases** and **BukkitDev**.

The built-in updater uses **GitHub Releases** as its update source.

Uploading the same release to BukkitDev does not change the updater behavior.

## Location data and GeoNames attribution

MCRealTime includes geographical place data used for location search.

**Location data is based on GeoNames geographical data, licensed under CC BY 4.0.**

GeoNames:

https://www.geonames.org/

Creative Commons Attribution 4.0 International:

https://creativecommons.org/licenses/by/4.0/

The GeoNames data is embedded with MCRealTime so `/mcrt location set` does not need to contact an external geocoding service while the server is running.

## Network access

Normal time synchronization does not require an external time or location web service.

Network access is used only by the optional updater when it contacts GitHub Releases.

Disable updater network access with:

```yaml
updater:
  enabled: false
```

## Compatibility

MCRealTime 4.8 is compiled for **Java 8 bytecode**.

Verified during 4.8 development:

| Server | Java | Status |
| --- | --- | --- |
| Spigot/Bukkit 1.8.8 | Java 8 | Tested successfully |
| Paper 1.14.4 (git-Paper-245) | OpenJDK 8 | Tested successfully |
| Paper 26.3 build 140 | Temurin 25.0.4 | Tested successfully |

MCRealTime deliberately avoids NMS/CraftBukkit internals for its core
synchronization logic in order to keep compatibility as broad as practical.

### Modern Paper legacy warning

Because MCRealTime 4.8 retains compatibility with Minecraft 1.8.8,
it intentionally does not declare Bukkit's `api-version` field.

Modern Paper versions therefore report MCRealTime as a legacy plugin
during startup. This warning is expected and does not indicate that
MCRealTime failed to load.

## Notes about Minecraft time

MCRealTime owns the time of worlds within its configured scope while synchronization is enabled.

Commands or mechanics that try to change world time may therefore appear to have no lasting effect because MCRealTime will synchronize the world again.

This is intentional.

## Troubleshooting

### The configured place is not shown in `/mcrt status`

Select a place with:

```text
/mcrt location set <location>
```

Use tab completion to choose a result from the bundled GeoNames database.

### MCRealTime warns about the plugin filename

Stop the server and rename the installed plugin JAR to:

```text
MCRealTime.jar
```

Do not keep the old MCRealTime JAR beside it.

### Update checks fail

The updater is optional. A GitHub/network failure does not disable MCRealTime's normal synchronization functionality.

You can disable it completely:

```yaml
updater:
  enabled: false
```

### A configuration migration occurred

Look inside:

```text
plugins/MCRealTime/
```

for a `config-legacy-v*.yml` archive containing the previous configuration.

## Source code and issue reports

Source code:

https://github.com/AppleCrafting/MCRealTime

BukkitDev:

https://dev.bukkit.org/projects/mcrealtime

For bug reports and development ideas, please use the GitHub issue tracker whenever possible.

## Credits

MCRealTime was originally created from the idea of bringing real-world time into Minecraft and has been developed and maintained by Gabriel / Sapentiae.

Thanks to everyone who has tested MCRealTime, reported problems, suggested improvements, or used the plugin over the years.

Special thanks to the **GeoNames** project and its contributors for the geographical data used by MCRealTime's location search.

## License

For MCRealTime's software license, see the `LICENSE` file in the source repository.

GeoNames-derived geographical data is separately licensed under **Creative Commons Attribution 4.0 International (CC BY 4.0)**.

---

I hope you enjoy MCRealTime.

— Gabriel / Sapentiae