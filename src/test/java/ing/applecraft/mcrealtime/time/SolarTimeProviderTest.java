package ing.applecraft.mcrealtime.time;

import ing.applecraft.mcrealtime.astronomy.GeoLocation;
import ing.applecraft.mcrealtime.astronomy.SolarDay;
import org.junit.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SolarTimeProviderTest {
    private static final ZoneId BERLIN = ZoneId.of("Europe/Berlin");
    private final SolarTimeProvider provider = new SolarTimeProvider(
            BERLIN,
            new GeoLocation(53.1435, 8.2146)
    );

    @Test
    public void mapsSolarEventsToCanonicalMinecraftTicks() {
        SolarDay day = provider.getSolarDay(LocalDate.of(2026, 6, 21));

        assertEquals(0L, provider.getMinecraftTime(day.getSunrise()));
        assertEquals(6_000L, provider.getMinecraftTime(day.getSolarNoon()));
        assertEquals(12_000L, provider.getMinecraftTime(day.getSunset()));
    }

    @Test
    public void seasonalDayLengthChangesTheMapping() {
        Instant juneEvening = ZonedDateTime.of(2026, 6, 21, 18, 0, 0, 0, BERLIN).toInstant();
        Instant decemberEvening = ZonedDateTime.of(2026, 12, 21, 18, 0, 0, 0, BERLIN).toInstant();

        long juneTicks = provider.getMinecraftTime(juneEvening);
        long decemberTicks = provider.getMinecraftTime(decemberEvening);

        assertTrue(juneTicks >= 6_000L && juneTicks < 12_000L);
        assertTrue(decemberTicks >= 12_000L && decemberTicks < 24_000L);
    }
}
