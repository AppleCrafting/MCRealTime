# Changelog

## 4.8

MCRealTime 4.8 is a major rewrite of the plugin with a new architecture,
location-based solar time, safer configuration handling, and a modern
GitHub-based updater.

### Added

- Added `CLOCK` and `SOLAR` time synchronization modes.
- Added real solar-day synchronization based on sunrise, solar noon,
  sunset, and the following sunrise.
- Added location search using bundled GeoNames data.
- Added `/mcrt location get`.
- Added `/mcrt location set <location>` with tab completion.
- Added `/mcrt mode get`.
- Added `/mcrt mode set <clock|solar>`.
- Added `/mcrt status` and `/mcrt info`.
- Added `/mcrt reload` for MCRealTime-only configuration/runtime reloads.
- Added `/mcrt update`.
- Added `/mcrt update download`.
- Added asynchronous GitHub release checks.
- Added optional automatic update downloading.
- Added safe staged downloads using temporary `.part` files.
- Added validation of downloaded update JARs before installation.
- Added configuration schema versioning.
- Added automatic configuration migration with full legacy backups.
- Added readable place information to status output.
- Added detection of non-standard plugin JAR filenames.
- Added guidance to rename the installed plugin to `MCRealTime.jar`.
- Added Java 8 compatible modern `java.time` based time handling.

### Changed

- Rewritten large parts of the original plugin architecture.
- Replaced the old static-heavy design with separated runtime,
  configuration, time-provider, location, and updater components.
- Replaced the old time calculation logic.
- Reduced unnecessary scheduling work.
- Reworked world synchronization behavior.
- Reworked sleep and insomnia handling.
- Reworked update handling to use GitHub Releases instead of the old
  updater implementation.
- The recommended installed plugin filename is now `MCRealTime.jar`.
- `/mcrt reload` now reloads only MCRealTime and does not rely on Bukkit's
  global reload mechanism.

### Removed

- Removed the legacy updater implementation and replaced it with the new
  GitHub Releases based updater.
- Removed the old monolithic, static-heavy runtime design from the rewritten
  core.
- Removed the legacy `Date`, `SimpleDateFormat`, and string-based time
  calculation approach in favor of `java.time`.
- Removed unnecessary every-tick time synchronization; synchronization now
  follows the configured interval.
- Removed unsafe assumptions that commands are always executed by players;
  MCRealTime commands now also support the server console correctly.
- Removed hardcoded gamerule reset behavior. MCRealTime now restores world
  state it actually owns instead of forcing predefined values.
- Removed obsolete configuration values from the active configuration schema
  when migrating older configurations. Their original values are preserved
  in the generated `config-legacy-v*.yml` archive.
- Removed dependence on Bukkit's global `/reload` mechanism for MCRealTime
  configuration changes.

### Fixed

- Fixed inaccurate minute/second conversion from the old implementation.
- Fixed unsafe command handling that could fail when used from the console.
- Fixed unsafe world lookups.
- Fixed configuration migration incorrectly seeing bundled defaults as
  values from an old on-disk configuration.
- Fixed the updater performing network work in a way that could affect the
  Minecraft main thread.
- Fixed update result duplication when `/mcrt update` was executed from the
  server console.
- Fixed compatibility with modern Paper world clocks and renamed game rules.
  MCRealTime now supports both modern `advance_time` / `spawn_phantoms`
  and legacy `doDaylightCycle` / `doInsomnia` rule names while preserving
  compatibility with older Bukkit versions.
- Fixed time synchronization on modern Paper dimensions without a mutable
  world clock. Unsupported dimensions are detected and skipped safely.

### Compatibility

MCRealTime 4.8 is compiled for Java 8 bytecode.

Verified during development:

- Bukkit/Spigot 1.8.8
- Paper 1.14.4

A current Paper release will also be tested before the final 4.8 release.

### Third-party data

Location data is based on GeoNames geographical data and is licensed under
CC BY 4.0.

See `THIRD_PARTY_NOTICES` for details.