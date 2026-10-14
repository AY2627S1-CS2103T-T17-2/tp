package trackflow;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.stage.Stage;
import trackflow.commons.core.LogsCenter;
import trackflow.commons.exceptions.DataLoadingException;
import trackflow.commons.util.StringUtil;
import trackflow.logic.Logic;
import trackflow.logic.LogicManager;
import trackflow.model.AthleteRoster;
import trackflow.model.Model;
import trackflow.model.ModelManager;
import trackflow.model.ReadOnlyRoster;
import trackflow.model.ReadOnlyUserPrefs;
import trackflow.model.UserPrefs;
import trackflow.model.util.SampleDataUtil;
import trackflow.storage.JsonRosterStorage;
import trackflow.storage.JsonUserPrefsStorage;
import trackflow.storage.Storage;
import trackflow.storage.StorageManager;
import trackflow.ui.Ui;
import trackflow.ui.UiManager;

/**
 * Runs the application.
 */
public class MainApp extends Application {

    public static final String VERSION = "v1.3";

    private static final Logger logger = LogsCenter.getLogger(MainApp.class);
    private static final Path USER_PREFS_FILE_PATH = Paths.get("preferences.json");
    private static final Path ROSTER_FILE_PATH = Paths.get("data", "trackflow.json");

    protected Ui ui;
    protected Logic logic;
    protected Storage storage;
    protected Model model;

    @Override
    public void init() throws Exception {
        logger.info("=============================[ Initializing TrackFlow ]===============================");
        super.init();

        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(USER_PREFS_FILE_PATH);
        UserPrefs userPrefs = initPrefs(userPrefsStorage);
        JsonRosterStorage rosterStorage = new JsonRosterStorage(ROSTER_FILE_PATH);
        storage = new StorageManager(rosterStorage, userPrefsStorage);

        model = initModelManager(storage, userPrefs);

        logic = new LogicManager(model, storage);

        ui = new UiManager(logic, storage.getRosterFilePath());
    }

    /**
     * Returns a {@code ModelManager} with the data from {@code storage}'s athlete roster and {@code userPrefs}. <br>
     * The data from the sample athlete roster will be used instead if {@code storage}'s athlete roster is not found,
     * or an empty athlete roster will be used instead if errors occur when reading {@code storage}'s athlete roster.
     */
    private Model initModelManager(Storage storage, ReadOnlyUserPrefs userPrefs) {
        logger.info("Using data file : " + storage.getRosterFilePath());

        Optional<ReadOnlyRoster> rosterOptional;
        ReadOnlyRoster initialData;
        try {
            rosterOptional = storage.readRoster();
            if (rosterOptional.isEmpty()) {
                logger.info("No saved roster found at " + storage.getRosterFilePath()
                        + ". Starting with the sample athlete roster; the file will be created after the first "
                        + "successful roster change.");
            }
            initialData = rosterOptional.orElseGet(SampleDataUtil::getSampleRoster);
        } catch (DataLoadingException e) {
            logger.warning("Data file at " + storage.getRosterFilePath() + " could not be loaded."
                    + " Will be starting with an empty athlete roster.");
            initialData = new AthleteRoster();
        }

        return new ModelManager(initialData, userPrefs);
    }

    /**
     * Returns a {@code UserPrefs} using the file at {@code storage}'s user prefs file path,
     * or a new {@code UserPrefs} with default configuration if errors occur when
     * reading from the file.
     */
    protected UserPrefs initPrefs(JsonUserPrefsStorage storage) {
        Path prefsFilePath = storage.getUserPrefsFilePath();
        logger.info("Using preference file : " + prefsFilePath);

        UserPrefs initializedPrefs;
        try {
            Optional<UserPrefs> prefsOptional = storage.readUserPrefs();
            if (prefsOptional.isEmpty()) {
                logger.info("Creating new preference file " + prefsFilePath);
            }
            initializedPrefs = prefsOptional.orElse(new UserPrefs());
        } catch (DataLoadingException e) {
            logger.warning("Preference file at " + prefsFilePath + " could not be loaded."
                    + " Using default preferences.");
            initializedPrefs = new UserPrefs();
        }

        //Update prefs file in case it was missing to begin with or there are new/unused fields
        try {
            storage.saveUserPrefs(initializedPrefs);
        } catch (IOException e) {
            logger.warning("Failed to save preference file : " + StringUtil.getDetails(e));
        }

        return initializedPrefs;
    }

    @Override
    public void start(Stage primaryStage) {
        logger.info("Starting TrackFlow " + MainApp.VERSION);
        ui.start(primaryStage);
    }

    @Override
    public void stop() {
        logger.info("============================ [ Stopping TrackFlow ] =================================");
        try {
            storage.saveUserPrefs(model.getUserPrefs());
        } catch (IOException e) {
            logger.severe("Failed to save preferences " + StringUtil.getDetails(e));
        }
    }
}
