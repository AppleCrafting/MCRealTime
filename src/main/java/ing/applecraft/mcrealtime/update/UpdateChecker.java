package ing.applecraft.mcrealtime.update;

public final class UpdateChecker {

    /**
     * Returns true when the available version is newer
     * than the currently installed version.
     */
    public boolean isUpdateAvailable(
            String currentVersion,
            String availableVersion) {

        PluginVersion current =
                PluginVersion.parse(
                        currentVersion
                );

        PluginVersion available =
                PluginVersion.parse(
                        availableVersion
                );

        return available.compareTo(current) > 0;
    }
}
