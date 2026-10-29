// @@author Austin-Chia
// Unassigned sections are adapted from SE-EDU AddressBook-Level3.
package trackflow.storage;

import static trackflow.commons.util.CollectionUtil.requireAllNonNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Logger;

import trackflow.commons.core.LogsCenter;
import trackflow.commons.exceptions.DataLoadingException;
import trackflow.model.ReadOnlyRoster;
import trackflow.model.ReadOnlyUserPrefs;
import trackflow.model.UserPrefs;

/**
 * Manages storage of AthleteRoster data in local storage.
 */
public class StorageManager implements Storage {

    private static final Logger logger = LogsCenter.getLogger(StorageManager.class);
    private JsonRosterStorage rosterStorage;
    private JsonUserPrefsStorage userPrefsStorage;

    /**
     * Creates a {@code StorageManager} with the given athlete roster and user prefs storage.
     *
     * @throws NullPointerException If either storage component is null.
     */
    public StorageManager(JsonRosterStorage rosterStorage, JsonUserPrefsStorage userPrefsStorage) {
        requireAllNonNull(rosterStorage, userPrefsStorage);
        this.rosterStorage = rosterStorage;
        this.userPrefsStorage = userPrefsStorage;
    }

    // ================ UserPrefs methods ==============================

    @Override
    public Path getUserPrefsFilePath() {
        return userPrefsStorage.getUserPrefsFilePath();
    }

    @Override
    public Optional<UserPrefs> readUserPrefs() throws DataLoadingException {
        return userPrefsStorage.readUserPrefs();
    }

    @Override
    public void saveUserPrefs(ReadOnlyUserPrefs userPrefs) throws IOException {
        userPrefsStorage.saveUserPrefs(userPrefs);
    }


    // ================ AthleteRoster methods ==============================

    @Override
    public Path getRosterFilePath() {
        return rosterStorage.getRosterFilePath();
    }

    @Override
    public Optional<ReadOnlyRoster> readRoster() throws DataLoadingException {
        logger.fine("Attempting to read data from file: " + rosterStorage.getRosterFilePath());
        return rosterStorage.readRoster();
    }

    @Override
    public void saveRoster(ReadOnlyRoster roster) throws IOException {
        logger.fine("Attempting to write to data file: " + rosterStorage.getRosterFilePath());
        rosterStorage.saveRoster(roster);
    }

}
// @@author
