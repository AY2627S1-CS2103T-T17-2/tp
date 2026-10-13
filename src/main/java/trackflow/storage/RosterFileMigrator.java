package trackflow.storage;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import trackflow.commons.util.FileUtil;

/** Migrates the legacy roster filename without risking the existing data. */
public final class RosterFileMigrator {

    private RosterFileMigrator() {}

    /**
     * Copies the legacy roster to {@code rosterFile} when the new file does not exist yet.
     * The legacy file is retained as a backup. If no migration is needed, this returns {@code rosterFile} unchanged.
     */
    public static Path migrateIfNeeded(Path legacyFile, Path rosterFile) throws IOException {
        requireNonNull(legacyFile);
        requireNonNull(rosterFile);

        if (Files.exists(rosterFile) || !Files.exists(legacyFile)) {
            return rosterFile;
        }

        String legacyData = Files.readString(legacyFile, StandardCharsets.UTF_8);
        FileUtil.writeToFile(rosterFile, legacyData);
        return rosterFile;
    }
}
