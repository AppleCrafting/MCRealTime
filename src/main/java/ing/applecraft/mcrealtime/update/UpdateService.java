package ing.applecraft.mcrealtime.update;

import java.io.IOException;

public final class UpdateService {

    private final ReleaseProvider releaseProvider;
    private final UpdateChecker updateChecker;

    public UpdateService(
            ReleaseProvider releaseProvider,
            UpdateChecker updateChecker) {

        if (releaseProvider == null) {
            throw new IllegalArgumentException(
                    "releaseProvider must not be null"
            );
        }

        if (updateChecker == null) {
            throw new IllegalArgumentException(
                    "updateChecker must not be null"
            );
        }

        this.releaseProvider =
                releaseProvider;

        this.updateChecker =
                updateChecker;
    }

    /**
     * Checks whether a newer published version exists.
     *
     * This method may perform network I/O depending
     * on the configured ReleaseProvider.
     *
     * Therefore it must not be called directly from
     * Minecraft's main server thread when using the
     * GitHubReleaseClient.
     */
    public UpdateCheckResult check(
            String currentVersion)
            throws IOException {

        if (currentVersion == null
                || currentVersion.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "currentVersion must not be empty"
            );
        }

        GitHubRelease latestRelease =
                releaseProvider
                        .fetchLatestRelease();

        boolean updateAvailable =
                updateChecker
                        .isUpdateAvailable(
                                currentVersion,
                                latestRelease.getTagName()
                        );

        return new UpdateCheckResult(
                currentVersion,
                latestRelease,
                updateAvailable
        );
    }
}
