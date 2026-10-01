package ing.applecraft.mcrealtime.astronomy;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

/**
 * Offline sunrise, solar-noon and sunset calculator based on the NOAA/Meeus
 * solar equations. Sunrise/sunset use an apparent solar zenith of 90.833°.
 *
 * Longitude follows the international convention: east positive, west negative.
 */
public final class SolarCalculator {
    private static final double SUNRISE_ZENITH_DEGREES = 90.833;
    private static final double MINUTES_PER_DAY = 1440.0;

    public SolarDay calculate(LocalDate date, GeoLocation location) {
        return calculateForUtcDate(date, date, location);
    }

    public SolarDay calculate(
            LocalDate date,
            GeoLocation location,
            ZoneId zoneId) {

        if (zoneId == null) {
            throw new IllegalArgumentException("zoneId must not be null");
        }

        LocalDate utcAnchorDate = date;

        for (int attempt = 0; attempt < 3; attempt++) {
            SolarDay result =
                    calculateForUtcDate(date, utcAnchorDate, location);

            LocalDate solarNoonLocalDate =
                    result.getSolarNoon()
                            .atZone(zoneId)
                            .toLocalDate();

            long dayDifference =
                    ChronoUnit.DAYS.between(solarNoonLocalDate, date);

            if (dayDifference == 0L) {
                return result;
            }

            utcAnchorDate = utcAnchorDate.plusDays(dayDifference);
        }

        throw new IllegalStateException(
                "Could not associate solar noon with the requested local date.");
    }

    private SolarDay calculateForUtcDate(
            LocalDate date,
            LocalDate utcAnchorDate,
            GeoLocation location) {
        if (date == null) {
            throw new IllegalArgumentException("date must not be null");
        }
        if (location == null) {
            throw new IllegalArgumentException("location must not be null");
        }

        double julianDay = julianDayAtUtcMidnight(utcAnchorDate);
        double solarNoonMinutes = calculateSolarNoonUtcMinutes(julianDay, location.getLongitude());
        Instant solarNoon =
                instantFromUtcMinutes(utcAnchorDate, solarNoonMinutes);

        double centuryAtNoon = julianCentury(julianDay + solarNoonMinutes / MINUTES_PER_DAY);
        double declinationAtNoon = sunDeclinationDegrees(centuryAtNoon);
        double hourAngleArgument = sunriseHourAngleArgument(location.getLatitude(), declinationAtNoon);

        if (hourAngleArgument < -1.0) {
            return SolarDay.polar(date, SolarDay.Status.POLAR_DAY, solarNoon);
        }
        if (hourAngleArgument > 1.0) {
            return SolarDay.polar(date, SolarDay.Status.POLAR_NIGHT, solarNoon);
        }

        double hourAngle = Math.toDegrees(Math.acos(clamp(hourAngleArgument, -1.0, 1.0)));

        double sunriseMinutes = solarNoonMinutes - 4.0 * hourAngle;
        double sunsetMinutes = solarNoonMinutes + 4.0 * hourAngle;

        // One refinement pass accounts for the change in equation-of-time and
        // declination between solar noon and the event itself.
        sunriseMinutes = refineEventUtcMinutes(julianDay, location, sunriseMinutes, true);
        sunsetMinutes = refineEventUtcMinutes(julianDay, location, sunsetMinutes, false);

        Instant sunrise =
                instantFromUtcMinutes(utcAnchorDate, sunriseMinutes);
        Instant sunset =
                instantFromUtcMinutes(utcAnchorDate, sunsetMinutes);

        // Near the international date line, the values may naturally lie on an
        // adjacent UTC date. Instant ordering must still hold for one solar day.
        if (!sunrise.isBefore(solarNoon)) {
            sunrise = sunrise.minusSeconds(86_400L);
        }
        if (!solarNoon.isBefore(sunset)) {
            sunset = sunset.plusSeconds(86_400L);
        }

        return SolarDay.normal(date, sunrise, solarNoon, sunset);
    }

    private double calculateSolarNoonUtcMinutes(double julianDay, double longitudeDegrees) {
        double t = julianCentury(julianDay + 0.5);
        double minutes = 720.0 - 4.0 * longitudeDegrees - equationOfTimeMinutes(t);

        // Refine at the estimated instant of solar noon.
        double refinedT = julianCentury(julianDay + minutes / MINUTES_PER_DAY);
        return 720.0 - 4.0 * longitudeDegrees - equationOfTimeMinutes(refinedT);
    }

