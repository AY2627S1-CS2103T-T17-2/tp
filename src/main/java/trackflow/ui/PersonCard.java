package trackflow.ui;

import java.util.Comparator;
import java.util.Locale;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import javafx.util.Duration;
import trackflow.model.person.Person;

/**
 * A UI component that displays information of a {@code Person}.
 */
public class PersonCard extends UiPart<Region> {

    private static final String FXML = "PersonListCard.fxml";

    /**
     * Note: Certain keywords such as "location" and "resources" are reserved keywords in JavaFX.
     * As a consequence, UI elements' variable names cannot be set to such keywords
     * or an exception will be thrown by JavaFX during runtime.
     */
    private final Person person;

    @FXML
    private GridPane cardPane;
    @FXML
    private Label name;
    @FXML
    private Label id;
    @FXML
    private Text phone;
    @FXML
    private Label ageCategory;
    @FXML
    private Label address;
    @FXML
    private Text email;
    @FXML
    private Label remark;
    @FXML
    private FlowPane tags;

    /**
     * Creates a {@code PersonCard} with the given {@code Person} and index to display.
     */
    public PersonCard(Person person, int displayedIndex) {
        super(FXML);
        this.person = person;
        id.setText(formatDisplayedIndex(displayedIndex));
        name.setText(person.getName().fullName);
        ageCategory.setText(person.getAgeCategory().value);
        phone.setText(person.getPhone().value);
        email.setText(person.getEmail().value);
        showIfNotEmpty(address, person.getAddress().value);
        showIfNotEmpty(remark, person.getRemark().value);
        person.getTags().stream()
                // Sort ignoring case to match the order used in tag/untag command feedback (EventTagCommand).
                .sorted(Comparator.comparing(tag -> tag.tagName.toLowerCase(Locale.ROOT)))
                .forEach(tag -> tags.getChildren().add(createEventChip(tag.tagName)));
        tags.setVisible(!tags.getChildren().isEmpty());
        tags.setManaged(tags.isVisible());
    }

    /**
     * Formats an index using the ASCII digits accepted by the command parser, regardless of the system locale.
     */
    static String formatDisplayedIndex(int displayedIndex) {
        return String.format(Locale.ROOT, "%02d", displayedIndex);
    }

    /**
     * Returns a chip for an event name. A long name is shortened with an ellipsis to fit the card instead of
     * overflowing it, and an instant tooltip reveals the full value on hover. The tooltip is attached only while
     * the name is actually truncated, so a name that already fits (e.g. in a maximized window) shows no tooltip.
     * The stored value is unchanged.
     */
    private Label createEventChip(String eventName) {
        Label chip = new Label(eventName);
        // Clamp to the card width (minus the index and age-category columns, which the FlowPane width would not
        // account for) so a long name is truncated with an ellipsis within the card rather than overflowing it.
        // Floored so a very narrow window still leaves a usable chip width.
        chip.maxWidthProperty().bind(Bindings.max(100, cardPane.widthProperty().subtract(170)));
        Tooltip tooltip = new Tooltip(eventName);
        tooltip.setShowDelay(Duration.ZERO); // appear immediately on hover, not after JavaFX's 1s default
        // Attach the tooltip only while the name is truncated; re-evaluated whenever the chip is resized.
        chip.widthProperty().addListener((observable, oldWidth, newWidth) ->
                chip.setTooltip(isTruncated(chip, eventName) ? tooltip : null));
        return chip;
    }

    /**
     * Returns true if {@code chip} is too narrow to show {@code text} in full, so the ellipsis is visible.
     */
    private static boolean isTruncated(Label chip, String text) {
        Text measuredText = new Text(text);
        measuredText.setFont(chip.getFont());
        double available = chip.getWidth() - chip.getInsets().getLeft() - chip.getInsets().getRight();
        return measuredText.getLayoutBounds().getWidth() > available + 1;
    }

    /**
     * Shows {@code text} in {@code label}, hiding the label entirely when {@code text} is empty.
     */
    private static void showIfNotEmpty(Label label, String text) {
        label.setText(text);
        label.setVisible(!text.isEmpty());
        label.setManaged(label.isVisible());
    }
}
