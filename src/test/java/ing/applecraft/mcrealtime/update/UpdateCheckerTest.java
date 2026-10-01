package ing.applecraft.mcrealtime.update;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class UpdateCheckerTest {

    private final UpdateChecker checker =
            new UpdateChecker();

    @Test
    public void detectsNewerMinorVersion() {

        assertTrue(
                checker.isUpdateAvailable(
                        "4.7",
                        "4.8"
                )
        );
    }

    @Test
    public void detectsNewerPatchVersion() {

        assertTrue(
                checker.isUpdateAvailable(
                        "4.8",
                        "4.8.1"
                )
        );
    }

    @Test
    public void acceptsGitHubTagPrefix() {

        assertTrue(
                checker.isUpdateAvailable(
                        "4.7",
                        "v4.8"
                )
        );
    }

    @Test
    public void sameVersionIsNotAnUpdate() {

        assertFalse(
                checker.isUpdateAvailable(
                        "4.8",
                        "v4.8"
                )
        );
    }

    @Test
    public void olderReleaseIsNotAnUpdate() {

        assertFalse(
                checker.isUpdateAvailable(
                        "4.9",
                        "4.8"
                )
        );
    }

    @Test
    public void stableReleaseUpdatesSnapshot() {

        assertTrue(
                checker.isUpdateAvailable(
                        "4.8-SNAPSHOT",
                        "4.8"
                )
        );
    }

    @Test
    public void olderStableDoesNotDowngradeNewerSnapshot() {

        assertFalse(
                checker.isUpdateAvailable(
                        "4.9-SNAPSHOT",
                        "4.8"
                )
        );
    }
}
