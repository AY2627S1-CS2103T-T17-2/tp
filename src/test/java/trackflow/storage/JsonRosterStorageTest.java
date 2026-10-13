package trackflow.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static trackflow.testutil.Assert.assertThrows;
import static trackflow.testutil.TypicalPersons.ALICE;
import static trackflow.testutil.TypicalPersons.HOON;
import static trackflow.testutil.TypicalPersons.IDA;
import static trackflow.testutil.TypicalPersons.getTypicalRoster;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import trackflow.commons.exceptions.DataLoadingException;
import trackflow.model.AthleteRoster;
import trackflow.model.ReadOnlyRoster;

public class JsonRosterStorageTest {
    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonRosterStorageTest");

    @TempDir
    public Path testFolder;

    @Test
    public void readRoster_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> readRoster(null));
    }

    private java.util.Optional<ReadOnlyRoster> readRoster(String filePath) throws Exception {
        return new JsonRosterStorage(Paths.get(filePath)).readRoster(addToTestDataPathIfNotNull(filePath));
    }

    private Path addToTestDataPathIfNotNull(String prefsFileInTestDataFolder) {
        return prefsFileInTestDataFolder != null
                ? TEST_DATA_FOLDER.resolve(prefsFileInTestDataFolder)
                : null;
    }

    @Test
    public void read_missingFile_emptyResult() throws Exception {
        assertFalse(readRoster("NonExistentFile.json").isPresent());
    }

    @Test
    public void read_notJsonFormat_exceptionThrown() {
        assertThrows(DataLoadingException.class, () -> readRoster("notJsonFormatAthleteRoster.json"));
    }

    @Test
    public void readRoster_invalidPersonAthleteRoster_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readRoster("invalidPersonAthleteRoster.json"));
    }

    @Test
    public void readRoster_invalidAndValidPersonAthleteRoster_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readRoster("invalidAndValidPersonAthleteRoster.json"));
    }

    @Test
    public void readRoster_invalidSavedAgeCategory_throwsDataLoadingException() throws Exception {
        Path filePath = testFolder.resolve("invalidCategory.json");
        for (String category : new String[] {"", "Under 15"}) {
            Files.writeString(filePath, """
                    {"persons": [{"name": "Avery Tan", "ageCategory": "%s", "phone": "91234567",
                    "email": "avery.tan@example.com", "address": "", "remark": "", "tags": []}]}
                    """.formatted(category));
            JsonRosterStorage storage = new JsonRosterStorage(filePath);
            assertThrows(DataLoadingException.class, storage::readRoster);
        }
    }

    @Test
    public void readAndSaveAthleteRoster_allInOrder_success() throws Exception {
        Path filePath = testFolder.resolve("TempAthleteRoster.json");
        AthleteRoster original = getTypicalRoster();
        JsonRosterStorage jsonRosterStorage = new JsonRosterStorage(filePath);

        // Save in new file and read back
        jsonRosterStorage.saveRoster(original, filePath);
        ReadOnlyRoster readBack = jsonRosterStorage.readRoster(filePath).get();
        assertEquals(original, new AthleteRoster(readBack));

        // Modify data, overwrite existing file, and read back
        original.addPerson(HOON);
        original.removePerson(ALICE);
        jsonRosterStorage.saveRoster(original, filePath);
        readBack = jsonRosterStorage.readRoster(filePath).get();
        assertEquals(original, new AthleteRoster(readBack));

        // Save and read without specifying file path
        original.addPerson(IDA);
        jsonRosterStorage.saveRoster(original); // file path not specified
        readBack = jsonRosterStorage.readRoster().get(); // file path not specified
        assertEquals(original, new AthleteRoster(readBack));

    }

    @Test
    public void saveRoster_nullAthleteRoster_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveRoster(null, "SomeFile.json"));
    }

    /**
     * Saves {@code roster} at the specified {@code filePath}.
     */
    private void saveRoster(ReadOnlyRoster roster, String filePath) {
        try {
            new JsonRosterStorage(Paths.get(filePath))
                    .saveRoster(roster, addToTestDataPathIfNotNull(filePath));
        } catch (IOException ioe) {
            throw new AssertionError("There should not be an error writing to the file.", ioe);
        }
    }

    @Test
    public void saveRoster_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveRoster(new AthleteRoster(), null));
    }
}
