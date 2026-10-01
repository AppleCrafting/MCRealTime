package ing.applecraft.mcrealtime.update;

public final class UpdaterSettings {

    private final boolean enabled;
    private final boolean checkOnStart;
    private final boolean autoDownload;

    public UpdaterSettings(
            boolean enabled,
            boolean checkOnStart,
            boolean autoDownload) {

        this.enabled = enabled;
        this.checkOnStart = checkOnStart;
        this.autoDownload = autoDownload;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isCheckOnStart() {
        return checkOnStart;
    }

    public boolean isAutoDownload() {
        return autoDownload;
    }
}
