package ing.applecraft.mcrealtime.update;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public final class GitHubReleaseClient
        implements ReleaseProvider {

    private static final String LATEST_RELEASE_URL =
            "https://api.github.com/repos/"
                    + "AppleCrafting/MCRealTime/"
                    + "releases/latest";

    private static final int CONNECT_TIMEOUT_MILLIS =
            5000;

    private static final int READ_TIMEOUT_MILLIS =
            5000;

    private final GitHubReleaseParser parser;

    public GitHubReleaseClient() {
        this.parser =
                new GitHubReleaseParser();
    }

    @Override
    public GitHubRelease fetchLatestRelease()
            throws IOException {

        HttpURLConnection connection =
                openConnection();

        try {

            int responseCode =
                    connection.getResponseCode();

            /*
             * GitHub's /releases/latest endpoint returns
             * HTTP 404 when there is currently no published
             * full release available.
             *
             * This is not necessarily an updater failure.
             */
            if (responseCode
                    == HttpURLConnection.HTTP_NOT_FOUND) {

                throw new NoPublishedReleaseException();
            }

            if (responseCode
                    != HttpURLConnection.HTTP_OK) {

                throw new IOException(
                        "GitHub returned HTTP "
                                + responseCode
                                + "."
                );
            }

            String response =
                    readResponse(
                            connection.getInputStream()
                    );

            return parser.parse(
                    response
            );

        } finally {

            connection.disconnect();
        }
    }

    private HttpURLConnection openConnection()
            throws IOException {

        URL url =
                new URL(
                        LATEST_RELEASE_URL
                );

        HttpURLConnection connection =
                (HttpURLConnection)
                        url.openConnection();

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
                "Accept",
                "application/vnd.github+json"
        );

        connection.setRequestProperty(
                "User-Agent",
                "MCRealTime-Updater"
        );

        connection.setRequestProperty(
                "X-GitHub-Api-Version",
                "2026-03-10"
        );

        connection.setUseCaches(
                false
        );

        return connection;
    }

    private String readResponse(
            InputStream inputStream)
            throws IOException {

        StringBuilder builder =
                new StringBuilder();

        try (BufferedReader reader =
                     new BufferedReader(
                             new InputStreamReader(
                                     inputStream,
                                     StandardCharsets.UTF_8
                             )
                     )) {

            String line;

            while ((line = reader.readLine())
                    != null) {

                builder.append(line);
            }
        }

        return builder.toString();
    }
}
