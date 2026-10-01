package ing.applecraft.mcrealtime.update;

import java.io.IOException;

public interface ReleaseProvider {

    GitHubRelease fetchLatestRelease()
            throws IOException;
}
