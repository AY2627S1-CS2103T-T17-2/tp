package trackflow.storage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import trackflow.commons.exceptions.DataLoadingException;
import trackflow.commons.util.JsonUtil;
import trackflow.model.ReadOnlyUserPrefs;
import trackflow.model.UserPrefs;

/**
 * A class to access UserPrefs stored on the hard disk as a JSON file
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
     * @throws DataLoadingException If the file cannot be read or its JSON cannot be converted to preferences.
     */
    public Optional<UserPrefs> readUserPrefs(Path prefsFilePath) throws DataLoadingException {
        Optional<UserPrefs> userPrefs = JsonUtil.readJsonFile(prefsFilePath, UserPrefs.class);
        if (userPrefs.isPresent() && userPrefs.get().getGuiSettings() == null) {
            // A preferences file with "guiSettings": null would otherwise crash start-up; treat it as unreadable
            // so that the caller falls back to the default preferences.
            throw new DataLoadingException(
                    new IllegalArgumentException("Preferences file is missing its GUI settings."));
        }
        return userPrefs;
    }

    /**
     * Saves the given {@link trackflow.model.ReadOnlyUserPrefs} to the storage.
     * @param userPrefs cannot be null.
     * @throws IOException if there was any problem writing to the file.
     */
    public void saveUserPrefs(ReadOnlyUserPrefs userPrefs) throws IOException {
        JsonUtil.saveJsonFile(userPrefs, filePath);
    }

}
