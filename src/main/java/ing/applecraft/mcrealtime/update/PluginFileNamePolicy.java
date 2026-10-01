package ing.applecraft.mcrealtime.update;

import java.io.File;

public final class PluginFileNamePolicy {

    public static final String CANONICAL_FILE_NAME =
            "MCRealTime.jar";

    public boolean isCanonical(
            File pluginFile) {

        if (pluginFile == null) {
            throw new IllegalArgumentException(
                    "pluginFile must not be null"
            );
        }

        return isCanonicalFileName(
                pluginFile.getName()
        );
    }

    public boolean isCanonicalFileName(
            String fileName) {

        if (fileName == null
                || fileName.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "fileName must not be empty"
            );
        }

        return CANONICAL_FILE_NAME
                .equals(fileName);
    }

    public String getCanonicalFileName() {
        return CANONICAL_FILE_NAME;
    }
}
