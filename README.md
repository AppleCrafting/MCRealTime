
# MCRealTime

**Bring real-world time into Minecraft.**

MCRealTime synchronizes Minecraft's day/night cycle with either civil time in a configured time zone or the astronomical solar day of a selected real-world location.

Version **4.8** introduced a major rewrite with location-based solar time, offline location search, safe configuration migration, and a GitHub-based updater.

Version **4.9** is a maintenance release that corrects solar-event date calculations for locations near the International Date Line.

## What's new in 4.9?

MCRealTime 4.9 improves the reliability of astronomical time calculations without introducing new commands or configuration requirements.

### Fixed

- Corrected the association of solar events with the selected location's local calendar date.
- Improved solar-day calculations for locations near the International Date Line, including locations with large UTC offsets.

### Tests

- Added automated regression tests for Berlin, Sydney, Samoa, and Kiribati.
- Verified the association of sunrise, solar noon, and sunset with the expected local calendar date.

### Updating

If you already use MCRealTime 4.8, its built-in GitHub updater can download and stage version 4.9 without requiring you to replace the JAR manually:

- With `updater.enabled: true`, `updater.check-on-start: true`, and `updater.auto-download: true`, the plugin checks for updates at startup and automatically stages an available update.
- With the default `updater.auto-download: false`, use `/mcrt update` to check for a release and `/mcrt update download` to download and stage it when available.

In both cases, **restart the server normally to activate the staged update**. MCRealTime never replaces its running JAR while the server is online. No configuration schema changes are required between versions 4.8 and 4.9.

**Users of MCRealTime 4.1:** On October 1, 2026, the historical CurseForge ServerMods API listed both versions 4.1 and 4.8. In a test of the installed 4.1 plugin, its legacy automatic updater successfully downloaded 4.8 **when automatic updating was enabled**. With that option disabled, the old manual update command incorrectly reported that the plugin was up to date. This is an observation about the **4.1 updater**, not the new `/mcrt update` command in 4.8 and later. If your 4.1 updater cannot obtain the release, a one-time manual upgrade remains an option. After starting version 4.8, use its built-in GitHub updater for 4.9 and later releases. Behavior in versions older than 4.1 has not been verified.

## Features

- **CLOCK mode** — synchronizes Minecraft time with the configured real-world time zone.
- **SOLAR mode** — maps the real sunrise, solar noon, sunset, and night cycle of a selected location to the Minecraft day.
- **Seasonal solar calculations** — automatically accounts for date-dependent changes in sunrise and sunset.
- **Location search** — choose a place using `/mcrt location set <location>` with tab completion.
- **Bundled GeoNames database** — location lookup works locally without an external geocoding service.
- **Global or per-world synchronization**.
- **Configurable synchronization interval**.
- **Safe world-state handling** — restores managed world settings when synchronization is stopped.
- **Sleep and insomnia handling** to prevent normal Minecraft mechanics from interfering with synchronized time.
- **Safe configuration migration** with backups of older configuration files.
- **Asynchronous update checks** using GitHub Releases.
- **Optional update downloads** into Bukkit's update directory for installation on the next server restart.
- **No hot-swapping** of the running plugin JAR.

## Time modes

### CLOCK

CLOCK mode follows civil time in the configured time zone.

Minecraft's normal 24,000-tick day is aligned with the real clock:

| Real-world time | Minecraft time |
| --- | --- |
| 06:00 | 0 |
| 12:00 | 6000 |
| 18:00 | 12000 |
| 00:00 | 18000 |

The configured IANA time-zone ID is used, for example:

```yaml
timezone: Europe/Berlin
```

Daylight-saving-time changes are handled using Java's time-zone database.

### SOLAR

SOLAR mode uses the selected location's coordinates, time zone, and calendar date to calculate the astronomical solar day.

The mapping is based on:

| Astronomical event | Minecraft time |
| --- | --- |
| Sunrise | 0 |
| Solar noon | 6000 |
| Sunset | 12000 |
| Next sunrise | 24000 / 0 |

The periods between these events are interpolated so that Minecraft follows the local solar day rather than a fixed 12-hour day/night split.

Because the calculation uses the actual calendar date, sunrise and sunset change throughout the year.

