package ing.applecraft.mcrealtime.update;

import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class UpdateDownloaderTest {

    @Test
    public void downloadsValidPluginJar()
            throws IOException {

        File testDirectory =
                Files.createTempDirectory(
                        "mcrealtime-downloader-test"
                ).toFile();

        try {

            File sourceJar =
                    new File(
                            testDirectory,
                            "source.jar"
                    );

            createPluginJar(
                    sourceJar
            );

            File updateDirectory =
                    new File(
                            testDirectory,
                            "update"
                    );

            UpdateDownloader downloader =
                    new UpdateDownloader(
                            new CopyTransport(
                                    sourceJar
                            )
                    );

            GitHubReleaseAsset asset =
                    new GitHubReleaseAsset(
                            "https://github.com/"
                                    + "AppleCrafting/"
                                    + "MCRealTime/"
                                    + "releases/download/"
                                    + "v4.8/"
                                    + "MCRealTime-4.8.jar"
                    );

            File downloaded =
                    downloader.download(
                            asset,
                            updateDirectory,
                            "MCRealTime.jar"
                    );

            assertTrue(
                    downloaded.isFile()
            );

            assertTrue(
                    downloaded.length() > 0L
            );

            assertFalse(
                    new File(
                            updateDirectory,
                            "MCRealTime.jar.part"
                    ).exists()
            );

        } finally {

            deleteRecursively(
                    testDirectory
            );
        }
    }

    @Test
    public void rejectsInvalidJar()
            throws IOException {

        File testDirectory =
                Files.createTempDirectory(
                        "mcrealtime-downloader-test"
                ).toFile();

        try {

            File invalidSource =
                    new File(
                            testDirectory,
                            "invalid.jar"
                    );

            Files.write(
                    invalidSource.toPath(),
                    "This is not a JAR."
                            .getBytes(
                                    StandardCharsets.UTF_8
                            )
            );

            File updateDirectory =
                    new File(
                            testDirectory,
                            "update"
                    );

            UpdateDownloader downloader =
                    new UpdateDownloader(
                            new CopyTransport(
                                    invalidSource
                            )
                    );

            GitHubReleaseAsset asset =
                    new GitHubReleaseAsset(
                            "https://github.com/"
                                    + "AppleCrafting/"
                                    + "MCRealTime/"
                                    + "releases/download/"
                                    + "v4.8/"
                                    + "MCRealTime-4.8.jar"
                    );

            boolean failed =
                    false;

            try {

                downloader.download(
                        asset,
                        updateDirectory,
                        "MCRealTime.jar"
                );

            } catch (IOException exception) {

                failed =
                        true;
            }

            assertTrue(
                    failed
            );

            assertFalse(
                    new File(
                            updateDirectory,
                            "MCRealTime.jar"
                    ).exists()
            );

            assertFalse(
                    new File(
                            updateDirectory,
                            "MCRealTime.jar.part"
                    ).exists()
            );

        } finally {

            deleteRecursively(
                    testDirectory
            );
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsPathTraversal()
            throws IOException {

        File testDirectory =
                Files.createTempDirectory(
                        "mcrealtime-downloader-test"
                ).toFile();

        try {

            File sourceJar =
                    new File(
                            testDirectory,
                            "source.jar"
                    );

            createPluginJar(
                    sourceJar
            );

            UpdateDownloader downloader =
                    new UpdateDownloader(
                            new CopyTransport(
                                    sourceJar
                            )
                    );

            downloader.download(
                    new GitHubReleaseAsset(
                            "https://github.com/"
                                    + "AppleCrafting/"
                                    + "MCRealTime/"
                                    + "releases/download/"
                                    + "v4.8/"
                                    + "MCRealTime-4.8.jar"
                    ),
                    new File(
                            testDirectory,
                            "update"
                    ),
                    "../MCRealTime.jar"
            );

        } finally {

            deleteRecursively(
                    testDirectory
            );
        }
    }

    private static void createPluginJar(
            File file)
            throws IOException {

        try (JarOutputStream output =
                     new JarOutputStream(
                             new FileOutputStream(
                                     file
                             )
                     )) {

            output.putNextEntry(
                    new JarEntry(
                            "plugin.yml"
                    )
            );

            output.write(
                    (
                            "name: MCRealTime\n"
                                    + "version: 4.8\n"
                                    + "main: "
                                    + "ing.applecraft.mcrealtime."
                                    + "MCRealTimePlugin\n"
                    ).getBytes(
                            StandardCharsets.UTF_8
                    )
            );

            output.closeEntry();
        }
    }

    private static void deleteRecursively(
            File file) {

        if (file == null
                || !file.exists()) {

            return;
        }

        if (file.isDirectory()) {

            File[] children =
                    file.listFiles();

            if (children != null) {

                for (File child : children) {

                    deleteRecursively(
                            child
                    );
                }
            }
        }

        file.delete();
    }

    private static final class CopyTransport
            implements DownloadTransport {

        private final File source;

        private CopyTransport(
                File source) {

            this.source =
                    source;
        }

        @Override
        public void download(
                String sourceUrl,
                File destination)
                throws IOException {

            Files.copy(
                    source.toPath(),
                    destination.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
            );
        }
    }
}
