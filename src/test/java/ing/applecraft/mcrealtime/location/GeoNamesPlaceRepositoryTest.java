package ing.applecraft.mcrealtime.location;

import ing.applecraft.mcrealtime.astronomy.GeoLocation;
import org.junit.Test;

import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GeoNamesPlaceRepositoryTest {

    @Test
    public void searchesIndexedPlaces() {

        Place oldenburg = new Place(
                1L,
                "Oldenburg",
                "DE",
                new GeoLocation(
                        53.1412,
                        8.2147
                ),
                ZoneId.of("Europe/Berlin"),
                170000L
        );

        Place oldenburgInHolstein =
                new Place(
                        2L,
                        "Oldenburg in Holstein",
                        "DE",
                        new GeoLocation(
                                54.2958,
                                10.9016
                        ),
                        ZoneId.of(
                                "Europe/Berlin"
                        ),
                        10000L
                );

        Place berlin = new Place(
                3L,
                "Berlin",
                "DE",
                new GeoLocation(
                        52.5200,
                        13.4050
                ),
                ZoneId.of("Europe/Berlin"),
                3600000L
        );

        PlaceRepository repository =
                new GeoNamesPlaceRepository(
                        Arrays.asList(
                                oldenburgInHolstein,
                                berlin,
                                oldenburg
                        )
                );

        List<Place> result =
                repository.findByNamePrefix(
                        "old",
                        10
                );

        assertEquals(2, result.size());

        assertEquals(
                "Oldenburg",
                result.get(0).getName()
        );

        assertEquals(
                "Oldenburg in Holstein",
                result.get(1).getName()
        );

        assertTrue(
                repository
                        .findByGeoNameId(1L)
                        .isPresent()
        );
    }
}