Version 4.9 improves the association between astronomical events and the selected location's local calendar date, particularly near the International Date Line.

**Polar regions:** When a normal sunrise/sunset cycle cannot be calculated, MCRealTime currently falls back to CLOCK mode. A dedicated polar sky model is not included.

**Visual limitation:** SOLAR mode adjusts Minecraft's time progression. It does not change the physical trajectory or seasonal inclination of the sun in Minecraft's sky.

## First-time installation or manual installation

These steps are for first-time installations and for cases where an existing updater is unavailable or cannot install the new version. **Existing MCRealTime 4.8 installations can use their built-in GitHub updater instead; see the updating section below.**

1. Stop your Minecraft server.
2. Download the latest MCRealTime JAR from GitHub Releases or BukkitDev.
3. Remove the older MCRealTime JAR from your `plugins/` directory.
4. Rename the downloaded JAR to `MCRealTime.jar`.
5. Place it in your server's `plugins/` directory.
6. Start the server.
7. Configure MCRealTime or use its `/mcrt` commands.

Download:

- GitHub Releases: https://github.com/AppleCrafting/MCRealTime/releases
- BukkitDev: https://dev.bukkit.org/projects/mcrealtime

The downloadable release asset includes the version number:

```text
MCRealTime-4.9.jar
```

The recommended installed filename is version-independent:

```text
plugins/MCRealTime.jar
```

Using a consistent installed filename makes future updates easier and prevents misleading filenames after upgrading.

If MCRealTime detects another filename, it warns the administrator. The plugin and updater can continue using the existing filename until the administrator renames it.

**Important:** Never leave multiple MCRealTime JAR files in the `plugins/` directory.

For later updates, prefer MCRealTime's built-in updater instead of repeating the manual installation process.

## Updating from an older MCRealTime version

### Updating from 4.8

MCRealTime 4.9 does not introduce a new configuration schema. Existing MCRealTime 4.8 installations can use their built-in GitHub updater:

1. Run `/mcrt update` to check GitHub Releases for a newer version, or enable `updater.check-on-start: true` to check at server startup.
2. With the default `updater.auto-download: false`, run `/mcrt update download` when an update is available. Alternatively, set `updater.auto-download: true` together with startup checks to let MCRealTime download updates automatically.
3. Once the update has been staged, **restart the server normally**. Bukkit installs the staged JAR during the restart.

There is no need to remove or replace the existing JAR manually when the built-in updater succeeds. A manual installation remains an option if the updater is disabled or cannot reach GitHub.

### Updating from 4.1 or earlier

MCRealTime 4.8 introduced a redesigned configuration format and a new GitHub-based updater.

MCRealTime 4.1 uses a legacy BukkitDev-based updater. On October 1, 2026, the historical CurseForge ServerMods API listed both 4.1 and 4.8. A test of the installed **4.1 plugin confirmed that its automatic updater successfully downloaded 4.8 when automatic updating was enabled**. When the automatic updater was disabled, the old manual update command reported "up to date" even though 4.8 was available. The reason for that misleading command result has not been established.

**For 4.1 installations:** Back up your configuration, enable the legacy automatic updater, and check whether it downloads 4.8. Verify that the new JAR has been staged and restart the test/server normally to activate it. Once **4.8 is actually running**, use its **GitHub-based updater** to check for and download 4.9, either with `/mcrt update` and `/mcrt update download`, or with automatic downloads enabled. Do not confuse these 4.8+ commands with the older 4.1 update command.

If your old updater still fails, you can upgrade manually once using the first-installation instructions. Direct upgrades from 4.1 to 4.9 through the legacy API, and update behavior in versions older than 4.1, have not been verified.

Before migrating from the older configuration format, back up your existing plugin configuration.

When an older configuration requires migration, MCRealTime:

1. Reads the existing configuration.
2. Preserves settings supported by the current schema.
3. Archives the complete previous configuration.
4. Writes the new configuration format.
5. Restores the preserved settings.

Legacy configuration backups use filenames such as:

```text
config-legacy-v1.yml
config-legacy-v1-1.yml
```

Values no longer supported by the current schema remain available in the legacy backup.

If migration fails, MCRealTime attempts to restore the previous configuration.

## Commands

