package ing.applecraft.mcrealtime.time;

import org.junit.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.Assert.assertEquals;

public class ClockTimeProviderTest {
    private final ZoneId zone = ZoneId.of("Europe/Berlin");
    private final ClockTimeProvider provider = new ClockTimeProvider(zone);

    @Test
    public void mapsCivilClockToMinecraftDay() {
        assertEquals(18_000L, timeAt(0));
        assertEquals(0L, timeAt(6));
        assertEquals(6_000L, timeAt(12));
        assertEquals(12_000L, timeAt(18));
    }

    @Test
    public void includesMinutesAndSecondsWithoutIntegerDivisionLoss() {
        ZonedDateTime time = ZonedDateTime.of(2026, 1, 1, 12, 30, 30, 0, zone);
        long ticks = provider.getMinecraftTime(time.toInstant());
        assertEquals(6_508L, ticks);
    }

    private long timeAt(int hour) {
        ZonedDateTime time = ZonedDateTime.of(2026, 1, 1, hour, 0, 0, 0, zone);
        return provider.getMinecraftTime(time.toInstant());
    }
}
