package ing.applecraft.mcrealtime.update;

import org.bukkit.configuration.file.FileConfiguration;

public final class UpdaterSettingsLoader {

    public UpdaterSettings load(
            FileConfiguration config) {

        if (config == null) {
            throw new IllegalArgumentException(
                    "config must not be null"
            );
        }

        boolean enabled =
                config.getBoolean(
                        "updater.enabled",
                        true
                );

        boolean checkOnStart =
                config.getBoolean(
                        "updater.check-on-start",
                        true
                );

        boolean autoDownload =
                config.getBoolean(
                        "updater.auto-download",
                        false
                );

        return new UpdaterSettings(
                enabled,
                checkOnStart,
                autoDownload
        );
    }
}
