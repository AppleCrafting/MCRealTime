package ing.applecraft.mcrealtime.location;

import org.junit.Test;

import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class GeoNamesPlaceLoaderTest {

    @Test
    public void loadsBundledGeoNamesDatabase()
            throws Exception {

        InputStream inputStream =
                getClass().getResourceAsStream(
                        "/geonames/cities5000.zip"
                );

        assertNotNull(inputStream);

        GeoNamesPlaceLoader loader =
                new GeoNamesPlaceLoader(
                        new GeoNamesPlaceParser()
                );

        List<Place> places =
                loader.load(inputStream);

        assertTrue(
                places.size() > 10000
        );
    }

    @Test
    public void canSearchLoadedGeoNamesDatabase()
            throws Exception {

        InputStream inputStream =
                getClass().getResourceAsStream(
                        "/geonames/cities5000.zip"
                );

        assertNotNull(inputStream);

        GeoNamesPlaceLoader loader =
                new GeoNamesPlaceLoader(
                        new GeoNamesPlaceParser()
                );

        List<Place> places =
                loader.load(inputStream);

        PlaceRepository repository =
                new GeoNamesPlaceRepository(places);

        LocationSearchService service =
                new LocationSearchService(repository);

        List<Place> result =
                service.suggestedPlaces("Oldenburg");

        assertTrue(result.size() > 0);
        assertTrue(
                result.get(0)
                        .getName()
                        .toLowerCase()
                        .startsWith("oldenburg")
        );
    }
}