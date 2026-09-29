package ing.applecraft.mcrealtime.location;

import ing.applecraft.mcrealtime.astronomy.GeoLocation;

import java.time.ZoneId;

public final class Place {

    private final long geonameId;
    private final String name;
    private final String countryCode;
    private final GeoLocation coordinates;
    private final ZoneId zoneId;
    private final long population;

    public Place(long geonameId,
                 String name,
                 String countryCode,
                 GeoLocation coordinates,
                 ZoneId zoneId,
                 long population) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("name must not be empty");
        }

        if (countryCode == null || countryCode.trim().isEmpty()) {
            throw new IllegalArgumentException("countryCode must not be empty");
        }

        if (coordinates == null) {
            throw new IllegalArgumentException("coordinates must not be null");
        }

        if (zoneId == null) {
            throw new IllegalArgumentException("zoneId must not be null");
        }

        if (population < 0) {
            throw new IllegalArgumentException("population must not be negative");
        }

        this.geonameId = geonameId;
        this.name = name;
        this.countryCode = countryCode;
        this.coordinates = coordinates;
        this.zoneId = zoneId;
        this.population = population;
    }

    public long getGeonameId() {
        return geonameId;
    }

    public String getName() {
        return name;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public GeoLocation getCoordinates() {
        return coordinates;
    }

    public ZoneId getZoneId() {
        return zoneId;
    }

    public long getPopulation() {
        return population;
    }

    @Override
    public String toString() {
        return name + " (" + countryCode + ")";
    }
}
