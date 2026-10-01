package ing.applecraft.mcrealtime.update;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public final class UpdateServiceTest {

    @Test
    public void detectsAvailableUpdate()
            throws IOException {

        ReleaseProvider provider =
                new FixedReleaseProvider(
                        "v4.8"
                );

        UpdateService service =
                new UpdateService(
                        provider,
                        new UpdateChecker()
                );

        UpdateCheckResult result =
                service.check(
                        "4.7"
                );

        assertTrue(
                result.isUpdateAvailable()
        );

        assertEquals(
                "4.7",
                result.getCurrentVersion()
        );

        assertEquals(
                "v4.8",
                result
                        .getLatestRelease()
                        .getTagName()
        );
    }

    @Test
    public void reportsCurrentVersionAsUpToDate()
            throws IOException {

        ReleaseProvider provider =
                new FixedReleaseProvider(
                        "v4.8"
                );

        UpdateService service =
                new UpdateService(
                        provider,
                        new UpdateChecker()
                );

        UpdateCheckResult result =
                service.check(
                        "4.8"
                );

        assertFalse(
                result.isUpdateAvailable()
        );
    }

    @Test
    public void doesNotDowngradeDevelopmentVersion()
            throws IOException {

        ReleaseProvider provider =
                new FixedReleaseProvider(
                        "v4.8"
                );

        UpdateService service =
                new UpdateService(
                        provider,
                        new UpdateChecker()
                );

        UpdateCheckResult result =
                service.check(
                        "4.9-SNAPSHOT"
                );

        assertFalse(
                result.isUpdateAvailable()
        );
    }

    @Test(expected = IOException.class)
    public void propagatesProviderFailure()
            throws IOException {

        ReleaseProvider provider =
                new FailingReleaseProvider();

        UpdateService service =
                new UpdateService(
                        provider,
                        new UpdateChecker()
                );

        service.check(
                "4.8"
        );
    }

    private static final class FixedReleaseProvider
            implements ReleaseProvider {

        private final String tagName;

        private FixedReleaseProvider(
                String tagName) {

            this.tagName =
                    tagName;
        }

        @Override
        public GitHubRelease fetchLatestRelease() {

            return new GitHubRelease(
                    tagName
            );
        }
    }

    private static final class FailingReleaseProvider
            implements ReleaseProvider {

        @Override
        public GitHubRelease fetchLatestRelease()
                throws IOException {

            throw new IOException(
                    "Simulated network failure"
            );
        }
    }
}
