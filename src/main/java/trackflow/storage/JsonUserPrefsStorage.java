package trackflow.storage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import trackflow.commons.exceptions.DataLoadingException;
import trackflow.commons.util.JsonUtil;
import trackflow.model.ReadOnlyUserPrefs;
import trackflow.model.UserPrefs;

/**
 * A class to access UserPrefs stored on the hard disk as a JSON file.
 */
public class JsonUserPrefsStorage {

    private Path filePath;

    /**
     * Creates storage for user preferences at {@code filePath} without reading or creating the file.
     */
    public JsonUserPrefsStorage(Path filePath) {
        this.filePath = filePath;
    }

    public Path getUserPrefsFilePath() {
        return filePath;
    }

    /**
     * Returns UserPrefs data from storage.
     * Returns {@code Optional.empty()} if storage file is not found.
     *
     * @throws DataLoadingException if the loading of data from preference file failed.
     */
    public Optional<UserPrefs> readUserPrefs() throws DataLoadingException {
        return readUserPrefs(filePath);
    }

    /**
     * Returns preferences from {@code prefsFilePath}, or an empty optional if that file is absent.
     *
     * @param prefsFilePath Location of the preferences; must not be null.
     * @return The loaded preferences, or an empty optional if the file is absent.
     * @throws DataLoadingException If the file cannot be read, its JSON is invalid, or its GUI settings are invalid.
     */
    public Optional<UserPrefs> readUserPrefs(Path prefsFilePath) throws DataLoadingException {
        Optional<UserPrefs> preferences = JsonUtil.readJsonFile(prefsFilePath, UserPrefs.class);
        if (preferences.isPresent()) {
            UserPrefs loaded = preferences.get();
            if (loaded.getGuiSettings() == null || !loaded.getGuiSettings().isValid()) {
                throw new DataLoadingException(new IOException("Saved GUI settings must be present "
                        + "and have finite, positive window dimensions"));
            }
        }
        return preferences;
    }

    /**
     * Saves the given {@link trackflow.model.ReadOnlyUserPrefs} to the storage.
     *
     * @param userPrefs cannot be null.
     * @throws IOException if there was any problem writing to the file.
     */
    public void saveUserPrefs(ReadOnlyUserPrefs userPrefs) throws IOException {
        JsonUtil.saveJsonFile(userPrefs, filePath);
    }

}
