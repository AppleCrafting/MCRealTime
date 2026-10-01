package ing.applecraft.mcrealtime.update;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class UpdaterSettingsLoaderTest {

    private final UpdaterSettingsLoader loader =
            new UpdaterSettingsLoader();

    @Test
    public void loadsConfiguredValues() {

        YamlConfiguration config =
                new YamlConfiguration();

        config.set(
                "updater.enabled",
                true
        );

        config.set(
                "updater.check-on-start",
                false
        );

        config.set(
                "updater.auto-download",
                true
        );

        UpdaterSettings settings =
                loader.load(config);

        assertTrue(
                settings.isEnabled()
        );

        assertFalse(
                settings.isCheckOnStart()
        );

        assertTrue(
                settings.isAutoDownload()
        );
    }

    @Test
    public void usesSafeDefaults() {

        YamlConfiguration config =
                new YamlConfiguration();

        UpdaterSettings settings =
                loader.load(config);

        assertTrue(
                settings.isEnabled()
        );

        assertTrue(
                settings.isCheckOnStart()
        );

        assertFalse(
                settings.isAutoDownload()
        );
    }
}