The main command is:

```text
/mcrt
```

| Command | Description |
| --- | --- |
| `/mcrt` | Display available MCRealTime commands |
| `/mcrt status` | Display the current synchronization state |
| `/mcrt info` | Alias of `/mcrt status` |
| `/mcrt reload` | Validate and reload MCRealTime's configuration and runtime |
| `/mcrt mode get` | Display the current time mode |
| `/mcrt mode set clock` | Switch to CLOCK mode |
| `/mcrt mode set solar` | Switch to SOLAR mode |
| `/mcrt location get` | Display the configured solar location |
| `/mcrt location set <location>` | Select a location using tab completion |
| `/mcrt update` | Check GitHub for an available update |
| `/mcrt update download` | Download an available update for installation on restart |

`/mcrt reload` reloads **MCRealTime only**.

It is not equivalent to Bukkit's global `/reload` command.

## Permissions

| Permission | Purpose | Default |
| --- | --- | --- |
| `mcrealtime.use` | Access to status and information commands | Everyone |
| `mcrealtime.admin` | Administrative commands, configuration changes, and updater operations | Operators |

Permission defaults are defined in `plugin.yml`.

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

The IANA time-zone ID used by MCRealTime.

Examples:

```text
Europe/Berlin
America/New_York
Asia/Tokyo
```

### Solar settings

#### `solar.latitude` / `solar.longitude`

Coordinates used for solar calculations.

Normally, you do not need to edit these settings manually.

Use:

```text
/mcrt location set <location>
```

MCRealTime updates the coordinates and time zone for the selected GeoNames location.

#### `solar.place.*`

Stores metadata about the selected place.

Example:

```yaml
solar:
  place:
    geoname-id: 0
    name: Berlin
    country-code: DE
```

This allows commands such as `/mcrt status` and `/mcrt location get` to display a readable place name.

### World scope

#### `global: true`

Synchronizes all loaded worlds that MCRealTime can manage.

#### `global: false`

Synchronizes only the worlds listed under `worlds`.

Example:

```yaml
global: false

worlds:
  - world
  - survival
```

Worlds without a mutable world clock are skipped.

### Synchronization

#### `synchronization.interval-ticks`

Controls how frequently MCRealTime reapplies synchronized time.

Example:

```yaml
synchronization:
  interval-ticks: 20
```

Twenty Minecraft ticks normally correspond to approximately one second.

### Behavior

#### `behavior.prevent-sleep`

Enables MCRealTime's sleep-related protection so normal sleep mechanics do not interrupt synchronized time.

#### `behavior.disable-insomnia`

Controls insomnia handling where the running server version supports the relevant gamerule.

MCRealTime detects version-dependent gamerules and restores the original state of the settings it manages when appropriate.

### Updater

Example:

```yaml
updater:
  enabled: true
  check-on-start: true
  auto-download: false
```

#### `updater.enabled`

Enables or disables the built-in updater.

#### `updater.check-on-start`

When enabled, checks GitHub Releases asynchronously at server startup. The HTTP request does not run on Minecraft's main server thread.

#### `updater.auto-download`

Controls whether MCRealTime downloads available updates automatically following a startup check.

The default is `false`: the plugin can check for updates, but does not download them without an explicit administrator command. To enable unattended download and staging, configure:

```yaml
updater:
  enabled: true
  check-on-start: true
  auto-download: true
```

If you prefer to approve downloads yourself, keep `auto-download: false` and use:

```text
/mcrt update
/mcrt update download
```

The first command checks GitHub Releases; the second downloads an available update. **Neither command requires you to replace the plugin JAR manually.**

MCRealTime validates the download, initially saves it to a temporary `.part` file, and stages the completed JAR in Bukkit's update directory. Bukkit installs the staged update when the server is restarted normally. The running plugin is never hot-swapped.

If the updater is disabled or network access to GitHub is unavailable, a manual update remains possible.

### Update source

MCRealTime is distributed through GitHub Releases and BukkitDev.

The built-in updater in MCRealTime 4.8 and later uses **GitHub Releases** as its update source.

Uploading a new version to BukkitDev does not change the new updater's behavior.

## Location data and GeoNames attribution

MCRealTime includes geographical data used for offline location search.

