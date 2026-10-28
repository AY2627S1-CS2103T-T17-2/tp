package trackflow.ui;

import java.util.List;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
// @@author antonydchakola-hub
import javafx.stage.Popup;
import trackflow.logic.AutocompleteEngine;
// @@author
import trackflow.logic.commands.CommandResult;
import trackflow.logic.commands.exceptions.CommandException;
import trackflow.logic.parser.exceptions.ParseException;

/**
 * The UI component that is responsible for receiving user command inputs.
 */
public class CommandBox extends UiPart<Region> {

    public static final String ERROR_STYLE_CLASS = "error";
    private static final String FXML = "CommandBox.fxml";

    /** Inline style for the suggestions list; the popup has its own scene, so the main stylesheet does not apply. */
    private static final String SUGGESTIONS_STYLE = "-fx-font-family: 'Consolas'; -fx-font-size: 11pt;";
    /** Approximate height of one suggestion row, in pixels. */
    private static final double SUGGESTION_ROW_HEIGHT = 24;
    /** Extra height around the suggestion rows, in pixels. */
    private static final double SUGGESTIONS_PADDING = 10;
    /** Height above which the suggestions list scrolls instead of growing, in pixels. */
    private static final double MAX_SUGGESTIONS_HEIGHT = 150;

    private final CommandExecutor commandExecutor;
    private final AutocompleteEngine autocompleteEngine;
    private final Popup autocompletePopup;
    private final ListView<String> suggestionsListView;

    @FXML
    private TextField commandTextField;

