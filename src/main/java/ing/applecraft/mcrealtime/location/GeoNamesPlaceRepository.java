package ing.applecraft.mcrealtime.location;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class GeoNamesPlaceRepository
        implements PlaceRepository {

    private static final int INDEX_PREFIX_LENGTH = 2;

    private final Map<Long, Place> placesById;
    private final Map<String, List<Place>> prefixIndex;

    public GeoNamesPlaceRepository(List<Place> places) {
        if (places == null) {
            throw new IllegalArgumentException(
                    "places must not be null"
            );
        }

        this.placesById = new HashMap<>();
        this.prefixIndex = new HashMap<>();

        buildIndex(places);
    }

    private void buildIndex(List<Place> places) {
        for (Place place : places) {
            placesById.put(
                    place.getGeonameId(),
                    place
            );

            String normalizedName =
                    normalize(place.getName());

            if (normalizedName.length()
                    < INDEX_PREFIX_LENGTH) {
                continue;
            }

            String prefix =
                    normalizedName.substring(
                            0,
                            INDEX_PREFIX_LENGTH
                    );

            List<Place> bucket =
                    prefixIndex.get(prefix);

            if (bucket == null) {
                bucket = new ArrayList<>();
                prefixIndex.put(prefix, bucket);
            }

            bucket.add(place);
        }

        Comparator<Place> comparator =
                Comparator
                        .comparingLong(
                                Place::getPopulation
                        )
                        .reversed()
                        .thenComparing(
                                Place::getName,
                                String.CASE_INSENSITIVE_ORDER
                        );

        for (List<Place> bucket
                : prefixIndex.values()) {

            bucket.sort(comparator);
        }
    }

    @Override
    public List<Place> findByNamePrefix(
            String prefix,
            int limit) {

        if (prefix == null) {
            throw new IllegalArgumentException(
                    "prefix must not be null"
            );
        }

        if (limit <= 0) {
            return Collections.emptyList();
        }

        String normalizedPrefix =
                normalize(prefix.trim());

        if (normalizedPrefix.length()
                < INDEX_PREFIX_LENGTH) {

            return Collections.emptyList();
        }

        String indexKey =
                normalizedPrefix.substring(
                        0,
                        INDEX_PREFIX_LENGTH
                );

        List<Place> bucket =
                prefixIndex.get(indexKey);

        if (bucket == null) {
            return Collections.emptyList();
        }

        List<Place> matches =
                new ArrayList<>();

        for (Place place : bucket) {

            String normalizedName =
                    normalize(place.getName());

            if (normalizedName.startsWith(
                    normalizedPrefix)) {

                matches.add(place);

                if (matches.size() >= limit) {
                    break;
                }
            }
        }

        return matches;
    }

    @Override
    public Optional<Place> findByGeoNameId(
            long geonameId) {

        return Optional.ofNullable(
                placesById.get(geonameId)
        );
    }

    private String normalize(String value) {
        return value.toLowerCase(Locale.ROOT);
    }
}