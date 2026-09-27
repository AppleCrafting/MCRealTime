package ing.applecraft.mcrealtime.time;

import ing.applecraft.mcrealtime.astronomy.GeoLocation;
import ing.applecraft.mcrealtime.astronomy.SolarCalculator;
import ing.applecraft.mcrealtime.astronomy.SolarDay;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Astronomical mode:
 * sunrise -> 0, solar noon -> 6000, sunset -> 12000,
 * and the night is interpolated to the next sunrise.
 * <p>
 * Polar days/nights have no sunrise/sunset pair. For those dates this provider
 * deliberately falls back to classic clock mapping until a dedicated polar
 * sky model is introduced.
 */
public final class SolarTimeProvider implements TimeProvider {
    private static final int CACHE_SIZE = 8;

    private final ZoneId zoneId;
    private final GeoLocation location;
    private final SolarCalculator calculator;
    private final TimeProvider polarFallback;
    private final Map<LocalDate, SolarDay> cache;

    public SolarTimeProvider(ZoneId zoneId, GeoLocation location) {
        this(zoneId, location, new SolarCalculator(), new ClockTimeProvider(zoneId));
    }

    SolarTimeProvider(ZoneId zoneId,
                      GeoLocation location,
                      SolarCalculator calculator,
                      TimeProvider polarFallback) {
        this.zoneId = Objects.requireNonNull(zoneId, "zoneId");
        this.location = Objects.requireNonNull(location, "location");
        this.calculator = Objects.requireNonNull(calculator, "calculator");
        this.polarFallback = Objects.requireNonNull(polarFallback, "polarFallback");
        this.cache = new LinkedHashMap<LocalDate, SolarDay>(CACHE_SIZE + 1, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<LocalDate, SolarDay> eldest) {
                return size() > CACHE_SIZE;
            }
        };
    }

    @Override
    public synchronized long getMinecraftTime(Instant instant) {
        Objects.requireNonNull(instant, "instant");
        LocalDate date = instant.atZone(zoneId).toLocalDate();
        SolarDay today = solarDay(date);

        if (!today.hasSunriseAndSunset()) {
            return polarFallback.getMinecraftTime(instant);
        }

        if (instant.isBefore(today.getSunrise())) {
            SolarDay yesterday = solarDay(date.minusDays(1));
            if (!yesterday.hasSunriseAndSunset()) {
                return polarFallback.getMinecraftTime(instant);
            }
            return interpolate(instant, yesterday.getSunset(), today.getSunrise(), 12_000L, 24_000L);
        }

        if (instant.isBefore(today.getSolarNoon())) {
            return interpolate(instant, today.getSunrise(), today.getSolarNoon(), 0L, 6_000L);
        }

        if (instant.isBefore(today.getSunset())) {
            return interpolate(instant, today.getSolarNoon(), today.getSunset(), 6_000L, 12_000L);
        }

        SolarDay tomorrow = solarDay(date.plusDays(1));
        if (!tomorrow.hasSunriseAndSunset()) {
            return polarFallback.getMinecraftTime(instant);
        }
        return interpolate(instant, today.getSunset(), tomorrow.getSunrise(), 12_000L, 24_000L);
    }

    public SolarDay getSolarDay(LocalDate date) {
        return solarDay(date);
    }

    private SolarDay solarDay(LocalDate date) {
        SolarDay cached = cache.get(date);
        if (cached != null) {
            return cached;
        }
        SolarDay calculated = calculator.calculate(date, location);
        cache.put(date, calculated);
        return calculated;
    }

    private long interpolate(Instant now,
                             Instant start,
                             Instant end,
                             long startTicks,
                             long endTicks) {
        long startMillis = start.toEpochMilli();
        long endMillis = end.toEpochMilli();
        long nowMillis = now.toEpochMilli();

        if (endMillis <= startMillis) {
            throw new IllegalStateException("Interpolation interval must have positive duration.");
        }

        double progress = (double) (nowMillis - startMillis) / (double) (endMillis - startMillis);
        progress = Math.max(0.0, Math.min(1.0, progress));

        long mapped = Math.round(startTicks + progress * (endTicks - startTicks));
        return Math.floorMod(mapped, 24_000L);
    }
}
