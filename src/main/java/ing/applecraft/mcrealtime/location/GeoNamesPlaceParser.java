package ing.applecraft.mcrealtime.location;

import ing.applecraft.mcrealtime.astronomy.GeoLocation;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.Optional;

public final class GeoNamesPlaceParser {

    public Optional<Place> parse(String line) {
        if (line == null || line.trim().isEmpty()) {
            return Optional.empty();
        }

        String[] columns = line.split("\t", -1);

        if (columns.length < 18) {
            return Optional.empty();
        }

        try {
            long geonameId = Long.parseLong(columns[0]);
            String name = columns[1];

            double latitude = Double.parseDouble(columns[4]);
            double longitude = Double.parseDouble(columns[5]);

            String countryCode = columns[8];

            long population = columns[14].isEmpty()
                    ? 0L
                    : Long.parseLong(columns[14]);

            ZoneId zoneId = ZoneId.of(columns[17]);

            Place place = new Place(
                    geonameId,
                    name,
                    countryCode,
                    new GeoLocation(latitude, longitude),
                    zoneId,
                    population
            );

            return Optional.of(place);
        } catch (NumberFormatException | DateTimeException exception) {
            return Optional.empty();
        }
    }
}
