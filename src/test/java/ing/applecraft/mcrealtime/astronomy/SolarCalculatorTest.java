package ing.applecraft.mcrealtime.astronomy;

import org.junit.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SolarCalculatorTest {
    private static final GeoLocation OLDENBURG = new GeoLocation(53.1435, 8.2146);
    private static final ZoneId BERLIN = ZoneId.of("Europe/Berlin");

    private final SolarCalculator calculator = new SolarCalculator();

    @Test
    public void oldenburgSummerSolsticeIsCloseToPublishedSolarTimes() {
        SolarDay day = calculator.calculate(LocalDate.of(2026, 6, 21), OLDENBURG);

        assertEquals(SolarDay.Status.NORMAL, day.getStatus());
        assertNear(LocalTime.of(5, 0), day.getSunrise().atZone(BERLIN).toLocalTime(), 8);
        assertNear(LocalTime.of(13, 28), day.getSolarNoon().atZone(BERLIN).toLocalTime(), 8);
        assertNear(LocalTime.of(21, 57), day.getSunset().atZone(BERLIN).toLocalTime(), 8);
    }

    @Test
    public void oldenburgWinterSolsticeIsCloseToPublishedSolarTimes() {
        SolarDay day = calculator.calculate(LocalDate.of(2026, 12, 21), OLDENBURG);

        assertEquals(SolarDay.Status.NORMAL, day.getStatus());
        assertNear(LocalTime.of(8, 39), day.getSunrise().atZone(BERLIN).toLocalTime(), 8);
        assertNear(LocalTime.of(12, 25), day.getSolarNoon().atZone(BERLIN).toLocalTime(), 8);
        assertNear(LocalTime.of(16, 11), day.getSunset().atZone(BERLIN).toLocalTime(), 8);
    }

    @Test
    public void detectsPolarDayAndPolarNight() {
        GeoLocation tromso = new GeoLocation(69.6492, 18.9553);

        assertEquals(
                SolarDay.Status.POLAR_DAY,
                calculator.calculate(LocalDate.of(2026, 6, 21), tromso).getStatus()
        );
        assertEquals(
                SolarDay.Status.POLAR_NIGHT,
                calculator.calculate(LocalDate.of(2026, 12, 21), tromso).getStatus()
        );
    }

    private void assertNear(LocalTime expected, LocalTime actual, long toleranceMinutes) {
        ZonedDateTime reference = ZonedDateTime.of(LocalDate.of(2026, 1, 1), expected, BERLIN);
        ZonedDateTime measured = ZonedDateTime.of(LocalDate.of(2026, 1, 1), actual, BERLIN);
        long difference = Math.abs(Duration.between(reference, measured).toMinutes());
        assertTrue("Expected about " + expected + " but got " + actual, difference <= toleranceMinutes);
    }
}
