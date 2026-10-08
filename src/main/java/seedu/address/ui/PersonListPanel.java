package seedu.address.ui;

import java.util.logging.Logger;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.person.Person;

/**
 * Panel containing the list of persons.
 */
public class PersonListPanel extends UiPart<Region> {
    private static final String FXML = "PersonListPanel.fxml";
    private final Logger logger = LogsCenter.getLogger(PersonListPanel.class);

    @FXML
    private ListView<Person> personListView;

    /**
     * Creates a {@code PersonListPanel} with the given {@code ObservableList}.
     */
    public PersonListPanel(ObservableList<Person> personList) {
        super(FXML);
        personListView.setItems(personList);
        personListView.setCellFactory(listView -> new PersonListViewCell());
    }

    /**
     * Custom {@code ListCell} that displays the graphics of a {@code Person} using a {@code PersonCard}.
     */
    class PersonListViewCell extends ListCell<Person> {
        @Override
        protected void updateItem(Person person, boolean empty) {
            super.updateItem(person, empty);

            if (empty || person == null) {
                setGraphic(null);
                setText(null);
            } else {
                setGraphic(new PersonCard(person, getIndex() + 1).getRoot());
            }
        }

        /**
         * Measures the card at the width the cell is given, so that wrapped text gets exactly the height it needs.
         * By default, a cell measures its graphic at the graphic's preferred width instead, which leaves empty
         * space below cards whose text would wrap at that (narrower) width.
         */
        @Override
        protected double computePrefHeight(double width) {
            Node graphic = getGraphic();
            if (graphic == null || width < 0) {
                return super.computePrefHeight(width);
            }
            double graphicWidth = width - snappedLeftInset() - snappedRightInset();
            return graphic.prefHeight(graphicWidth) + snappedTopInset() + snappedBottomInset();
        }
    }

}
