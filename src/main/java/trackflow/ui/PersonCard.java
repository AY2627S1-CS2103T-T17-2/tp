package trackflow.ui;

import java.util.Comparator;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import trackflow.model.person.Performance;
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

    public final Person person;

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
    private Label performancesCaption;
    @FXML
    private FlowPane performances;
    @FXML
    private FlowPane tags;

    /**
     * Creates a {@code PersonCard} with the given {@code Person} and index to display.
     */
    public PersonCard(Person person, int displayedIndex) {
        super(FXML);
        this.person = person;
        id.setText(String.format("%02d", displayedIndex));
        name.setText(person.getName().fullName);
        ageCategory.setText(person.getAgeCategory().value);
        phone.setText(person.getPhone().value);
        email.setText(person.getEmail().value);
        showIfNotEmpty(address, person.getAddress().value);
        showIfNotEmpty(remark, person.getRemark().value);
        for (Performance performance : person.getPerformances()) {
            performances.getChildren().add(createPerformancePill(performance));
        }
        boolean hasPerformances = !person.getPerformances().isEmpty();
        setShown(performancesCaption, hasPerformances);
        setShown(performances, hasPerformances);
        person.getTags().stream()
                .sorted(Comparator.comparing(tag -> tag.tagName))
                .forEach(tag -> tags.getChildren().add(new Label(tag.tagName)));
        tags.setVisible(!tags.getChildren().isEmpty());
        tags.setManaged(tags.isVisible());
    }

    /**
     * Shows {@code text} in {@code label}, hiding the label entirely when {@code text} is empty.
     */
    private static void showIfNotEmpty(Label label, String text) {
        label.setText(text);
        label.setVisible(!text.isEmpty());
        label.setManaged(label.isVisible());
    }

    /**
     * Returns a pill showing a performance's event and result, with its date (if any) shown as a tooltip.
     */
    private static Label createPerformancePill(Performance performance) {
        Label pill = new Label(performance.getEvent() + "  ·  " + performance.getResult());
        pill.getStyleClass().add("pb-pill");
        if (performance.getDate() != null) {
            pill.setTooltip(new Tooltip("Achieved " + performance.getDate()));
        }
        return pill;
    }

    /**
     * Shows or hides {@code node}, removing it from the layout when hidden.
     */
    private static void setShown(Node node, boolean shown) {
        node.setVisible(shown);
        node.setManaged(shown);
    }
}
