package ing.applecraft.mcrealtime.update;

import ing.applecraft.mcrealtime.MCRealTimePlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;

public final class AsyncUpdateManager {

    private final MCRealTimePlugin plugin;
    private final UpdateService updateService;
    private final UpdateDownloader updateDownloader;
    private final UpdaterSettingsLoader settingsLoader;

    /*
     * One update operation at a time.
     *
     * This covers both checks and downloads.
     */
    private boolean checkInProgress;

    private UpdateCheckResult lastResult;

    public AsyncUpdateManager(
            MCRealTimePlugin plugin,
            UpdateService updateService,
            UpdateDownloader updateDownloader,
            UpdaterSettingsLoader settingsLoader) {

        if (plugin == null) {
            throw new IllegalArgumentException(
                    "plugin must not be null"
            );
        }

        if (updateService == null) {
            throw new IllegalArgumentException(
                    "updateService must not be null"
            );
        }

        if (updateDownloader == null) {
            throw new IllegalArgumentException(
                    "updateDownloader must not be null"
            );
        }

        if (settingsLoader == null) {
            throw new IllegalArgumentException(
                    "settingsLoader must not be null"
            );
        }

        this.plugin = plugin;
        this.updateService = updateService;
        this.updateDownloader = updateDownloader;
        this.settingsLoader = settingsLoader;
    }

    public void checkOnStart() {

        UpdaterSettings settings =
                settingsLoader.load(
                        plugin.getConfig()
                );

        if (!settings.isEnabled()) {
            return;
        }

        if (!settings.isCheckOnStart()) {
            return;
        }

        if (settings.isAutoDownload()) {

            downloadLatestUpdate();

        } else {

            checkForUpdates();
        }
    }

    public boolean checkForUpdates() {

        return startCheck(
                null
        );
    }

    public boolean checkForUpdates(
            CommandSender requester) {

        if (requester == null) {
            throw new IllegalArgumentException(
                    "requester must not be null"
            );
        }

        return startCheck(
                requester
        );
    }

    private boolean startCheck(
            final CommandSender requester) {

        if (!beginCheck()) {
            return false;
        }

        final String currentVersion =
                plugin.getDescription()
                        .getVersion();

        try {

            plugin.getServer()
                    .getScheduler()
                    .runTaskAsynchronously(
                            plugin,
                            new Runnable() {

                                @Override
                                public void run() {

                                    performAsyncCheck(
                                            currentVersion,
                                            requester
                                    );
                                }
                            }
                    );

            return true;

        } catch (RuntimeException exception) {

            finishCheck();

            throw exception;
        }
    }

    /*
     * =========================================================
     * DOWNLOAD
     * =========================================================
     */

    public boolean downloadLatestUpdate() {

        return startDownload(
                null
        );
    }

    public boolean downloadLatestUpdate(
            CommandSender requester) {

        if (requester == null) {
            throw new IllegalArgumentException(
                    "requester must not be null"
            );
        }

        return startDownload(
                requester
        );
    }

    private boolean startDownload(
            final CommandSender requester) {

        if (!beginCheck()) {
            return false;
        }

        /*
         * Read Bukkit state while still on the
         * Minecraft server thread.
         */
        final String currentVersion =
                plugin.getDescription()
                        .getVersion();

        final File updateDirectory =
                plugin.getServer()
                        .getUpdateFolderFile();

        final String targetFileName =
                plugin.getPluginFileName();

        try {

            plugin.getServer()
                    .getScheduler()
                    .runTaskAsynchronously(
                            plugin,
                            new Runnable() {

                                @Override
                                public void run() {

                                    performAsyncDownload(
                                            currentVersion,
                                            updateDirectory,
                                            targetFileName,
                                            requester
                                    );
                                }
                            }
                    );

            return true;

        } catch (RuntimeException exception) {

            /*
             * Scheduling itself failed, therefore there
             * will never be an asynchronous task that can
             * clear the operation flag.
             */
            finishCheck();

            throw exception;
        }
    }

    private void performAsyncDownload(
            String currentVersion,
            File updateDirectory,
            String targetFileName,
            final CommandSender requester) {

        try {

            final UpdateCheckResult result =
                    updateService.check(
                            currentVersion
                    );

            if (!result.isUpdateAvailable()) {

                runOnMainThread(
                        new Runnable() {

                            @Override
                            public void run() {

                                handleNoDownloadRequired(
                                        result,
                                        requester
                                );
                            }
                        }
                );

                return;
            }

            Optional<GitHubReleaseAsset> asset =
                    result
                            .getLatestRelease()
                            .findPluginJarAsset();

            if (!asset.isPresent()) {

                throw new IOException(
                        "The GitHub release "
                                + result
                                .getLatestRelease()
                                .getTagName()
                                + " does not contain "
                                + "an MCRealTime JAR asset."
                );
            }

            final File downloadedFile =
                    updateDownloader.download(
                            asset.get(),
                            updateDirectory,
                            targetFileName
                    );

            runOnMainThread(
                    new Runnable() {

                        @Override
                        public void run() {

                            handleDownloadSuccess(
                                    result,
                                    downloadedFile,
                                    requester
                            );
                        }
                    }
            );

        } catch (final NoPublishedReleaseException exception) {

            runOnMainThread(
                    new Runnable() {

                        @Override
                        public void run() {

                            handleNoPublishedRelease(
                                    requester
                            );
                        }
                    }
            );

        } catch (final IOException exception) {

            runOnMainThread(
                    new Runnable() {

                        @Override
                        public void run() {

                            handleDownloadFailure(
                                    exception,
                                    requester
                            );
                        }
                    }
            );

        } catch (final RuntimeException exception) {

            runOnMainThread(
                    new Runnable() {

                        @Override
                        public void run() {

                            handleDownloadFailure(
                                    exception,
                                    requester
                            );
                        }
                    }
            );
        }
    }

