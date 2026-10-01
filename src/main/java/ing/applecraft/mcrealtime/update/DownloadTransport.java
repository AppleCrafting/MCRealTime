package ing.applecraft.mcrealtime.update;

import java.io.File;
import java.io.IOException;

public interface DownloadTransport {

    void download(
            String sourceUrl,
            File destination)
            throws IOException;
}
