package trackflow.storage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import trackflow.commons.exceptions.DataLoadingException;
import trackflow.model.ReadOnlyRoster;
import trackflow.model.ReadOnlyUserPrefs;
import trackflow.model.UserPrefs;

/**
 * API for loading and saving roster data and user preferences.
 */
public interface Storage {

    /**
     * Returns the file path of the UserPrefs data file.
     */
    Path getUserPrefsFilePath();

    /**
     * Returns UserPrefs data from storage.
     * Returns {@code Optional.empty()} if storage file is not found.
     *
     * @throws DataLoadingException if the loading of data from preference file failed.
     */
    Optional<UserPrefs> readUserPrefs() throws DataLoadingException;

    /**
     * Saves the given {@link trackflow.model.ReadOnlyUserPrefs} to the storage.
     *
     * @param userPrefs cannot be null.
     * @throws IOException if there was any problem writing to the file.
     */
    void saveUserPrefs(ReadOnlyUserPrefs userPrefs) throws IOException;

    /**
     * Returns the file path of the AthleteRoster data file.
     */
    Path getRosterFilePath();

    /**
     * Returns saved roster data after validating its structure, athlete fields, and duplicate identities.
     * Returns {@code Optional.empty()} only when the data file is absent; a saved empty roster is still present.
     * Reading does not replace or repair the source file.
     *
     * @throws DataLoadingException If the file cannot be read or contains invalid roster data.
     */
    Optional<ReadOnlyRoster> readRoster() throws DataLoadingException;

    /**
     * Saves the given {@link ReadOnlyRoster} to the storage.
     *
     * @param roster cannot be null.
     * @throws IOException if there was any problem writing to the file.
     */
    void saveRoster(ReadOnlyRoster roster) throws IOException;

}
