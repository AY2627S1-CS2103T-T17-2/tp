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
     */
    void setRoster(ReadOnlyRoster roster);

    /** Returns the AthleteRoster */
    ReadOnlyRoster getRoster();

    /**
     * Returns true if a person with the same identity as {@code person} exists in the athlete roster.
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
     */
    void addPerson(Person person);

    /**
     * Replaces the given person {@code target} with {@code editedPerson}.
     * {@code target} must exist in the athlete roster.
     * The person identity of {@code editedPerson} must not be the same as another existing person in the
     * athlete roster.
     */
    void setPerson(Person target, Person editedPerson);

    /** Returns an unmodifiable view of the filtered person list */
    ObservableList<Person> getFilteredPersonList();

    /**
     * Updates the filter of the filtered person list to filter by the given {@code predicate}.
     * @throws NullPointerException if {@code predicate} is null.
     */
    void updateFilteredPersonList(Predicate<Person> predicate);

    /**
     * Updates the ordering of the displayed person list.
     * @throws NullPointerException if {@code comparator} is null.
     */
    void updateSortedPersonList(Comparator<Person> comparator);
}
