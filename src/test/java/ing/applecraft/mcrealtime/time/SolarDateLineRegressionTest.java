
package ing.applecraft.mcrealtime.time;

import ing.applecraft.mcrealtime.astronomy.GeoLocation;
import ing.applecraft.mcrealtime.astronomy.SolarDay;
import org.junit.Test;

import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SolarDateLineRegressionTest {

    @Test
    public void solarEventsMustBelongToTheCorrectLocalDate() {
        // Locations on both sides of the International Date Line,
        // plus two reference locations.
        verifyLocalDate(
                "Pacific/Apia",
                -13.8333, -171.7500
        );

        verifyLocalDate(
                "Pacific/Kiritimati",
                1.8721, -157.4278
        );

        verifyLocalDate(
                "Europe/Berlin",
                52.5200, 13.4050
        );

        verifyLocalDate(
                "Australia/Sydney",
                -33.8688, 151.2093
        );
    }

    @Test
    public void solarEventsMustMapToExpectedMinecraftTimes() {
        ZoneId zone = ZoneId.of("Pacific/Apia");

        SolarTimeProvider provider = new SolarTimeProvider(
                zone,
                new GeoLocation(-13.8333, -171.7500)
        );

        LocalDate date = LocalDate.of(2026, 6, 21);
        SolarDay solarDay = provider.getSolarDay(date);

        assertTrue(solarDay.hasSunriseAndSunset());

        assertEquals(
                0L,
                provider.getMinecraftTime(solarDay.getSunrise())
        );

        assertEquals(
                6000L,
                provider.getMinecraftTime(solarDay.getSolarNoon())
        );

        assertEquals(
                12000L,
                provider.getMinecraftTime(solarDay.getSunset())
        );
    }

    private void verifyLocalDate(
            String timeZone,
            double latitude,
            double longitude) {

        ZoneId zone = ZoneId.of(timeZone);

        SolarTimeProvider provider = new SolarTimeProvider(
                zone,
                new GeoLocation(latitude, longitude)
        );

        // Summer and winter dates expose different UTC offsets
        // and seasonal astronomical conditions.
        LocalDate[] dates = {
                LocalDate.of(2026, 6, 21),
                LocalDate.of(2026, 12, 21)
        };

        for (LocalDate date : dates) {
            SolarDay result = provider.getSolarDay(date);

            assertTrue(
                    timeZone + ": Missing solar events for " + date,
                    result.hasSunriseAndSunset()
            );

            assertEquals(
                    timeZone + ": Incorrect sunrise date",
                    date,
                    result.getSunrise().atZone(zone).toLocalDate()
            );

            assertEquals(
                    timeZone + ": Incorrect solar noon date",
                    date,
                    result.getSolarNoon().atZone(zone).toLocalDate()
            );

            assertEquals(
                    timeZone + ": Incorrect sunset date",
                    date,
                    result.getSunset().atZone(zone).toLocalDate()
            );

            assertTrue(
                    timeZone + ": Invalid sunrise/noon ordering",
                    result.getSunrise().isBefore(result.getSolarNoon())
            );

            assertTrue(
                    timeZone + ": Invalid noon/sunset ordering",
                    result.getSolarNoon().isBefore(result.getSunset())
            );
        }
    }
}
