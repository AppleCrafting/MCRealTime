package ing.applecraft.mcrealtime.command;

import ing.applecraft.mcrealtime.astronomy.GeoLocation;
import ing.applecraft.mcrealtime.location.Place;
import org.junit.Test;

import java.time.ZoneId;

import static org.junit.Assert.assertEquals;

public class PlaceCommandCodecTest {

    @Test
    public void encodesAndDecodesPlace() {
        Place place = new Place(
                2950159L,
                "Oldenburg",
                "DE",
                new GeoLocation(
                        53.14118,
                        8.21467
                ),
                ZoneId.of("Europe/Berlin"),
                170000L
        );

        PlaceCommandCodec codec =
                new PlaceCommandCodec();

        String encoded = codec.encode(place);

        assertEquals(
                "Oldenburg_DE_2950159",
                encoded
        );

        assertEquals(
                2950159L,
                codec.decodeGeoNameId(encoded)
        );
    }
}
