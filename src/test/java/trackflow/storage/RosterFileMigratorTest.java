package trackflow.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class RosterFileMigratorTest {

    @TempDir
    public Path temporaryFolder;

    @Test
    public void migrateIfNeeded_legacyRosterExists_copiesDataAndRetainsLegacyFile() throws Exception {
        Path legacyFile = temporaryFolder.resolve("legacy.json");
        Path rosterFile = temporaryFolder.resolve("data/trackflow.json");
        Files.createDirectories(rosterFile.getParent());
        Files.writeString(legacyFile, "legacy roster");

        Path selectedFile = RosterFileMigrator.migrateIfNeeded(legacyFile, rosterFile);

        assertEquals(rosterFile, selectedFile);
        assertEquals("legacy roster", Files.readString(rosterFile));
        assertTrue(Files.exists(legacyFile));
    }

    @Test
    public void migrateIfNeeded_currentRosterExists_keepsCurrentRoster() throws Exception {
        Path legacyFile = temporaryFolder.resolve("legacy.json");
        Path rosterFile = temporaryFolder.resolve("trackflow.json");
        Files.writeString(legacyFile, "legacy roster");
        Files.writeString(rosterFile, "current roster");

        Path selectedFile = RosterFileMigrator.migrateIfNeeded(legacyFile, rosterFile);

        assertEquals(rosterFile, selectedFile);
        assertEquals("current roster", Files.readString(rosterFile));
    }

    @Test
    public void migrateIfNeeded_noFiles_returnsCurrentPathWithoutCreatingIt() throws Exception {
        Path legacyFile = temporaryFolder.resolve("legacy.json");
        Path rosterFile = temporaryFolder.resolve("trackflow.json");

        Path selectedFile = RosterFileMigrator.migrateIfNeeded(legacyFile, rosterFile);

        assertEquals(rosterFile, selectedFile);
        assertFalse(Files.exists(rosterFile));
    }
}