    /**
     * Creates a {@code CommandBox} with the given {@code CommandExecutor}.
     */
    public CommandBox(CommandExecutor commandExecutor) {
        super(FXML);
        this.commandExecutor = commandExecutor;
        this.autocompleteEngine = new AutocompleteEngine();
        this.suggestionsListView = createSuggestionsListView();
        this.autocompletePopup = createAutocompletePopup(suggestionsListView);

        suggestionsListView.setOnMouseClicked(event -> applySelectedSuggestion());

        suggestionsListView.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            // @@author ken2k7
            if (event.getCode() == KeyCode.TAB && event.isShiftDown()) {
                autocompletePopup.hide();
                return;
            }
            // @@author
            handleSuggestionsKeyPressed(event);
        });

        // calls #setStyleToDefault() whenever there is a change to the text of the command box.
        commandTextField.textProperty().addListener((unused1, unused2, unused3) -> {
            setStyleToDefault();
            updateSuggestions(commandTextField.getText());
        });

        commandTextField.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            // @@author ken2k7
            if (event.getCode() == KeyCode.TAB && event.isShiftDown()) {
                autocompletePopup.hide();
                return;
            }
            // @@author
            handleCommandKeyPressed(event);
        });
    }

    private static ListView<String> createSuggestionsListView() {
        ListView<String> listView = new ListView<>();
        listView.setStyle(SUGGESTIONS_STYLE);
        return listView;
    }

    private static Popup createAutocompletePopup(ListView<String> suggestions) {
        Popup popup = new Popup();
        popup.getContent().add(suggestions);
        popup.setAutoHide(true);
        return popup;
    }

    /**
     * Shows the command templates matching {@code query} below the command box, or hides the suggestions
     * if there is nothing useful to suggest.
     */
    private void updateSuggestions(String query) {
        if (query == null || query.trim().isEmpty()) {
            autocompletePopup.hide();
            return;
        }
        List<String> suggestions = autocompleteEngine.getSuggestions(query);
        if (suggestions.isEmpty() || isOnlySuggestionAlreadyTyped(suggestions, query)) {
            autocompletePopup.hide();
            return;
        }
        suggestionsListView.getItems().setAll(suggestions);
        suggestionsListView.getSelectionModel().selectFirst();
        resizeSuggestionsList(suggestions.size());
        showSuggestionsBelowCommandBox();
    }

    private static boolean isOnlySuggestionAlreadyTyped(List<String> suggestions, String query) {
        return suggestions.size() == 1 && suggestions.get(0).trim().equalsIgnoreCase(query.trim());
    }

    private void resizeSuggestionsList(int suggestionCount) {
        suggestionsListView.setPrefWidth(commandTextField.getWidth());
        double height = Math.min(suggestionCount * SUGGESTION_ROW_HEIGHT + SUGGESTIONS_PADDING,
                MAX_SUGGESTIONS_HEIGHT);
        suggestionsListView.setPrefHeight(height);
    }

    /**
     * Opens the suggestions popup directly below the command box, if it is not already open and the
     * command box is currently displayed on screen.
     */
    private void showSuggestionsBelowCommandBox() {
        if (autocompletePopup.isShowing() || commandTextField.getScene() == null
                || commandTextField.getScene().getWindow() == null) {
            return;
        }
        Point2D commandBoxOrigin = commandTextField.localToScreen(0, 0);
        if (commandBoxOrigin == null) {
            return;
        }
        autocompletePopup.show(commandTextField.getScene().getWindow(), commandBoxOrigin.getX(),
                commandBoxOrigin.getY() + commandTextField.getHeight());
    }

    /**
     * Replaces the command text with the command word of the selected suggestion, then closes the suggestions.
     */
    private void applySelectedSuggestion() {
        String selected = suggestionsListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            String commandWord = selected.split("\\s+")[0];
            setCommandText(commandWord + " ");
        }
        autocompletePopup.hide();
        // @@author ken2k7
        commandTextField.requestFocus();
        // @@author
    }

    /**
     * Applies the selected suggestion after the current key event has been handled.
     */
    private void applySelectedSuggestionLater(KeyEvent event) {
        event.consume();
        Platform.runLater(this::applySelectedSuggestion);
    }

    /**
     * Handles keys, other than Shift+Tab, pressed while the suggestions list itself has focus.
     */
    private void handleSuggestionsKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.TAB) {
            applySelectedSuggestionLater(event);
        } else if (event.getCode() == KeyCode.ENTER) {
            event.consume();
            autocompletePopup.hide();
            handleCommandEntered();
        }
    }

    /**
     * Handles keys, other than Shift+Tab, pressed in the command box: suggestion navigation while suggestions
     * are shown, otherwise Tab inserts the next missing prefix.
     */
    private void handleCommandKeyPressed(KeyEvent event) {
        if (autocompletePopup.isShowing()) {
            handleSuggestionNavigationKey(event);
        } else if (event.getCode() == KeyCode.TAB) {
            insertNextPrefix(event);
        }
    }

    private void handleSuggestionNavigationKey(KeyEvent event) {
        KeyCode key = event.getCode();
        if (key == KeyCode.UP) {
            moveSuggestionSelection(event, -1);
        } else if (key == KeyCode.DOWN) {
            moveSuggestionSelection(event, 1);
        } else if (key == KeyCode.TAB) {
            applySelectedSuggestionLater(event);
        } else if (key == KeyCode.ENTER || key == KeyCode.ESCAPE) {
            autocompletePopup.hide();
        }
    }

    /**
     * Moves the selected suggestion by {@code offset} rows, staying within the list.
     */
    private void moveSuggestionSelection(KeyEvent event, int offset) {
        event.consume();
        int targetIndex = suggestionsListView.getSelectionModel().getSelectedIndex() + offset;
        if (targetIndex >= 0 && targetIndex < suggestionsListView.getItems().size()) {
            suggestionsListView.getSelectionModel().select(targetIndex);
            suggestionsListView.scrollTo(targetIndex);
        }
    }

    /**
     * Appends the next missing prefix of the typed command, if any, followed by a space.
     */
    private void insertNextPrefix(KeyEvent event) {
        String currentText = commandTextField.getText();
        String nextPrefix = autocompleteEngine.getNextPrefix(currentText);
        if (nextPrefix.isEmpty()) {
            return;
        }
        // @@author ken2k7
        event.consume();
        // @@author
        String newText = currentText + nextPrefix;
        if (!newText.endsWith(" ")) {
            newText += " ";
        }
        setCommandText(newText);
    }

    private void setCommandText(String text) {
        commandTextField.setText(text);
        commandTextField.positionCaret(text.length());
    }

    /**
     * Handles the Enter button pressed event.
     */
    @FXML
    private void handleCommandEntered() {
        String commandText = commandTextField.getText();
        try {
            commandExecutor.execute(commandText);
            commandTextField.setText("");
        } catch (CommandException | ParseException e) {
            setStyleToIndicateCommandFailure();
        }
    }

    /**
     * Sets the command box style to use the default style.
     */
    private void setStyleToDefault() {
        commandTextField.getStyleClass().remove(ERROR_STYLE_CLASS);
    }

    /**
     * Sets the command box style to indicate a failed command.
     */
    private void setStyleToIndicateCommandFailure() {
        ObservableList<String> styleClass = commandTextField.getStyleClass();

        if (styleClass.contains(ERROR_STYLE_CLASS)) {
            return;
        }

        styleClass.add(ERROR_STYLE_CLASS);
    }

    /**
     * Represents a function that can execute commands.
     */
    @FunctionalInterface
    public interface CommandExecutor {
        /**
         * Executes the command and returns the result.
         *
         * @see trackflow.logic.Logic#execute(String)
         */
        CommandResult execute(String commandText) throws CommandException, ParseException;
    }

}
