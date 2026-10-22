package trackflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static trackflow.testutil.Assert.assertThrows;
import static trackflow.testutil.TypicalPersons.getTypicalRoster;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import trackflow.commons.core.GuiSettings;
import trackflow.commons.exceptions.DataLoadingException;
import trackflow.model.AthleteRoster;
import trackflow.model.Model;
import trackflow.model.ReadOnlyRoster;
import trackflow.model.UserPrefs;
import trackflow.model.util.SampleDataUtil;
import trackflow.storage.JsonRosterStorage;
import trackflow.storage.JsonUserPrefsStorage;
import trackflow.storage.Storage;
import trackflow.storage.StorageManager;

public class MainAppTest {
    @TempDir
    public Path testFolder;

    @Test
    public void initModelManager_unreadableRoster_preservesEachOriginalBeforeReplacement() throws Exception {
        Path rosterPath = testFolder.resolve("trackflow.json");
        Storage storage = createStorage(rosterPath);
        List<String> invalidRosters = List.of("{invalid JSON", "{\"persons\":[null]}");
        for (String invalidRoster : invalidRosters) {
            Files.writeString(rosterPath, invalidRoster);
            Model model = new MainApp().initModelManager(storage, new UserPrefs());
            assertEquals(new AthleteRoster(), model.getRoster());
            assertEquals(invalidRoster, Files.readString(rosterPath));

            storage.saveRoster(getTypicalRoster());
            assertEquals(getTypicalRoster(), new MainApp().initModelManager(storage, new UserPrefs()).getRoster());
        }

        try (Stream<Path> files = Files.list(testFolder)) {
            List<Path> backups = files.filter(path -> path.getFileName().toString().endsWith(".bak")).toList();
            assertEquals(invalidRosters.size(), backups.size());
            for (String original : invalidRosters) {
                boolean found = false;
                for (Path backup : backups) {
                    found |= original.equals(Files.readString(backup));
                }
                assertTrue(found);
            }
        }
    }

    @Test
    public void initModelManager_backupFailure_stopsStartupAndPreservesOriginal() throws Exception {
        Path rosterPath = testFolder.resolve("trackflow.json");
        Files.writeString(rosterPath, "{\"persons\":[null]}");
        // This path cannot be copied: its parent is a regular file. Avoid platform-dependent permission checks.
        Storage storage = new StorageManager(new JsonRosterStorage(rosterPath.resolve("unreadable.json")),
                new JsonUserPrefsStorage(testFolder.resolve("preferences.json"))) {
            @Override
            public Optional<ReadOnlyRoster> readRoster() throws DataLoadingException {
                throw new DataLoadingException(new IOException("Cannot read roster"));
            }
        };
        assertThrows(IOException.class, () -> new MainApp().initModelManager(storage, new UserPrefs()));
        assertEquals("{\"persons\":[null]}", Files.readString(rosterPath));
    }

    @Test
    public void initModelManager_missingRoster_usesSampleWithoutCreatingFile() throws Exception {
        Path rosterPath = testFolder.resolve("missing.json");
        Model model = new MainApp().initModelManager(createStorage(rosterPath), new UserPrefs());
        assertEquals(SampleDataUtil.getSampleRoster(), model.getRoster());
        assertFalse(Files.exists(rosterPath));
    }

    @Test
    public void initModelManager_savedEmptyRoster_remainsEmptyWithoutBackup() throws Exception {
        Path rosterPath = testFolder.resolve("trackflow.json");
        Storage storage = createStorage(rosterPath);
        storage.saveRoster(new AthleteRoster());
        assertEquals(new AthleteRoster(), new MainApp().initModelManager(storage, new UserPrefs()).getRoster());
        try (Stream<Path> files = Files.list(testFolder)) {
            assertEquals(1, files.count());
        }
    }

    @Test
    public void initPrefs_missingOrCorruptedOrValidFile_returnsExpectedPrefsAndSaves() throws Exception {
        Path prefsPath = testFolder.resolve("preferences.json");
        JsonUserPrefsStorage prefsStorage = new JsonUserPrefsStorage(prefsPath);

        // Missing file -> creates default prefs file
        UserPrefs defaultPrefs = new MainApp().initPrefs(prefsStorage);
        assertEquals(new UserPrefs(), defaultPrefs);
        assertTrue(Files.exists(prefsPath));

        // Valid saved prefs -> loads saved prefs
        UserPrefs customPrefs = new UserPrefs();
        customPrefs.setGuiSettings(new GuiSettings(1024, 768, 50, 60));
        prefsStorage.saveUserPrefs(customPrefs);
        assertEquals(customPrefs, new MainApp().initPrefs(prefsStorage));

        // Corrupted prefs file -> falls back to default prefs and overwrites file
        Files.writeString(prefsPath, "{corrupted json");
        assertEquals(new UserPrefs(), new MainApp().initPrefs(prefsStorage));
        assertEquals(new UserPrefs(), prefsStorage.readUserPrefs().orElseThrow());
    }

    private Storage createStorage(Path rosterPath) {
        return new StorageManager(new JsonRosterStorage(rosterPath),
                new JsonUserPrefsStorage(testFolder.resolve("preferences.json")));
    }
}
