package ing.applecraft.mcrealtime.update;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class PluginVersion
        implements Comparable<PluginVersion> {

    private final List<Integer> numbers;
    private final String qualifier;

    private PluginVersion(
            List<Integer> numbers,
            String qualifier) {

        this.numbers =
                Collections.unmodifiableList(
                        new ArrayList<Integer>(numbers)
                );

        this.qualifier = qualifier;
    }

    public static PluginVersion parse(
            String value) {

        if (value == null) {
            throw new IllegalArgumentException(
                    "version must not be null"
            );
        }

        String normalized =
                value.trim();

        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(
                    "version must not be empty"
            );
        }

        /*
         * GitHub tags are commonly written as:
         *
         * v4.8
         *
         * while plugin.yml may contain:
         *
         * 4.8
         */
        if (normalized.startsWith("v")
                || normalized.startsWith("V")) {

            normalized =
                    normalized.substring(1);
        }

        String numberPart;
        String qualifier = null;

        int qualifierSeparator =
                normalized.indexOf('-');

        if (qualifierSeparator >= 0) {

            numberPart =
                    normalized.substring(
                            0,
                            qualifierSeparator
                    );

            qualifier =
                    normalized.substring(
                            qualifierSeparator + 1
                    ).trim();

            if (qualifier.isEmpty()) {
                qualifier = null;
            }

        } else {

            numberPart = normalized;
        }

        String[] parts =
                numberPart.split("\\.");

        if (parts.length == 0) {
            throw new IllegalArgumentException(
                    "Invalid version: " + value
            );
        }

        List<Integer> numbers =
                new ArrayList<Integer>();

        for (String part : parts) {

            if (part.isEmpty()) {
                throw new IllegalArgumentException(
                        "Invalid version: " + value
                );
            }

            try {

                numbers.add(
                        Integer.parseInt(part)
                );

            } catch (NumberFormatException exception) {

                throw new IllegalArgumentException(
                        "Invalid version: " + value,
                        exception
                );
            }
        }

        /*
         * Make versions such as 4.8 and 4.8.0 equivalent.
         *
         * At least three components are convenient for:
         *
         * major.minor.patch
         */
        while (numbers.size() < 3) {
            numbers.add(0);
        }

        /*
         * Remove unnecessary trailing zeros after
         * major.minor.patch.
         *
         * 4.8.0.0 becomes equivalent to 4.8.0.
         */
        while (numbers.size() > 3
                && numbers.get(numbers.size() - 1) == 0) {

            numbers.remove(
                    numbers.size() - 1
            );
        }

        return new PluginVersion(
                numbers,
                qualifier
        );
    }

    public boolean isPreRelease() {
        return qualifier != null;
    }

    public String getQualifier() {
        return qualifier;
    }

    @Override
    public int compareTo(
            PluginVersion other) {

        if (other == null) {
            throw new IllegalArgumentException(
                    "other version must not be null"
            );
        }

        int maximumLength =
                Math.max(
                        numbers.size(),
                        other.numbers.size()
                );

        for (int index = 0;
             index < maximumLength;
             index++) {

            int left =
                    numberAt(index);

            int right =
                    other.numberAt(index);

            int comparison =
                    Integer.compare(
                            left,
                            right
                    );

            if (comparison != 0) {
                return comparison;
            }
        }

        /*
         * Same numeric version:
         *
         * 4.8-SNAPSHOT < 4.8
         *
         * A final/stable release is newer than a
         * prerelease with the same numeric version.
         */
        if (qualifier == null
                && other.qualifier == null) {

            return 0;
        }

        if (qualifier == null) {
            return 1;
        }

        if (other.qualifier == null) {
            return -1;
        }

        return compareQualifiers(
                qualifier,
                other.qualifier
        );
    }

    private int numberAt(
            int index) {

        if (index >= numbers.size()) {
            return 0;
        }

        return numbers.get(index);
    }

    private int compareQualifiers(
            String left,
            String right) {

        String normalizedLeft =
                left.toLowerCase(Locale.ROOT);

        String normalizedRight =
                right.toLowerCase(Locale.ROOT);

        int leftRank =
                qualifierRank(
                        normalizedLeft
                );

        int rightRank =
                qualifierRank(
                        normalizedRight
                );

        int rankComparison =
                Integer.compare(
                        leftRank,
                        rightRank
                );

        if (rankComparison != 0) {
            return rankComparison;
        }

        return normalizedLeft.compareTo(
                normalizedRight
        );
    }

    private int qualifierRank(
            String qualifier) {

        if (qualifier.startsWith("snapshot")) {
            return 0;
        }

        if (qualifier.startsWith("alpha")) {
            return 1;
        }

        if (qualifier.startsWith("beta")) {
            return 2;
        }

        if (qualifier.startsWith("rc")) {
            return 3;
        }

        /*
         * Unknown prerelease qualifiers are still
         * considered older than a stable release.
         */
        return 1;
    }

    @Override
    public boolean equals(
            Object object) {

        if (this == object) {
            return true;
        }

        if (!(object instanceof PluginVersion)) {
            return false;
        }

        PluginVersion other =
                (PluginVersion) object;

        return compareTo(other) == 0;
    }

    @Override
    public int hashCode() {

        int result =
                numbers.hashCode();

        result =
                31 * result
                        + (
                        qualifier == null
                                ? 0
                                : qualifier
                                .toLowerCase(Locale.ROOT)
                                .hashCode()
                );

        return result;
    }

    @Override
    public String toString() {

        StringBuilder builder =
                new StringBuilder();

        for (int index = 0;
             index < numbers.size();
             index++) {

            if (index > 0) {
                builder.append('.');
            }

            builder.append(
                    numbers.get(index)
            );
        }

        if (qualifier != null) {

            builder.append('-');
            builder.append(qualifier);
        }

        return builder.toString();
    }
}
