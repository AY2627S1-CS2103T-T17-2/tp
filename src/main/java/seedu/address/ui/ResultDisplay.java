package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;

/**
 * A UI component that displays the result of a command execution.
 */
public class ResultDisplay extends UiPart<Region> {

    private static final String FXML = "ResultDisplay.fxml";

    @FXML
    private TextArea resultDisplay;

    @FXML
    private StackPane resultIcon;

    @FXML
    private SVGPath resultIconGlyph;

    public ResultDisplay() {
        super(FXML);
    }

    /**
     * Displays {@code feedbackToUser} without marking it as a success or an error.
     */
    public void setFeedbackToUser(String feedbackToUser) {
        showFeedback(feedbackToUser, FeedbackType.NEUTRAL);
    }

    /**
     * Displays {@code feedbackToUser} as the result of a command that succeeded.
     */
    public void showSuccess(String feedbackToUser) {
        showFeedback(feedbackToUser, FeedbackType.SUCCESS);
    }

    /**
     * Displays {@code feedbackToUser} as the result of a command that failed.
     */
    public void showError(String feedbackToUser) {
        showFeedback(feedbackToUser, FeedbackType.ERROR);
    }

    private void showFeedback(String feedbackToUser, FeedbackType type) {
        requireNonNull(feedbackToUser);
        for (FeedbackType otherType : FeedbackType.values()) {
            getRoot().getStyleClass().remove(otherType.styleClass);
        }

        boolean hasIcon = type != FeedbackType.NEUTRAL;
        if (hasIcon) {
            getRoot().getStyleClass().add(type.styleClass);
            resultIconGlyph.setContent(type.iconShape);
        }
        resultIcon.setVisible(hasIcon);
        resultIcon.setManaged(hasIcon);
        resultDisplay.setText(feedbackToUser);
    }

    /**
     * Kinds of feedback, each with the style class and icon shape used to present it.
     * The message text itself always states the outcome, so colour is never the only cue.
     */
    private enum FeedbackType {
        NEUTRAL("result-neutral", ""),
        SUCCESS("result-success", "M5.5 10.5 L8.5 13.5 L14.5 6.5"),
        ERROR("result-error", "M6.5 6.5 L13.5 13.5 M13.5 6.5 L6.5 13.5");

        private final String styleClass;
        private final String iconShape;

        FeedbackType(String styleClass, String iconShape) {
            this.styleClass = styleClass;
            this.iconShape = iconShape;
        }
    }

}
