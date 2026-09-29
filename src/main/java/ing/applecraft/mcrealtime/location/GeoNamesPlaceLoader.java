package ing.applecraft.mcrealtime.location;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class GeoNamesPlaceLoader {

    private final GeoNamesPlaceParser parser;

    public GeoNamesPlaceLoader(GeoNamesPlaceParser parser) {
        if (parser == null) {
            throw new IllegalArgumentException("parser must not be null");
        }

        this.parser = parser;
    }

    public List<Place> load(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            throw new IllegalArgumentException("inputStream must not be null");
        }

        List<Place> places = new ArrayList<>();

        try (ZipInputStream zipInputStream =
                     new ZipInputStream(
                             new BufferedInputStream(inputStream)
                     )) {

            ZipEntry entry;

            while ((entry = zipInputStream.getNextEntry()) != null) {

                if (entry.isDirectory()
                        || !entry.getName().endsWith(".txt")) {
                    continue;
                }

                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        zipInputStream,
                                        StandardCharsets.UTF_8
                                )
                        );

                String line;

                while ((line = reader.readLine()) != null) {
                    Optional<Place> place =
                            parser.parse(line);

                    if (place.isPresent()) {
                        places.add(place.get());
                    }
                }

                return places;
            }
        }

        throw new IOException(
                "GeoNames archive does not contain a text file"
        );
    }
}