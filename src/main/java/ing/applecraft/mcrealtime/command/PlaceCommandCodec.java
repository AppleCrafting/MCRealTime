package ing.applecraft.mcrealtime.command;

import ing.applecraft.mcrealtime.location.Place;

public final class PlaceCommandCodec {

    public String encode(Place place) {
        if (place == null) {
            throw new IllegalArgumentException("place must not be null");
        }

        String name = place.getName()
                .trim()
                .replace(' ', '_');

        return name
                + "_"
                + place.getCountryCode()
                + "_"
                + place.getGeonameId();
    }

    public long decodeGeoNameId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "place token must not be empty"
            );
        }

        int separator = value.lastIndexOf('_');

        if (separator < 0
                || separator == value.length() - 1) {
            throw new IllegalArgumentException(
                    "Invalid place token: " + value
            );
        }

        String idPart =
                value.substring(separator + 1);

        try {
            return Long.parseLong(idPart);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Invalid GeoNames ID in place token: "
                            + value,
                    exception
            );
        }
    }
}
