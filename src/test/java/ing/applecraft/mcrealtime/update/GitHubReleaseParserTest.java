package ing.applecraft.mcrealtime.update;

import org.junit.Test;

import java.io.IOException;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class GitHubReleaseParserTest {

    private final GitHubReleaseParser parser =
            new GitHubReleaseParser();

    @Test
    public void parsesTagName()
            throws IOException {

        String json =
                "{"
                        + "\"id\":123,"
                        + "\"tag_name\":\"v4.8\","
                        + "\"name\":\"MCRealTime 4.8\""
                        + "}";

        GitHubRelease release =
                parser.parse(json);

        assertEquals(
                "v4.8",
                release.getTagName()
        );
    }

    @Test
    public void parsesReleaseAssets()
            throws IOException {

        String json =
                "{"
                        + "\"tag_name\":\"v4.8\","
                        + "\"assets\":["
                        + "{"
                        + "\"browser_download_url\":"
                        + "\"https://github.com/"
                        + "AppleCrafting/MCRealTime/"
                        + "releases/download/v4.8/"
                        + "MCRealTime-4.8.jar\""
                        + "},"
                        + "{"
                        + "\"browser_download_url\":"
                        + "\"https://github.com/"
                        + "AppleCrafting/MCRealTime/"
                        + "releases/download/v4.8/"
                        + "MCRealTime-4.8.zip\""
                        + "}"
                        + "]"
                        + "}";

        GitHubRelease release =
                parser.parse(json);

        assertEquals(
                2,
                release.getAssets().size()
        );

        assertEquals(
                "MCRealTime-4.8.jar",
                release
                        .getAssets()
                        .get(0)
                        .getFileName()
        );
    }

    @Test
    public void findsPluginJar()
            throws IOException {

        String json =
                "{"
                        + "\"tag_name\":\"v4.8\","
                        + "\"assets\":["
                        + "{"
                        + "\"browser_download_url\":"
                        + "\"https://github.com/"
                        + "AppleCrafting/MCRealTime/"
                        + "releases/download/v4.8/"
                        + "MCRealTime-4.8-sources.jar\""
                        + "},"
                        + "{"
                        + "\"browser_download_url\":"
                        + "\"https://github.com/"
                        + "AppleCrafting/MCRealTime/"
                        + "releases/download/v4.8/"
                        + "MCRealTime-4.8.jar\""
                        + "}"
                        + "]"
                        + "}";

        GitHubRelease release =
                parser.parse(json);

        Optional<GitHubReleaseAsset> asset =
                release.findPluginJarAsset();

        assertTrue(
                asset.isPresent()
        );

        assertEquals(
                "MCRealTime-4.8.jar",
                asset.get().getFileName()
        );
    }

    @Test
    public void reportsMissingPluginJar()
            throws IOException {

        String json =
                "{"
                        + "\"tag_name\":\"v4.8\","
                        + "\"assets\":["
                        + "{"
                        + "\"browser_download_url\":"
                        + "\"https://github.com/"
                        + "AppleCrafting/MCRealTime/"
                        + "releases/download/v4.8/"
                        + "README.txt\""
                        + "}"
                        + "]"
                        + "}";

        GitHubRelease release =
                parser.parse(json);

        assertFalse(
                release
                        .findPluginJarAsset()
                        .isPresent()
        );
    }

    @Test(expected = IOException.class)
    public void rejectsMissingTagName()
            throws IOException {

        parser.parse(
                "{\"id\":123}"
        );
    }

    @Test(expected = IOException.class)
    public void rejectsEmptyResponse()
            throws IOException {

        parser.parse("");
    }
}
