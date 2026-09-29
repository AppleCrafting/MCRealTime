package ing.applecraft.mcrealtime.location;

import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GeoNamesPlaceParserTest {

    @Test
    public void parseValidGeoNamesLine() {
        GeoNamesPlaceParser parser = new GeoNamesPlaceParser();

        String line =
                "2950159\t" +
                "Oldenburg\t" +
                "Oldenburg\t" +
                "Oldenburg\t" +
                "53.14118\t" +
                "8.21467\t" +
                "P\t" +
                "PPLA2\t" +
                "DE\t" +
                "\t" +
                "06\t" +
                "\t" +
                "\t" +
                "\t" +
                "170000\t" +
                "\t" +
                "4\t" +
                "Europe/Berlin\t" +
                "2026-01-01";

        Optional<Place> result =
                parser.parse(line);

        assertTrue(result.isPresent());

        Place place = result.get();

        assertEquals(2950159L, place.getGeonameId());
        assertEquals("Oldenburg", place.getName());
        assertEquals("DE", place.getCountryCode());
        assertEquals(170000L, place.getPopulation());
        assertEquals(
                "Europe/Berlin",
                place.getZoneId().getId()
        );
    }

    @Test
    public void rejectsInvalidLine() {
        GeoNamesPlaceParser parser = new GeoNamesPlaceParser();

        Optional<Place> result =
                parser.parse("not a valid GeoNames line");
    }
}
