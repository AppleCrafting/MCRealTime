package ing.applecraft.mcrealtime.update;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public final class HttpDownloadTransport
        implements DownloadTransport {

    private static final int CONNECT_TIMEOUT_MILLIS =
            10000;

    private static final int READ_TIMEOUT_MILLIS =
            10000;

    /*
     * Generous safety limit.
     *
     * MCRealTime should be far smaller than this,
     * even with the bundled GeoNames database.
     */
    private static final long MAX_DOWNLOAD_SIZE =
            64L * 1024L * 1024L;

    @Override
    public void download(
            String sourceUrl,
            File destination)
            throws IOException {

        if (sourceUrl == null
                || sourceUrl.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "sourceUrl must not be empty"
            );
        }

        if (destination == null) {

            throw new IllegalArgumentException(
                    "destination must not be null"
            );
        }

        URL url =
                new URL(sourceUrl);

        if (!"https".equalsIgnoreCase(
                url.getProtocol())) {

            throw new IOException(
                    "Update downloads must use HTTPS."
            );
        }

        HttpURLConnection connection =
                (HttpURLConnection)
                        url.openConnection();

        try {

            connection.setRequestMethod(
                    "GET"
            );

            connection.setConnectTimeout(
                    CONNECT_TIMEOUT_MILLIS
            );

            connection.setReadTimeout(
                    READ_TIMEOUT_MILLIS
            );

            connection.setRequestProperty(
                    "User-Agent",
                    "MCRealTime-Updater"
            );

            connection.setUseCaches(
                    false
            );

            /*
             * GitHub's browser_download_url may redirect
             * to GitHub's release asset infrastructure.
             */
            connection.setInstanceFollowRedirects(
                    true
            );

            int responseCode =
                    connection.getResponseCode();

            if (responseCode
                    != HttpURLConnection.HTTP_OK) {

                throw new IOException(
                        "Download server returned HTTP "
                                + responseCode
                                + "."
                );
            }

            long contentLength =
                    connection.getContentLengthLong();

            if (contentLength > MAX_DOWNLOAD_SIZE) {

                throw new IOException(
                        "Update file is unexpectedly large: "
                                + contentLength
                                + " bytes."
                );
            }

            copyResponse(
                    connection.getInputStream(),
                    destination
            );

        } finally {

            connection.disconnect();
        }
    }

    private void copyResponse(
            InputStream inputStream,
            File destination)
            throws IOException {

        long totalBytes =
                0L;

        byte[] buffer =
                new byte[8192];

        try (
                BufferedInputStream input =
                        new BufferedInputStream(
                                inputStream
                        );

                BufferedOutputStream output =
                        new BufferedOutputStream(
                                new FileOutputStream(
                                        destination
                                )
                        )
        ) {

            int read;

            while ((read = input.read(buffer))
                    != -1) {

                totalBytes += read;

                if (totalBytes
                        > MAX_DOWNLOAD_SIZE) {

                    throw new IOException(
                            "Update file exceeded "
                                    + "the maximum allowed size."
                    );
                }

                output.write(
                        buffer,
                        0,
                        read
                );
            }
        }

        if (totalBytes == 0L) {

            throw new IOException(
                    "Downloaded update file is empty."
            );
        }
    }
}