**Location data is based on GeoNames geographical data, licensed under Creative Commons Attribution 4.0 International (CC BY 4.0).**

GeoNames:

https://www.geonames.org/

Creative Commons Attribution 4.0 International:

https://creativecommons.org/licenses/by/4.0/

The bundled GeoNames database allows location searches without contacting an external geocoding service at runtime.

Additional license information is available in the repository's `THIRD_PARTY_NOTICES` file.

## Network access

Normal time synchronization and offline location searches do not require external web services.

The optional updater contacts GitHub Releases when enabled.

To disable updater network access:

```yaml
updater:
  enabled: false
```

## Compatibility

MCRealTime is compiled for **Java 8 bytecode**.

The Java runtime required by the Minecraft server itself depends on its Minecraft and server implementation version.

The following configurations were successfully tested during development of version 4.8:

| Server | Java runtime | Verification |
| --- | --- | --- |
| Spigot 1.8.8 | Java 8 | Tested |
| Paper 1.14.4 (git-Paper-245) | OpenJDK 8 | Tested |
| Paper 26.3 (build 140) | Temurin 25 | Tested |

Version 4.9 additionally includes automated regression tests for astronomical date calculations.

These automated tests do not replace full integration testing on every supported Minecraft server version.

MCRealTime deliberately avoids NMS and CraftBukkit internals in its core synchronization logic to maintain broad compatibility.

## Notes about Minecraft time

MCRealTime manages the time of worlds within its configured scope while synchronization is enabled.

Commands and other mechanics that change world time may have no lasting effect because MCRealTime reapplies the synchronized time.

This is intentional.

## Troubleshooting

### The configured place is not shown in `/mcrt status`

Select a place using:

```text
/mcrt location set <location>
```

Tab completion provides available results from the bundled GeoNames database.

### MCRealTime warns about its filename

Stop the server and rename the installed plugin JAR to:

```text
MCRealTime.jar
```

Do not keep an older MCRealTime JAR beside the new version.

### Update checks fail

The updater is optional.

A GitHub or network failure does not disable MCRealTime's normal synchronization functionality.

You can disable the updater:

```yaml
updater:
  enabled: false
```

### Version 4.1 does not detect newer releases

On October 1, 2026, the old API listed 4.8 and the **automatic updater in an installed MCRealTime 4.1 successfully downloaded 4.8 when enabled**. In the same test, the old manual update command had misleadingly reported "up to date" while automatic updating was disabled. If you encounter this behavior in 4.1, check its legacy automatic-updater setting and logs. Confirm that 4.8 is actually installed after a normal restart. If the legacy updater fails in your environment, use a one-time manual upgrade. Version 4.8 and later use a separate GitHub-based updater; this 4.1 observation does not apply to `/mcrt update`.

### Version 4.8 or later reports an update but does not install it

Update checks and downloads are separate by default. Run `/mcrt update download` to stage an available release, or enable `updater.auto-download: true` together with startup checks. Restart the server normally to activate an update that has been staged.

### A configuration migration occurred

Look inside:

```text
plugins/MCRealTime/
```

for a `config-legacy-v*.yml` backup containing the previous configuration.

## Source code and issue reports

Source code:

https://github.com/AppleCrafting/MCRealTime

GitHub Releases:

https://github.com/AppleCrafting/MCRealTime/releases

Issue tracker:

https://github.com/AppleCrafting/MCRealTime/issues

BukkitDev:

https://dev.bukkit.org/projects/mcrealtime

Please use the GitHub issue tracker for bug reports and development ideas whenever possible.

## Credits

MCRealTime is developed and maintained by Gabriel / Sapentiae.

Thanks to everyone who has tested MCRealTime, reported problems, suggested improvements, or used the plugin over the years.

Special thanks to the GeoNames project and its contributors for the geographical data used by MCRealTime's location search.

## License

MCRealTime's software is licensed under the MIT License.

See the repository's `LICENSE` file for the complete license text.

GeoNames-derived geographical data is separately licensed under Creative Commons Attribution 4.0 International (CC BY 4.0).

See `THIRD_PARTY_NOTICES` for additional information.

---

I hope you enjoy MCRealTime.

— Gabriel / Sapentiae