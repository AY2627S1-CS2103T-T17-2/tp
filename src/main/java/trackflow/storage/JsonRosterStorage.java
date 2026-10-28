// @@author -
// Unassigned sections are adapted from SE-EDU AddressBook-Level3.
// @@author qiyuannn
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

    // @@author chetangent
    /**
     * Creates storage that reads and writes roster data at {@code filePath}.
     * The file is not read or created until a read or save operation is requested.
     */
    // @@author -
    public JsonRosterStorage(Path filePath) {
        this.filePath = filePath;
    }

    public Path getRosterFilePath() {
        return filePath;
    }

    // @@author chetangent
    /**
     * Returns validated roster data from the configured file without changing the file.
     * An absent file returns {@code Optional.empty()}; an existing empty roster returns a present roster.
     *
     * @throws DataLoadingException If reading, JSON conversion, field validation, or duplicate checks fail.
     */
    // @@author -
    public Optional<ReadOnlyRoster> readRoster() throws DataLoadingException {
        return readRoster(filePath);
    }

    // @@author chetangent
    /**
     * Returns validated roster data from {@code filePath}, following the same rules as {@link #readRoster()}.
     *
     * @param filePath Location of the data; must not be null.
     * @return The loaded roster, or an empty optional if the file is absent.
     * @throws DataLoadingException If reading, JSON conversion, field validation, or duplicate checks fail.
     */
    // @@author -
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

    // @@author chetangent
    /**
     * Saves the given {@link ReadOnlyRoster} to the storage.
     *
     * @param roster Roster to save; must not be null.
     * @throws IOException If there is a problem writing or replacing the file.
     */
    // @@author -
    public void saveRoster(ReadOnlyRoster roster) throws IOException {
        saveRoster(roster, filePath);
    }

    // @@author chetangent
    /**
     * Saves the roster as JSON at {@code filePath}, creating parent directories as needed.
     * Writes to a temporary sibling before replacing the destination; replacement is atomic when supported.
     *
     * @param roster Roster to save; must not be null.
     * @param filePath Destination of the data; must not be null.
     * @throws IOException If there is a problem writing or replacing the file.
     */
    // @@author -
    public void saveRoster(ReadOnlyRoster roster, Path filePath) throws IOException {
        requireNonNull(roster);
        requireNonNull(filePath);

        JsonUtil.saveJsonFile(new JsonSerializableRoster(roster), filePath);
    }

}
// @@author
