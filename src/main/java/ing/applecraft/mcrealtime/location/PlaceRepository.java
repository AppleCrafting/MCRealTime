package ing.applecraft.mcrealtime.location;

import java.util.List;
import java.util.Optional;

public interface PlaceRepository {

    List<Place> findByNamePrefix(String prefix, int limit);

    Optional<Place> findByGeoNameId(long geonameId);
}
