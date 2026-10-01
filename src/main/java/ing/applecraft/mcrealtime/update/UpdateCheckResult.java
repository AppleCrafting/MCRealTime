package ing.applecraft.mcrealtime.update;

public final class UpdateCheckResult {

    private final String currentVersion;
    private final GitHubRelease latestRelease;
    private final boolean updateAvailable;

    public UpdateCheckResult(
            String currentVersion,
            GitHubRelease latestRelease,
            boolean updateAvailable) {

        if (currentVersion == null
                || currentVersion.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "currentVersion must not be empty"
            );
        }

        if (latestRelease == null) {
            throw new IllegalArgumentException(
                    "latestRelease must not be null"
            );
        }

        this.currentVersion =
                currentVersion;

        this.latestRelease =
                latestRelease;

        this.updateAvailable =
                updateAvailable;
    }

    public String getCurrentVersion() {
        return currentVersion;
    }

    public GitHubRelease getLatestRelease() {
        return latestRelease;
    }

    public boolean isUpdateAvailable() {
        return updateAvailable;
    }
}
