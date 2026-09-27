package ing.applecraft.mcrealtime.time;

import ing.applecraft.mcrealtime.config.PluginSettings;

public final class TimeProviderFactory {
    private TimeProviderFactory() {
    }

    public static TimeProvider create(PluginSettings settings) {
        if (settings.getMode() == TimeMode.SOLAR) {
            return new SolarTimeProvider(settings.getZoneId(), settings.getSolarLocation());
        }
        return new ClockTimeProvider(settings.getZoneId());
    }
}
