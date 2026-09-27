package ing.applecraft.mcrealtime.astronomy;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Astronomical events associated with one civil calendar date.
 */
public final class SolarDay {
    public enum Status {
        NORMAL,
        POLAR_DAY,
        POLAR_NIGHT
    }

    private final LocalDate date;
    private final Status status;
    private final Instant sunrise;
    private final Instant solarNoon;
    private final Instant sunset;

    private SolarDay(LocalDate date, Status status, Instant sunrise, Instant solarNoon, Instant sunset) {
        this.date = Objects.requireNonNull(date, "date");
        this.status = Objects.requireNonNull(status, "status");
        this.sunrise = sunrise;
        this.solarNoon = Objects.requireNonNull(solarNoon, "solarNoon");
        this.sunset = sunset;
    }

    public static SolarDay normal(LocalDate date, Instant sunrise, Instant solarNoon, Instant sunset) {
        Objects.requireNonNull(sunrise, "sunrise");
        Objects.requireNonNull(sunset, "sunset");
        if (!sunrise.isBefore(solarNoon) || !solarNoon.isBefore(sunset)) {
            throw new IllegalArgumentException("Solar events must satisfy sunrise < solarNoon < sunset.");
        }
        return new SolarDay(date, Status.NORMAL, sunrise, solarNoon, sunset);
    }

    public static SolarDay polar(LocalDate date, Status status, Instant solarNoon) {
        if (status == Status.NORMAL) {
            throw new IllegalArgumentException("Polar solar day cannot have NORMAL status.");
        }
        return new SolarDay(date, status, null, solarNoon, null);
    }

    public LocalDate getDate() {
        return date;
    }

    public Status getStatus() {
        return status;
    }

    public boolean hasSunriseAndSunset() {
        return status == Status.NORMAL;
    }

    public Instant getSunrise() {
        return sunrise;
    }

    public Instant getSolarNoon() {
        return solarNoon;
    }

    public Instant getSunset() {
        return sunset;
    }
}
