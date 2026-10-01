package ing.applecraft.mcrealtime.update;

import java.util.Locale;

public final class GitHubReleaseAsset {

    private final String browserDownloadUrl;

    public GitHubReleaseAsset(
            String browserDownloadUrl) {

        if (browserDownloadUrl == null
                || browserDownloadUrl.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "browserDownloadUrl must not be empty"
            );
        }

        this.browserDownloadUrl =
                browserDownloadUrl;
    }

    public String getBrowserDownloadUrl() {
        return browserDownloadUrl;
    }

    public String getFileName() {

        int separator =
                browserDownloadUrl.lastIndexOf('/');

        if (separator < 0
                || separator
                == browserDownloadUrl.length() - 1) {

            return browserDownloadUrl;
        }

        return browserDownloadUrl.substring(
                separator + 1
        );
    }

    public boolean isJar() {

        return getFileName()
                .toLowerCase(Locale.ROOT)
                .endsWith(".jar");
    }

    @Override
    public String toString() {
        return getFileName();
    }
}
