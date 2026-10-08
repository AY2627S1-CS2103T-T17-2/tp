package seedu.address.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
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
    private static final PseudoClass NEW_ATHLETE_PSEUDO_CLASS = PseudoClass.getPseudoClass("new-athlete");
    private final Logger logger = LogsCenter.getLogger(PersonListPanel.class);

    @FXML
    private ListView<Person> personListView;

    /** Every athlete object that has been shown in the list, compared by identity. */
    private final Set<Person> knownPersons = Collections.newSetFromMap(new IdentityHashMap<>());

    /** Athletes that first appeared in the list since the last command finished. */
    private final List<Person> newPersons = new ArrayList<>();

    /** The athlete added by the latest command, if it added exactly one. Each cell highlights itself to match. */
    private final ObjectProperty<Person> recentlyAddedPerson = new SimpleObjectProperty<>();

    /**
     * Creates a {@code PersonListPanel} with the given {@code ObservableList}.
     */
    public PersonListPanel(ObservableList<Person> personList) {
        super(FXML);
        personListView.setItems(personList);
        personListView.setCellFactory(listView -> new PersonListViewCell());
        knownPersons.addAll(personList);
        personList.addListener(this::recordNewPersons);
    }

    /**
     * Records athletes that appear in the list for the first time. An athlete that replaces exactly one other
     * athlete (e.g. after an edit) is an update rather than an addition, so it is not recorded as new.
     */
    private void recordNewPersons(ListChangeListener.Change<? extends Person> change) {
        while (change.next()) {
            if (change.wasPermutated() || change.wasUpdated() || !change.wasAdded()) {
                continue;
            }
            boolean isReplacementOfOne = change.wasReplaced()
                    && change.getRemovedSize() == 1 && change.getAddedSize() == 1;
            for (Person person : change.getAddedSubList()) {
                if (knownPersons.add(person) && !isReplacementOfOne) {
                    newPersons.add(person);
                }
            }
        }
    }

    /**
     * Highlights the athlete added by the command that just finished and scrolls to it, if the command added
     * exactly one athlete. Otherwise, clears any existing highlight.
     */
    public void highlightNewlyAddedPerson() {
        Person addedPerson = newPersons.size() == 1 ? newPersons.get(0) : null;
        newPersons.clear();
        recentlyAddedPerson.set(addedPerson);
        if (addedPerson != null) {
            // Scrolls once the list has laid out its rows, so that their actual heights are known.
            Platform.runLater(() -> personListView.scrollTo(addedPerson));
        }
    }

    /**
     * Custom {@code ListCell} that displays the graphics of a {@code Person} using a {@code PersonCard}.
     */
    class PersonListViewCell extends ListCell<Person> {
        PersonListViewCell() {
            recentlyAddedPerson.addListener((unused, oldPerson, newPerson) -> updateNewAthleteHighlight());
        }

        @Override
        protected void updateItem(Person person, boolean empty) {
            super.updateItem(person, empty);

            if (empty || person == null) {
                setGraphic(null);
                setText(null);
            } else {
                setGraphic(new PersonCard(person, getIndex() + 1).getRoot());
            }
            updateNewAthleteHighlight();
        }

        private void updateNewAthleteHighlight() {
            Person person = getItem();
            pseudoClassStateChanged(NEW_ATHLETE_PSEUDO_CLASS,
                    !isEmpty() && person != null && person == recentlyAddedPerson.get());
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
