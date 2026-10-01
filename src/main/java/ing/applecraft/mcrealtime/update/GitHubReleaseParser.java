package ing.applecraft.mcrealtime.update;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GitHubReleaseParser {

    private static final Pattern TAG_NAME_PATTERN =
            Pattern.compile(
                    "\"tag_name\"\\s*:\\s*\"([^\"]+)\""
            );

    private static final Pattern DOWNLOAD_URL_PATTERN =
            Pattern.compile(
                    "\"browser_download_url\""
                            + "\\s*:\\s*"
                            + "\"([^\"]+)\""
            );

    public GitHubRelease parse(
            String json)
            throws IOException {

        if (json == null
                || json.trim().isEmpty()) {

            throw new IOException(
                    "GitHub returned an empty response."
            );
        }

        Matcher tagMatcher =
                TAG_NAME_PATTERN.matcher(
                        json
                );

        if (!tagMatcher.find()) {

            throw new IOException(
                    "GitHub release response "
                            + "does not contain tag_name."
            );
        }

        String tagName =
                tagMatcher.group(1);

        List<GitHubReleaseAsset> assets =
                parseAssets(
                        json
                );

        return new GitHubRelease(
                tagName,
                assets
        );
    }

    private List<GitHubReleaseAsset> parseAssets(
            String json) {

        List<GitHubReleaseAsset> assets =
                new ArrayList<GitHubReleaseAsset>();

        Matcher matcher =
                DOWNLOAD_URL_PATTERN.matcher(
                        json
                );

        while (matcher.find()) {

            String downloadUrl =
                    matcher.group(1)
                            .replace(
                                    "\\/",
                                    "/"
                            );

            assets.add(
                    new GitHubReleaseAsset(
                            downloadUrl
                    )
            );
        }

        return assets;
    }
}
