package ing.applecraft.mcrealtime.location;

import ing.applecraft.mcrealtime.astronomy.GeoLocation;
import org.junit.Test;

import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LocationSearchServiceTest {

    private LocationSearchService createService() {

        Place oldenburg = new Place(
                2950159L,
                "Oldenburg",
                "DE",
                new GeoLocation(53.1412, 8.2147),
                ZoneId.of("Europe/Berlin"),
                170000L
        );

        Place oldenburgInHolstein = new Place(
                2857458L,
                "Oldenburg in Holstein",
                "DE",
                new GeoLocation(54.2958, 10.9016),
                ZoneId.of("Europe/Berlin"),
                10000L
        );

        PlaceRepository repository =
                new InMemoryPlaceRepository(
                        Arrays.asList(
                                oldenburg,
                                oldenburgInHolstein
                        )
                );
        return new LocationSearchService(repository);
    }

    @Test
    public void returnNoSuggestionsForEmptyQuery() {
        LocationSearchService service = createService();

        assertTrue(service.suggestedPlaces("").isEmpty());
    }

    @Test
    public void returnNoSuggestionsForOneCharacter() {
        LocationSearchService service = createService();

        assertTrue(service.suggestedPlaces("o").isEmpty());
    }

    @Test
    public void returnSuggestionsForValidQuery() {
        LocationSearchService service = createService();

        List<Place> results = service.suggestedPlaces("old");

        assertEquals(2, results.size());
        assertEquals("Oldenburg", results.get(0).getName());

    }
}
