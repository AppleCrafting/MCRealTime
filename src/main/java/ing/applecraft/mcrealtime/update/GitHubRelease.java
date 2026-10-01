package ing.applecraft.mcrealtime.update;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class GitHubRelease {

    private final String tagName;
    private final List<GitHubReleaseAsset> assets;

    /**
     * Kept for existing tests and places that only
     * need a release version.
     */
    public GitHubRelease(
            String tagName) {

        this(
                tagName,
                Collections.<GitHubReleaseAsset>emptyList()
        );
    }

    public GitHubRelease(
            String tagName,
            List<GitHubReleaseAsset> assets) {

        if (tagName == null
                || tagName.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "tagName must not be empty"
            );
        }

        if (assets == null) {
            throw new IllegalArgumentException(
                    "assets must not be null"
            );
        }

        this.tagName =
                tagName;

        this.assets =
                Collections.unmodifiableList(
                        new ArrayList<GitHubReleaseAsset>(
                                assets
                        )
                );
    }

    public String getTagName() {
        return tagName;
    }

    public List<GitHubReleaseAsset> getAssets() {
        return assets;
    }

    /**
     * Finds the normal MCRealTime plugin JAR.
     *
     * Source and Javadoc JARs are deliberately ignored.
     */
    public Optional<GitHubReleaseAsset>
    findPluginJarAsset() {

        for (GitHubReleaseAsset asset : assets) {

            String fileName =
                    asset.getFileName()
                            .toLowerCase(Locale.ROOT);

            if (!asset.isJar()) {
                continue;
            }

            if (!fileName.startsWith("mcrealtime")) {
                continue;
            }

            if (fileName.contains("sources")
                    || fileName.contains("javadoc")) {

                continue;
            }

            return Optional.of(
                    asset
            );
        }

        return Optional.empty();
    }
}
