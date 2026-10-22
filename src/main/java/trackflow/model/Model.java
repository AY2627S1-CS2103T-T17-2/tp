package trackflow.model;

import java.util.Comparator;
import java.util.function.Predicate;

import javafx.collections.ObservableList;
import trackflow.commons.core.GuiSettings;
import trackflow.model.person.Person;

/**
 * The API of the Model component.
 */
public interface Model {
    /** {@code Predicate} that always evaluates to true */
    Predicate<Person> PREDICATE_SHOW_ALL_PERSONS = unused -> true;

    /**
     * Returns the user prefs.
     */
    ReadOnlyUserPrefs getUserPrefs();

    /**
     * Returns the user prefs' GUI settings.
     */
    GuiSettings getGuiSettings();

    /**
     * Sets the user prefs' GUI settings.
     */
    void setGuiSettings(GuiSettings guiSettings);

    /**
     * Replaces athlete roster data with the data in {@code roster}.
     * Retains the current display predicate and comparator.
     */
    void setRoster(ReadOnlyRoster roster);

    /**
     * Returns a read-only view of the complete roster, including athletes hidden by the current filter.
     */
    ReadOnlyRoster getRoster();

    /**
     * Returns true if the complete roster contains an athlete matching {@link Person#isSamePerson(Person)}.
     * Athletes hidden by the current filter are included in the check.
     */
    boolean hasPerson(Person person);

    /**
     * Deletes the given person.
     * The person must exist in the athlete roster.
     */
    void deletePerson(Person target);

    /**
     * Adds the given person.
     * {@code person} must not already exist in the athlete roster.
     * Resets the filter to show all athletes, retaining the current sort order.
     */
    void addPerson(Person person);

    /**
     * Replaces the given person {@code target} with {@code editedPerson}.
     * {@code target} must exist in the athlete roster.
     * The person identity of {@code editedPerson} must not be the same as another existing person in the
     * athlete roster.
     */
    void setPerson(Person target, Person editedPerson);

    /**
     * Returns a live, unmodifiable view of the roster after applying the current filter and sort order.
     */
    ObservableList<Person> getFilteredPersonList();

    /**
     * Returns the predicate controlling which athletes are currently displayed.
     */
    default Predicate<Person> getPersonListPredicate() {
        return PREDICATE_SHOW_ALL_PERSONS;
    }

    /**
     * Returns the comparator controlling the displayed order, or {@code null} for roster order.
     */
    default Comparator<Person> getPersonListComparator() {
        return null;
    }

    /**
     * Replaces the display filter with {@code predicate}, retaining the current sort order.
     * Does not change roster data or its stored order.
     *
     * @throws NullPointerException If {@code predicate} is null.
     */
    void updateFilteredPersonList(Predicate<Person> predicate);

    /**
     * Replaces the display ordering with {@code comparator}, retaining the current filter.
     * Does not reorder the stored roster.
     *
     * @throws NullPointerException If {@code comparator} is null.
     */
    void updateSortedPersonList(Comparator<Person> comparator);
}
