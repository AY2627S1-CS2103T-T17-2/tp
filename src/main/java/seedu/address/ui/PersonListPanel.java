package seedu.address.ui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final Map<Integer, List<WeakReference<Person>>> knownPersons = new HashMap<>();

    /** Athletes that first appeared in the list since the last command finished. */
    private final List<Person> newPersons = new ArrayList<>();

    /** Athletes that were removed from the list since the last command finished (some may have returned). */
    private final List<Person> removedPersons = new ArrayList<>();

    /** The athlete added by the latest command, if it added exactly one. Each cell highlights itself to match. */
    private final ObjectProperty<Person> recentlyAddedPerson = new SimpleObjectProperty<>();

    /**
     * Creates a {@code PersonListPanel} with the given {@code ObservableList}.
     */
    public PersonListPanel(ObservableList<Person> personList) {
        super(FXML);
        personListView.setItems(personList);
        personListView.setCellFactory(listView -> new PersonListViewCell());
        for (Person person : personList) {
            addToKnownPersonsIfNew(person);
        }
        personList.addListener(this::recordListChange);
    }

    /**
     * Records athletes that appear in the list for the first time, and athletes that are removed from it.
     */
    private void recordListChange(ListChangeListener.Change<? extends Person> change) {
        // Clean up dead references to prevent memory leaks
        knownPersons.values().removeIf(bucket -> {
            bucket.removeIf(ref -> ref.get() == null);
            return bucket.isEmpty();
        });

        while (change.next()) {
            if (change.wasPermutated() || change.wasUpdated()) {
                continue;
            }
            removedPersons.addAll(change.getRemoved());
            if (change.wasAdded()) {
                for (Person person : change.getAddedSubList()) {
                    if (addToKnownPersonsIfNew(person)) {
                        newPersons.add(person);
                    }
                }
            }
        }
    }

    private boolean addToKnownPersonsIfNew(Person person) {
        int hash = System.identityHashCode(person);
        List<WeakReference<Person>> bucket = knownPersons.computeIfAbsent(hash, k -> new ArrayList<>());

        for (WeakReference<Person> ref : bucket) {
            if (ref.get() == person) {
                return false;
            }
        }
        bucket.add(new WeakReference<>(person));
        return true;
    }

    /**
     * Highlights the athlete added by the command that just finished and scrolls to it, if the command added
     * exactly one athlete. Otherwise, clears any existing highlight.
     * An athlete that appears while another athlete disappears for good (e.g. after an edit, which replaces the
     * edited athlete) is an update rather than an addition, so it is not highlighted.
     */
    public void highlightNewlyAddedPerson() {
        boolean hasReplacedPerson = removedPersons.stream().anyMatch(person -> !isShown(person));
        Person addedPerson = newPersons.size() == 1 && !hasReplacedPerson ? newPersons.get(0) : null;
        newPersons.clear();
        removedPersons.clear();

        recentlyAddedPerson.set(addedPerson);
        if (addedPerson != null) {
            // Scrolls once the list has laid out its rows, so that their actual heights are known.
            Platform.runLater(() -> personListView.scrollTo(addedPerson));
        }
    }

    private boolean isShown(Person person) {
        return personListView.getItems().stream().anyMatch(shownPerson -> shownPerson == person);
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
