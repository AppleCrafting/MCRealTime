package ing.applecraft.mcrealtime.update;

import java.io.IOException;

public final class NoPublishedReleaseException
        extends IOException {

    public NoPublishedReleaseException() {
        super(
                "No published GitHub release is available."
        );
    }
}
