package ing.applecraft.mcrealtime.update;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public final class PluginVersionTest {

    @Test
    public void comparesMinorVersions() {

        PluginVersion oldVersion =
                PluginVersion.parse("4.7");

        PluginVersion newVersion =
                PluginVersion.parse("4.8");

        assertTrue(
                oldVersion.compareTo(newVersion) < 0
        );
    }

    @Test
    public void comparesPatchVersions() {

        PluginVersion oldVersion =
                PluginVersion.parse("4.8");

        PluginVersion newVersion =
                PluginVersion.parse("4.8.1");

        assertTrue(
                oldVersion.compareTo(newVersion) < 0
        );
    }

    @Test
    public void treatsMissingPatchAsZero() {

        PluginVersion first =
                PluginVersion.parse("4.8");

        PluginVersion second =
                PluginVersion.parse("4.8.0");

        assertEquals(
                0,
                first.compareTo(second)
        );
    }

    @Test
    public void acceptsGitHubVPrefix() {

        PluginVersion first =
                PluginVersion.parse("v4.8");

        PluginVersion second =
                PluginVersion.parse("4.8");

        assertEquals(
                0,
                first.compareTo(second)
        );
    }

    @Test
    public void stableReleaseIsNewerThanSnapshot() {

        PluginVersion snapshot =
                PluginVersion.parse(
                        "4.8-SNAPSHOT"
                );

        PluginVersion release =
                PluginVersion.parse(
                        "4.8"
                );

        assertTrue(
                snapshot.compareTo(release) < 0
        );
    }

    @Test
    public void newerDevelopmentVersionDoesNotDowngrade() {

        PluginVersion development =
                PluginVersion.parse(
                        "4.9-SNAPSHOT"
                );

        PluginVersion release =
                PluginVersion.parse(
                        "4.8"
                );

        assertTrue(
                development.compareTo(release) > 0
        );
    }

    @Test
    public void releaseCandidateIsNewerThanBeta() {

        PluginVersion beta =
                PluginVersion.parse(
                        "4.8-beta"
                );

        PluginVersion releaseCandidate =
                PluginVersion.parse(
                        "4.8-rc"
                );

        assertTrue(
                beta.compareTo(releaseCandidate) < 0
        );
    }
}