    private double refineEventUtcMinutes(double julianDay,
                                         GeoLocation location,
                                         double estimatedMinutes,
                                         boolean sunrise) {
        double t = julianCentury(julianDay + estimatedMinutes / MINUTES_PER_DAY);
        double equationOfTime = equationOfTimeMinutes(t);
        double declination = sunDeclinationDegrees(t);
        double argument = sunriseHourAngleArgument(location.getLatitude(), declination);

        if (argument < -1.0 || argument > 1.0) {
            return estimatedMinutes;
        }

        double hourAngle = Math.toDegrees(Math.acos(clamp(argument, -1.0, 1.0)));
        if (sunrise) {
            return 720.0 - 4.0 * (location.getLongitude() + hourAngle) - equationOfTime;
        }
        return 720.0 - 4.0 * (location.getLongitude() - hourAngle) - equationOfTime;
    }

    private double sunriseHourAngleArgument(double latitudeDegrees, double declinationDegrees) {
        double latitude = Math.toRadians(latitudeDegrees);
        double declination = Math.toRadians(declinationDegrees);
        double zenith = Math.toRadians(SUNRISE_ZENITH_DEGREES);

        return Math.cos(zenith) / (Math.cos(latitude) * Math.cos(declination))
                - Math.tan(latitude) * Math.tan(declination);
    }

    private Instant instantFromUtcMinutes(LocalDate date, double utcMinutes) {
        long seconds = Math.round(utcMinutes * 60.0);
        return date.atStartOfDay(ZoneOffset.UTC).toInstant().plusSeconds(seconds);
    }

    private double julianDayAtUtcMidnight(LocalDate date) {
        int year = date.getYear();
        int month = date.getMonthValue();
        int day = date.getDayOfMonth();

        if (month <= 2) {
            year -= 1;
            month += 12;
        }

        int a = year / 100;
        int b = 2 - a + a / 4;

        return Math.floor(365.25 * (year + 4716))
                + Math.floor(30.6001 * (month + 1))
                + day + b - 1524.5;
    }

    private double julianCentury(double julianDay) {
        return (julianDay - 2451545.0) / 36525.0;
    }

    private double equationOfTimeMinutes(double t) {
        double epsilon = Math.toRadians(obliquityCorrectionDegrees(t));
        double l0 = Math.toRadians(geomMeanLongitudeSunDegrees(t));
        double e = eccentricityEarthOrbit(t);
        double m = Math.toRadians(geomMeanAnomalySunDegrees(t));

        double y = Math.tan(epsilon / 2.0);
        y *= y;

        double equation = y * Math.sin(2.0 * l0)
                - 2.0 * e * Math.sin(m)
                + 4.0 * e * y * Math.sin(m) * Math.cos(2.0 * l0)
                - 0.5 * y * y * Math.sin(4.0 * l0)
                - 1.25 * e * e * Math.sin(2.0 * m);

        return Math.toDegrees(equation) * 4.0;
    }

    private double sunDeclinationDegrees(double t) {
        double obliquity = Math.toRadians(obliquityCorrectionDegrees(t));
        double apparentLongitude = Math.toRadians(sunApparentLongitudeDegrees(t));
        return Math.toDegrees(Math.asin(Math.sin(obliquity) * Math.sin(apparentLongitude)));
    }

    private double geomMeanLongitudeSunDegrees(double t) {
        double value = 280.46646 + t * (36000.76983 + t * 0.0003032);
        return normalizeDegrees(value);
    }

    private double geomMeanAnomalySunDegrees(double t) {
        return 357.52911 + t * (35999.05029 - 0.0001537 * t);
    }

    private double eccentricityEarthOrbit(double t) {
        return 0.016708634 - t * (0.000042037 + 0.0000001267 * t);
    }

    private double sunEquationOfCenterDegrees(double t) {
        double m = Math.toRadians(geomMeanAnomalySunDegrees(t));
        return Math.sin(m) * (1.914602 - t * (0.004817 + 0.000014 * t))
                + Math.sin(2.0 * m) * (0.019993 - 0.000101 * t)
                + Math.sin(3.0 * m) * 0.000289;
    }

    private double sunTrueLongitudeDegrees(double t) {
        return geomMeanLongitudeSunDegrees(t) + sunEquationOfCenterDegrees(t);
    }

    private double sunApparentLongitudeDegrees(double t) {
        double omega = 125.04 - 1934.136 * t;
        return sunTrueLongitudeDegrees(t)
                - 0.00569
                - 0.00478 * Math.sin(Math.toRadians(omega));
    }

    private double meanObliquityOfEclipticDegrees(double t) {
        double seconds = 21.448 - t * (46.815 + t * (0.00059 - t * 0.001813));
        return 23.0 + (26.0 + seconds / 60.0) / 60.0;
    }

    private double obliquityCorrectionDegrees(double t) {
        double omega = 125.04 - 1934.136 * t;
        return meanObliquityOfEclipticDegrees(t)
                + 0.00256 * Math.cos(Math.toRadians(omega));
    }

    private double normalizeDegrees(double value) {
        double normalized = value % 360.0;
        return normalized < 0.0 ? normalized + 360.0 : normalized;
    }

    private double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
