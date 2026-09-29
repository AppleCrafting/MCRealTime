package ing.applecraft.mcrealtime.location;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class LocationSearchService {

    private static final int MIN_QUERY_LENGTH = 2;
    private static final int MAX_SUGGESTIONS = 10;

    private final PlaceRepository repository;

    public LocationSearchService(PlaceRepository repository) {
        if (repository == null) {
            throw new IllegalArgumentException("repository must not be null");
        }
        this.repository = repository;
    }

    public List<Place> suggestedPlaces(String query) {
        if (query == null) {
            return Collections.emptyList();
        }

        String normalizedQuery = query.trim();

        if (normalizedQuery.length() < MIN_QUERY_LENGTH) {
            return Collections.emptyList();
        }

        return repository.findByNamePrefix(normalizedQuery, MAX_SUGGESTIONS);
    }

    public Optional<Place> findByGeoNameId(long geonameId) {
        return repository.findByGeoNameId(geonameId);
    }
}
