package trackflow.storage;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Logger;

import trackflow.commons.core.LogsCenter;
import trackflow.commons.exceptions.DataLoadingException;
import trackflow.commons.exceptions.IllegalValueException;
import trackflow.commons.util.JsonUtil;
import trackflow.model.ReadOnlyRoster;

/**
 * A class to access AthleteRoster data stored as a JSON file on the hard disk.
 */
public class JsonRosterStorage {

    private static final Logger logger = LogsCenter.getLogger(JsonRosterStorage.class);

    private Path filePath;

    public JsonRosterStorage(Path filePath) {
        this.filePath = filePath;
    }

    public Path getRosterFilePath() {
        return filePath;
    }

    /**
     * Returns AthleteRoster data as a {@link ReadOnlyRoster}.
     * Returns {@code Optional.empty()} if storage file is not found.
     *
     * @throws DataLoadingException if loading the data from storage failed.
     */
    public Optional<ReadOnlyRoster> readRoster() throws DataLoadingException {
        return readRoster(filePath);
    }

    /**
     * Similar to {@link #readRoster()}.
     *
     * @param filePath location of the data. Cannot be null.
     * @throws DataLoadingException if loading the data from storage failed.
     */
    public Optional<ReadOnlyRoster> readRoster(Path filePath) throws DataLoadingException {
        requireNonNull(filePath);

        Optional<JsonSerializableRoster> jsonRoster = JsonUtil.readJsonFile(
                filePath, JsonSerializableRoster.class);
        if (!jsonRoster.isPresent()) {
            return Optional.empty();
        }

        try {
            return Optional.of(jsonRoster.get().toModelType());
        } catch (IllegalValueException ive) {
            logger.info("Illegal values found in " + filePath + ": " + ive.getMessage());
            throw new DataLoadingException(ive);
        }
    }

    /**
     * Saves the given {@link ReadOnlyRoster} to the storage.
     * @param roster cannot be null.
     * @throws IOException if there was any problem writing to the file.
     */
    public void saveRoster(ReadOnlyRoster roster) throws IOException {
        saveRoster(roster, filePath);
    }

    /**
     * Similar to {@link #saveRoster(ReadOnlyRoster)}.
     *
     * @param filePath location of the data. Cannot be null.
     */
    public void saveRoster(ReadOnlyRoster roster, Path filePath) throws IOException {
        requireNonNull(roster);
        requireNonNull(filePath);

        JsonUtil.saveJsonFile(new JsonSerializableRoster(roster), filePath);
    }

}
