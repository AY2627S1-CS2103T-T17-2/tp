package trackflow.ui;

import java.awt.Point;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;

import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import trackflow.commons.core.GuiSettings;
import trackflow.commons.core.LogsCenter;
import trackflow.logic.Logic;
import trackflow.logic.commands.CommandResult;
import trackflow.logic.commands.exceptions.CommandException;
import trackflow.logic.parser.exceptions.ParseException;

/**
 * The Main Window. Provides the basic application layout containing
 * a menu bar and space where other JavaFX elements can be placed.
 */
public class MainWindow extends UiPart<Stage> {

    private static final String FXML = "MainWindow.fxml";

    private final Logger logger = LogsCenter.getLogger(getClass());

    private Stage primaryStage;
    private Logic logic;
    private Path dataFilePath;

    // Independent Ui parts residing in this Ui container
    private PersonListPanel personListPanel;
    private ResultDisplay resultDisplay;
    private StatusBarFooter statusBarFooter;
    private HelpWindow helpWindow;

    @FXML
    private StackPane commandBoxPlaceholder;

    @FXML
    private StackPane personListPanelPlaceholder;

    @FXML
    private StackPane resultDisplayPlaceholder;

    @FXML
    private StackPane statusbarPlaceholder;

    /**
     * Creates a {@code MainWindow} with the given {@code Stage}, {@code Logic},
     * and the data file path to show in the status bar.
     */
    public MainWindow(Stage primaryStage, Logic logic, Path dataFilePath) {
        super(FXML, primaryStage);

        // Set dependencies
        this.primaryStage = primaryStage;
        this.logic = logic;
        this.dataFilePath = dataFilePath;

        // Configure the UI
        setWindowDefaultSize(logic.getGuiSettings());

        helpWindow = new HelpWindow();
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Fills up all the placeholders of this window.
     */
    void fillInnerParts() {
        personListPanel = new PersonListPanel(logic.getFilteredPersonList());
        personListPanelPlaceholder.getChildren().add(personListPanel.getRoot());

        resultDisplay = new ResultDisplay();
        resultDisplayPlaceholder.getChildren().add(resultDisplay.getRoot());

        statusBarFooter = new StatusBarFooter(dataFilePath, logic.getFilteredPersonList());
        statusbarPlaceholder.getChildren().add(statusBarFooter.getRoot());

        CommandBox commandBox = new CommandBox(this::executeCommand);
        commandBoxPlaceholder.getChildren().add(commandBox.getRoot());
    }

    /**
     * Shows a start-up warning (for example, that the saved data could not be read) in the result display.
     */
    void showStartupWarning(String message) {
        resultDisplay.showError(message);
    }

    /**
     * Sets the default size based on {@code guiSettings}, and the saved position only if it is still visible
     * on a connected screen. Otherwise the position is left unset, so JavaFX centres the window on the
     * primary screen when it is shown, and the size is capped to that screen.
     */
    private void setWindowDefaultSize(GuiSettings guiSettings) {
        double width = guiSettings.getWindowWidth();
        double height = guiSettings.getWindowHeight();
        Point coordinates = guiSettings.getWindowCoordinates();
        List<Rectangle2D> screens = Screen.getScreens().stream().map(Screen::getVisualBounds).toList();

        if (coordinates != null
                && WindowPlacement.isReachable(coordinates.getX(), coordinates.getY(), width, height, screens)) {
            primaryStage.setWidth(width);
            primaryStage.setHeight(height);
            primaryStage.setX(coordinates.getX());
            primaryStage.setY(coordinates.getY());
        } else {
            Rectangle2D primaryScreen = Screen.getPrimary().getVisualBounds();
            primaryStage.setWidth(Math.min(width, primaryScreen.getWidth()));
            primaryStage.setHeight(Math.min(height, primaryScreen.getHeight()));
        }
    }

    /**
     * Opens the help window or focuses on it if it's already opened.
     */
    @FXML
    public void handleHelp() {
        if (!helpWindow.isShowing()) {
            helpWindow.show();
        } else {
            helpWindow.focus();
        }
    }

    void show() {
        primaryStage.show();
    }

    /**
     * Closes the application.
     */
    @FXML
    private void handleExit() {
        if (!primaryStage.isIconified()) {
            // A minimised window can report a meaningless position, so keep the previously saved settings.
            GuiSettings guiSettings = new GuiSettings(primaryStage.getWidth(), primaryStage.getHeight(),
                    (int) primaryStage.getX(), (int) primaryStage.getY());
            logic.setGuiSettings(guiSettings);
        }
        helpWindow.hide();
        primaryStage.hide();
    }

    public PersonListPanel getPersonListPanel() {
        return personListPanel;
    }

    /**
     * Executes the command and returns the result.
     *
     * @see trackflow.logic.Logic#execute(String)
     */
    private CommandResult executeCommand(String commandText) throws CommandException, ParseException {
        try {
            CommandResult commandResult = logic.execute(commandText);
            logger.info("Result: " + commandResult.getFeedbackToUser());
            personListPanel.highlightNewlyAddedPerson();
            resultDisplay.showSuccess(commandResult.getFeedbackToUser());
            if (commandResult.isDataSaved()) {
                statusBarFooter.showSaved();
            } else {
                statusBarFooter.clearNotSaved();
            }

            if (commandResult.isShowHelp()) {
                handleHelp();
            }

            if (commandResult.isExit()) {
                handleExit();
            }

            return commandResult;
        } catch (CommandException | ParseException e) {
            logger.info("An error occurred while executing command: " + commandText);
            personListPanel.highlightNewlyAddedPerson();
            resultDisplay.showError(e.getMessage());
            if (e.getCause() instanceof IOException) {
                statusBarFooter.showNotSaved();
            }
            throw e;
        }
    }
}
