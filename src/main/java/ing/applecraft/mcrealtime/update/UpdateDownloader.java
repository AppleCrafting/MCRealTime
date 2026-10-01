package ing.applecraft.mcrealtime.update;

import java.io.File;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.jar.JarFile;

public final class UpdateDownloader {

    private final DownloadTransport transport;

    public UpdateDownloader(
            DownloadTransport transport) {

        if (transport == null) {

            throw new IllegalArgumentException(
                    "transport must not be null"
            );
        }

        this.transport =
                transport;
    }

    /**
     * Downloads a release asset safely.
     *
     * The download first goes into a temporary
     * .part file. Only after the JAR has been
     * validated is it moved to its final name.
     */
    public File download(
            GitHubReleaseAsset asset,
            File targetDirectory,
            String targetFileName)
            throws IOException {

        if (asset == null) {

            throw new IllegalArgumentException(
                    "asset must not be null"
            );
        }

        if (targetDirectory == null) {

            throw new IllegalArgumentException(
                    "targetDirectory must not be null"
            );
        }

        validateTargetFileName(
                targetFileName
        );

        ensureTargetDirectory(
                targetDirectory
        );

        File destination =
                new File(
                        targetDirectory,
                        targetFileName
                );

        File temporaryFile =
                new File(
                        targetDirectory,
                        targetFileName + ".part"
                );

        prepareTemporaryFile(
                temporaryFile
        );

        try {

            transport.download(
                    asset.getBrowserDownloadUrl(),
                    temporaryFile
            );

            validatePluginJar(
                    temporaryFile
            );

            moveIntoPlace(
                    temporaryFile,
                    destination
            );

            return destination;

        } catch (IOException exception) {

            deleteTemporaryFile(
                    temporaryFile
            );

            throw exception;

        } catch (RuntimeException exception) {

            deleteTemporaryFile(
                    temporaryFile
            );

            throw exception;
        }
    }

    private void validateTargetFileName(
            String targetFileName) {

        if (targetFileName == null
                || targetFileName.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "targetFileName must not be empty"
            );
        }

        if (targetFileName.contains("/")
                || targetFileName.contains("\\")
                || targetFileName.equals(".")
                || targetFileName.equals("..")) {

            throw new IllegalArgumentException(
                    "targetFileName must be a simple file name"
            );
        }

        if (!targetFileName
                .toLowerCase(Locale.ROOT)
                .endsWith(".jar")) {

            throw new IllegalArgumentException(
                    "targetFileName must end with .jar"
            );
        }
    }

    private void ensureTargetDirectory(
            File targetDirectory)
            throws IOException {

        if (targetDirectory.isDirectory()) {
            return;
        }

        if (targetDirectory.exists()) {

            throw new IOException(
                    "Update target exists but "
                            + "is not a directory: "
                            + targetDirectory
                            .getAbsolutePath()
            );
        }

        if (!targetDirectory.mkdirs()
                && !targetDirectory.isDirectory()) {

            throw new IOException(
                    "Could not create update directory: "
                            + targetDirectory
                            .getAbsolutePath()
            );
        }
    }

    private void prepareTemporaryFile(
            File temporaryFile)
            throws IOException {

        if (!temporaryFile.exists()) {
            return;
        }

        if (!temporaryFile.delete()) {

            throw new IOException(
                    "Could not remove old temporary "
                            + "update file: "
                            + temporaryFile
                            .getAbsolutePath()
            );
        }
    }

    /**
     * A successful HTTP request alone is not enough.
     *
     * The result must actually be a readable JAR and
     * contain plugin.yml before it may be staged as
     * an MCRealTime update.
     */
    private void validatePluginJar(
            File file)
            throws IOException {

        if (!file.isFile()
                || file.length() == 0L) {

            throw new IOException(
                    "Downloaded update is empty."
            );
        }

        try (JarFile jarFile =
                     new JarFile(file)) {

            if (jarFile.getJarEntry(
                    "plugin.yml"
            ) == null) {

                throw new IOException(
                        "Downloaded JAR does not contain "
                                + "plugin.yml."
                );
            }
        } catch (IOException exception) {

            throw new IOException(
                    "Downloaded file is not a valid "
                            + "MCRealTime plugin JAR: "
                            + exception.getMessage(),
                    exception
            );
        }
    }

    private void moveIntoPlace(
            File temporaryFile,
            File destination)
            throws IOException {

        try {

            Files.move(
                    temporaryFile.toPath(),
                    destination.toPath(),
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE
            );

        } catch (
                AtomicMoveNotSupportedException exception) {

            /*
             * Some filesystems do not support atomic
             * moves. The files are still on the same
             * update directory, so fall back safely.
             */
            Files.move(
                    temporaryFile.toPath(),
                    destination.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
            );
        }
    }

    private void deleteTemporaryFile(
            File temporaryFile) {

        if (temporaryFile.isFile()) {

            /*
             * Best-effort cleanup.
             *
             * The original failure is more important
             * than a cleanup failure.
             */
            temporaryFile.delete();
        }
    }
}
