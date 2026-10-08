package seedu.address.ui;

import java.util.List;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.stage.Popup;
import seedu.address.logic.AutocompleteEngine;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * The UI component that is responsible for receiving user command inputs.
 */
public class CommandBox extends UiPart<Region> {

    public static final String ERROR_STYLE_CLASS = "error";
    private static final String FXML = "CommandBox.fxml";

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

        this.suggestionsListView = new ListView<>();
        this.suggestionsListView.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 11pt;");
        this.autocompletePopup = new Popup();
        this.autocompletePopup.getContent().add(suggestionsListView);
        this.autocompletePopup.setAutoHide(true);

        Runnable applySuggestion = () -> {
            String selected = suggestionsListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                String commandWord = selected.split("\\s+")[0];
                String newText = commandWord + " ";
                commandTextField.setText(newText);
                commandTextField.positionCaret(newText.length());
            }
            autocompletePopup.hide();
        };

        suggestionsListView.setOnMouseClicked(event -> applySuggestion.run());

        suggestionsListView.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.TAB) {
                event.consume();
                Platform.runLater(applySuggestion);
            }
        });

        // calls #setStyleToDefault() whenever there is a change to the text of the command box.
        commandTextField.textProperty().addListener((unused1, unused2, unused3) -> {
            setStyleToDefault();
            String query = commandTextField.getText();
            if (query == null || query.trim().isEmpty()) {
                autocompletePopup.hide();
                return;
            }
            List<String> suggestions = autocompleteEngine.getSuggestions(query);
            if (suggestions.isEmpty() || (suggestions.size() == 1 && suggestions.get(0).equals(query))) {
                autocompletePopup.hide();
            } else {
                suggestionsListView.getItems().setAll(suggestions);
                suggestionsListView.getSelectionModel().selectFirst();
                suggestionsListView.setPrefWidth(commandTextField.getWidth());

                // Adjust height based on number of items (approx 24px per item, max 150px)
                double height = Math.min(suggestions.size() * 24 + 10, 150);
                suggestionsListView.setPrefHeight(height);

                if (!autocompletePopup.isShowing()) {
                    autocompletePopup.show(commandTextField.getScene().getWindow(),
                            commandTextField.localToScreen(0, 0).getX(),
                            commandTextField.localToScreen(0, 0).getY() + commandTextField.getHeight());
                }
            }
        });

        commandTextField.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (autocompletePopup.isShowing()) {
                if (event.getCode() == KeyCode.UP) {
                    event.consume();
                    int selected = suggestionsListView.getSelectionModel().getSelectedIndex();
                    if (selected > 0) {
                        suggestionsListView.getSelectionModel().select(selected - 1);
                        suggestionsListView.scrollTo(selected - 1);
                    }
                } else if (event.getCode() == KeyCode.DOWN) {
                    event.consume();
                    int selected = suggestionsListView.getSelectionModel().getSelectedIndex();
                    if (selected < suggestionsListView.getItems().size() - 1) {
                        suggestionsListView.getSelectionModel().select(selected + 1);
                        suggestionsListView.scrollTo(selected + 1);
                    }
                } else if (event.getCode() == KeyCode.TAB || event.getCode() == KeyCode.ENTER) {
                    event.consume();
                    Platform.runLater(applySuggestion);
                } else if (event.getCode() == KeyCode.ESCAPE) {
                    autocompletePopup.hide();
                }
            } else {
                if (event.getCode() == KeyCode.TAB) {
                    event.consume();
                    String currentText = commandTextField.getText();
                    String nextPrefix = autocompleteEngine.getNextPrefix(currentText);
                    if (!nextPrefix.isEmpty()) {
                        String newText = currentText + nextPrefix;
                        if (!newText.endsWith(" ")) {
                            newText += " ";
                        }
                        commandTextField.setText(newText);
                        commandTextField.positionCaret(newText.length());
                    }
                }
            }
        });
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
         * @see seedu.address.logic.Logic#execute(String)
         */
        CommandResult execute(String commandText) throws CommandException, ParseException;
    }

}