    /*
     * =========================================================
     * CHECK IMPLEMENTATION
     * =========================================================
     */

    private void performAsyncCheck(
            String currentVersion,
            final CommandSender requester) {

        try {

            final UpdateCheckResult result =
                    updateService.check(
                            currentVersion
                    );

            runOnMainThread(
                    new Runnable() {

                        @Override
                        public void run() {

                            handleSuccess(
                                    result,
                                    requester
                            );
                        }
                    }
            );

        } catch (final NoPublishedReleaseException exception) {

            runOnMainThread(
                    new Runnable() {

                        @Override
                        public void run() {

                            handleNoPublishedRelease(
                                    requester
                            );
                        }
                    }
            );

        } catch (final IOException exception) {

            runOnMainThread(
                    new Runnable() {

                        @Override
                        public void run() {

                            handleFailure(
                                    exception,
                                    requester
                            );
                        }
                    }
            );

        } catch (final RuntimeException exception) {

            runOnMainThread(
                    new Runnable() {

                        @Override
                        public void run() {

                            handleFailure(
                                    exception,
                                    requester
                            );
                        }
                    }
            );
        }
    }

    private void runOnMainThread(
            Runnable task) {

        if (!plugin.isEnabled()) {

            finishCheck();
            return;
        }

        try {

            plugin.getServer()
                    .getScheduler()
                    .runTask(
                            plugin,
                            task
                    );

        } catch (RuntimeException exception) {

            finishCheck();

            plugin.getLogger().log(
                    Level.FINE,
                    "Could not return update result "
                            + "to the server thread.",
                    exception
            );
        }
    }

    /*
     * =========================================================
     * RESULT HANDLERS
     * =========================================================
     */

    private void handleSuccess(
            UpdateCheckResult result,
            CommandSender requester) {

        lastResult =
                result;

        finishCheck();

        if (result.isUpdateAvailable()) {

            String message =
                    "A new MCRealTime version is available: "
                            + result
                            .getLatestRelease()
                            .getTagName()
                            + " (installed: "
                            + result.getCurrentVersion()
                            + ").";

            plugin.getLogger().warning(
                    message
            );

            sendToRequester(
                    requester,
                    ChatColor.GOLD
                            + message
            );

        } else {

            String message =
                    "MCRealTime is up to date. "
                            + "Installed version: "
                            + result.getCurrentVersion()
                            + ".";

            plugin.getLogger().info(
                    message
            );

            sendToRequester(
                    requester,
                    ChatColor.GREEN
                            + message
            );
        }
    }

    private void handleNoDownloadRequired(
            UpdateCheckResult result,
            CommandSender requester) {

        lastResult =
                result;

        finishCheck();

        String message =
                "No update needs to be downloaded. "
                        + "Installed version: "
                        + result.getCurrentVersion()
                        + ".";

        plugin.getLogger().info(
                message
        );

        sendToRequester(
                requester,
                ChatColor.GREEN
                        + message
        );
    }

    private void handleDownloadSuccess(
            UpdateCheckResult result,
            File downloadedFile,
            CommandSender requester) {

        lastResult =
                result;

        finishCheck();

        String message =
                "MCRealTime "
                        + result
                        .getLatestRelease()
                        .getTagName()
                        + " was downloaded successfully as "
                        + downloadedFile.getName()
                        + ". Restart the server to install it.";

        plugin.getLogger().info(
                message
        );

        sendToRequester(
                requester,
                ChatColor.GREEN
                        + message
        );
    }

    private void handleNoPublishedRelease(
            CommandSender requester) {

        finishCheck();

        String message =
                "No published MCRealTime release "
                        + "is available on GitHub yet.";

        plugin.getLogger().info(
                message
        );

        sendToRequester(
                requester,
                ChatColor.YELLOW
                        + message
        );
    }

    private void handleFailure(
            Exception exception,
            CommandSender requester) {

        finishCheck();

        String message =
                "Could not check for MCRealTime updates: "
                        + exception.getMessage();

        plugin.getLogger().warning(
                message
        );

        sendToRequester(
                requester,
                ChatColor.RED
                        + message
        );
    }

    private void handleDownloadFailure(
            Exception exception,
            CommandSender requester) {

        finishCheck();

        String message =
                "Could not download the MCRealTime update: "
                        + exception.getMessage();

        plugin.getLogger().warning(
                message
        );

        sendToRequester(
                requester,
                ChatColor.RED
                        + message
        );
    }

    private void sendToRequester(
            CommandSender requester,
            String message) {

        if (requester == null) {
            return;
        }

        if (requester instanceof ConsoleCommandSender) {
            return;
        }

        requester.sendMessage(
                ChatColor.DARK_GREEN
                        + "[MCRealTime] "
                        + ChatColor.RESET
                        + message
        );
    }

    private synchronized boolean beginCheck() {

        if (checkInProgress) {
            return false;
        }

        checkInProgress =
                true;

        return true;
    }

    private synchronized void finishCheck() {

        checkInProgress =
                false;
    }

    public synchronized boolean isCheckInProgress() {

        return checkInProgress;
    }

    public UpdateCheckResult getLastResult() {

        return lastResult;
    }
}
