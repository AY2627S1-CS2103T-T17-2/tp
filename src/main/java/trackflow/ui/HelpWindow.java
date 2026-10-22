package trackflow.ui;

import java.util.logging.Logger;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import trackflow.commons.core.LogsCenter;
import trackflow.logic.commands.HelpCommand;

/**
 * Controller for a help page.
 */
public class HelpWindow extends UiPart<Stage> {

    private static final Logger logger = LogsCenter.getLogger(HelpWindow.class);
    private static final String FXML = "HelpWindow.fxml";
    private static final String SUBTITLE_FORMAT =
            "All %d commands, with syntax and examples. Type a command and press Enter.";

    @FXML
    private Label subtitle;

    @FXML
    private VBox commandList;

    /**
     * Creates a new HelpWindow.
     *
     * @param root Stage to use as the root of the HelpWindow.
     */
    public HelpWindow(Stage root) {
        super(FXML, root);
        subtitle.setText(String.format(SUBTITLE_FORMAT, HelpCommand.COMMAND_USAGES.size()));
        for (String usage : HelpCommand.COMMAND_USAGES) {
            commandList.getChildren().add(createCommandCard(usage));
        }
    }

    /**
     * Creates a new HelpWindow.
     */
    public HelpWindow() {
        this(new Stage());
    }

    /**
     * Separates a command's usage into a heading, description, parameters and examples.
     */
    private VBox createCommandCard(String usage) {
        VBox card = new VBox(8);
        card.getStyleClass().add("command-card");

        int separator = usage.indexOf(":");
        String commandWord = usage.substring(0, separator);
        card.getChildren().add(createLabel(commandWord, "command-heading"));

        String[] lines = usage.substring(separator + 1).strip().split("\n");
        int parameters = lines[0].indexOf("Parameters:");
        String description = parameters < 0 ? lines[0] : lines[0].substring(0, parameters).strip();
        card.getChildren().add(createLabel(description, "command-description"));
        if (parameters >= 0) {
            addUsageField(card, "Parameters", lines[0].substring(parameters + "Parameters:".length()).strip());
        } else if (usage.indexOf("\nParameters:") < 0) {
            addUsageField(card, "Format", commandWord);
        }

        for (int i = 1; i < lines.length; i++) {
            int fieldSeparator = lines[i].indexOf(":");
            if (fieldSeparator >= 0) {
                addUsageField(card, lines[i].substring(0, fieldSeparator),
                        lines[i].substring(fieldSeparator + 1).strip());
            } else {
                card.getChildren().add(createLabel(lines[i], "command-description"));
            }
        }
        return card;
    }

    private void addUsageField(VBox card, String title, String value) {
        VBox field = new VBox(4);
        field.getChildren().addAll(createLabel(title, "field-heading"), createLabel(value, "command-code"));
        card.getChildren().add(field);
    }

    private Label createLabel(String text, String styleClass) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMaxWidth(Double.MAX_VALUE);
        label.getStyleClass().add(styleClass);
        return label;
    }

    /**
     * Shows and centers the help window without waiting for it to close.
     *
     * @throws IllegalStateException If called outside the JavaFX Application Thread.
     */
    public void show() {
        logger.fine("Showing help page about the application.");
        getRoot().show();
        getRoot().centerOnScreen();
    }

    /**
     * Returns true if the help window is currently being shown.
     */
    public boolean isShowing() {
        return getRoot().isShowing();
    }

    /**
     * Hides the help window.
     */
    public void hide() {
        getRoot().hide();
    }

    /**
     * Restores the help window if it is minimized, then brings it to the front and focuses on it.
     */
    public void focus() {
        Stage stage = getRoot();
        if (stage.isIconified()) {
            stage.setIconified(false);
        }
        stage.toFront();
        stage.requestFocus();
    }

}
