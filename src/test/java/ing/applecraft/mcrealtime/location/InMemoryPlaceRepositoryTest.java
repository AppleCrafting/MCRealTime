package ing.applecraft.mcrealtime.location;

import ing.applecraft.mcrealtime.astronomy.GeoLocation;
import org.junit.Test;

import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

public class InMemoryPlaceRepositoryTest {

    private final Place oldenburg = new Place(
            2950159L,
            "Oldenburg",
            "DE",
            new GeoLocation(53.1412, 8.2147),
            ZoneId.of("Europe/Berlin"),
            170000L
    );

    private final Place oldenburgInHolstein = new Place(
            2857458L,
            "Oldenburg in Holstein",
            "DE",
            new GeoLocation(54.2958, 10.9016),
            ZoneId.of("Europe/Berlin"),
            10000L
    );

    private final Place berlin = new Place(
            2950159 + 1,
            "Berlin",
            "DE",
            new GeoLocation(52.5200, 13.4050),
            ZoneId.of("Europe/Berlin"),
            3600000L
    );

    private  InMemoryPlaceRepository createRepository() {
        return new InMemoryPlaceRepository(
                Arrays.asList(
                        oldenburgInHolstein,
                        berlin,
                        oldenburg
                )
        );
    }

    @Test
    public void findPlacesByNamePrefix() {
        InMemoryPlaceRepository repository = createRepository();

        List<Place> result = repository.findByNamePrefix("Old", 10);

        assertEquals(2, result.size());
    }

    @Test
    public void prefixSearchIsCaseInsensitive() {
        InMemoryPlaceRepository repository = createRepository();

        List<Place> result = repository.findByNamePrefix("old", 10);

        assertEquals(2, result.size());
    }

    @Test
    public void sortsMatchesByPopulationDescending() {
        InMemoryPlaceRepository repository = createRepository();

        assertEquals(170000L, oldenburg.getPopulation());
        assertEquals(10000L, oldenburgInHolstein.getPopulation());

        List<Place> result = repository.findByNamePrefix("old", 10);

        assertEquals("Oldenburg", result.get(0).getName());
        assertEquals("Oldenburg in Holstein", result.get(1).getName());
    }

    @Test
    public void respectsResultLimit() {
        InMemoryPlaceRepository repository = createRepository();

        List<Place> result = repository.findByNamePrefix("old", 1);

        assertEquals(1, result.size());
        assertEquals("Oldenburg", result.get(0).getName());
    }

    @Test
    public void findsPlaceByGeoNameId() {
        InMemoryPlaceRepository repository = createRepository();

        Optional<Place> result = repository.findByGeoNameId(oldenburg.getGeonameId());

        assertTrue(result.isPresent());
        assertEquals("Oldenburg", result.get().getName());
    }

    @Test
    public void returnsEmptyOptionalForUnknownGeoNameId() {
        InMemoryPlaceRepository repository = createRepository();

        Optional<Place> result = repository.findByGeoNameId(999999999L);

        assertFalse(result.isPresent());
    }
}
