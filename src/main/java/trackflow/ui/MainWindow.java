package trackflow.ui;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;

import javafx.application.Platform;
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
    private GuiSettings windowedSettings;

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
        primaryStage.xProperty().addListener(unused -> Platform.runLater(this::rememberWindowedBounds));
        primaryStage.yProperty().addListener(unused -> Platform.runLater(this::rememberWindowedBounds));
        primaryStage.widthProperty().addListener(unused -> Platform.runLater(this::rememberWindowedBounds));
        primaryStage.heightProperty().addListener(unused -> Platform.runLater(this::rememberWindowedBounds));

        helpWindow = new HelpWindow();
        helpWindow.getRoot().initOwner(primaryStage);
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
     * Fits saved outer bounds to a connected screen's usable area, including window decorations.
     * Centers on the primary screen when the saved position is no longer reachable.
     */
    private void setWindowDefaultSize(GuiSettings guiSettings) {
        List<Rectangle2D> screens = Screen.getScreens().stream().map(Screen::getVisualBounds).toList();
        Rectangle2D bounds = WindowPlacement.fit(guiSettings, screens, Screen.getPrimary().getVisualBounds());
        primaryStage.setMinWidth(Math.min(WindowPlacement.MIN_WINDOW_WIDTH, bounds.getWidth()));
        primaryStage.setMinHeight(Math.min(WindowPlacement.MIN_WINDOW_HEIGHT, bounds.getHeight()));
        primaryStage.setWidth(bounds.getWidth());
        primaryStage.setHeight(bounds.getHeight());
        primaryStage.setX(bounds.getMinX());
        primaryStage.setY(bounds.getMinY());
        windowedSettings = new GuiSettings(bounds.getWidth(), bounds.getHeight(),
                (int) bounds.getMinX(), (int) bounds.getMinY());
    }

    /**
     * Remembers normal outer bounds after native window-state changes have settled.
     */
    private void rememberWindowedBounds() {
        if (primaryStage.isShowing() && !primaryStage.isIconified()
                && !primaryStage.isMaximized() && !primaryStage.isFullScreen()) {
            windowedSettings = new GuiSettings(primaryStage.getWidth(), primaryStage.getHeight(),
                    (int) primaryStage.getX(), (int) primaryStage.getY());
            logic.setGuiSettings(windowedSettings);
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

    /**
     * Shows the stage and remembers its normal outer bounds.
     */
    void show() {
        primaryStage.show();
        rememberWindowedBounds();
    }

    /**
     * Closes the application.
     */
    @FXML
    private void handleExit() {
        rememberWindowedBounds();
        logic.setGuiSettings(windowedSettings);
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
