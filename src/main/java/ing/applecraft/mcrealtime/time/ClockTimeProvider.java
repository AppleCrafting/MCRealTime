package ing.applecraft.mcrealtime.time;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * Classic MCRealTime mode: maps local civil clock time linearly to Minecraft time.
 */
public final class ClockTimeProvider implements TimeProvider {
    private static final long TICKS_PER_DAY = 24_000L;
    private static final double SECONDS_PER_DAY = 86_400.0;
    private static final long MIDNIGHT_OFFSET_TICKS = 18_000L;

    private final ZoneId zoneId;

    public ClockTimeProvider(ZoneId zoneId) {
        this.zoneId = Objects.requireNonNull(zoneId, "zoneId");
    }

    @Override
    public long getMinecraftTime(Instant instant) {
        Objects.requireNonNull(instant, "instant");
        ZonedDateTime local = instant.atZone(zoneId);

        double secondsOfDay = local.getHour() * 3600.0
                + local.getMinute() * 60.0
                + local.getSecond()
                + local.getNano() / 1_000_000_000.0;

        long ticksSinceCivilMidnight = Math.round(secondsOfDay / SECONDS_PER_DAY * TICKS_PER_DAY);
        return Math.floorMod(ticksSinceCivilMidnight + MIDNIGHT_OFFSET_TICKS, TICKS_PER_DAY);
    }

    public ZoneId getZoneId() {
        return zoneId;
    }
}
