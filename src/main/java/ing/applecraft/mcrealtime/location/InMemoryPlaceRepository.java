package ing.applecraft.mcrealtime.location;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class InMemoryPlaceRepository implements PlaceRepository {

    private final List<Place> places;

    public InMemoryPlaceRepository(List<Place> places) {
        if (places == null) {
            throw new IllegalArgumentException("places must not be null");
        }

        this.places = new ArrayList<>(places);
    }

    @Override
    public List<Place> findByNamePrefix(String prefix, int limit) {
        if (prefix == null) {
            throw new IllegalArgumentException("prefix must not be null");
        }

        if (limit <= 0) {
            return Collections.emptyList();
        }

        String normalizedPrefix = prefix.trim().toLowerCase(Locale.ROOT);

        if (normalizedPrefix.isEmpty()) {
            return Collections.emptyList();
        }

        List<Place> matches = new ArrayList<Place>();

        for (Place place : places) {
            String normalized = place.getName().toLowerCase(Locale.ROOT);

            if (normalized.startsWith(normalizedPrefix)) {
                matches.add(place);
            }
        }

        matches.sort(
                Comparator.comparingLong(Place::getPopulation)
                        .reversed()
        );

        if (matches.size() <= limit) {
            return matches;
        }

        return new ArrayList<Place>(
                matches.subList(0, limit)
        );
    }

    @Override
    public Optional<Place> findByGeoNameId(long geonameId) {
        for (Place place : places) {
            if (place.getGeonameId() == geonameId) {
                return Optional.of(place);
            }
        }

        return Optional.empty();
    }
}
