package trackflow.commons.util;

import static trackflow.testutil.Assert.assertThrows;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import trackflow.logic.LogicManager;
import trackflow.logic.parser.Prefix;
import trackflow.model.ModelManager;
import trackflow.storage.JsonRosterStorage;
import trackflow.storage.JsonUserPrefsStorage;
import trackflow.storage.StorageManager;

public class RequiredAssociationsTest {
    @Test
    public void constructors_missingDependency_rejectsImmediately() {
        JsonRosterStorage rosterStorage = new JsonRosterStorage(Path.of("roster.json"));
        JsonUserPrefsStorage prefsStorage = new JsonUserPrefsStorage(Path.of("preferences.json"));
        StorageManager storage = new StorageManager(rosterStorage, prefsStorage);

        // Invalid object graphs must fail at construction rather than during a later command or save.
        assertThrows(NullPointerException.class, () -> new LogicManager(null, storage));
        assertThrows(NullPointerException.class, () -> new LogicManager(new ModelManager(), null));
        assertThrows(NullPointerException.class, () -> new StorageManager(null, prefsStorage));
        assertThrows(NullPointerException.class, () -> new StorageManager(rosterStorage, null));
        assertThrows(NullPointerException.class, () -> new JsonRosterStorage(null));
        assertThrows(NullPointerException.class, () -> new JsonUserPrefsStorage(null));
        assertThrows(NullPointerException.class, () -> new Prefix(null));
    }
}
