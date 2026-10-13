package trackflow.model;

import static java.util.Objects.requireNonNull;
import static trackflow.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Comparator;
import java.util.function.Predicate;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import trackflow.commons.core.GuiSettings;
import trackflow.commons.core.LogsCenter;
import trackflow.model.person.Person;

/**
 * Represents the in-memory model of the athlete roster data.
 */
public class ModelManager implements Model {
    private static final Logger logger = LogsCenter.getLogger(ModelManager.class);

    private final AthleteRoster roster;
    private final UserPrefs userPrefs;
    private final FilteredList<Person> filteredPersons;
    private final SortedList<Person> sortedPersons;

    /**
     * Initializes a ModelManager with the given roster and userPrefs.
     */
    public ModelManager(ReadOnlyRoster roster, ReadOnlyUserPrefs userPrefs) {
        requireAllNonNull(roster, userPrefs);

        logger.fine("Initializing with athlete roster: " + roster + " and user prefs " + userPrefs);

        this.roster = new AthleteRoster(roster);
        this.userPrefs = new UserPrefs(userPrefs);
        filteredPersons = new FilteredList<>(this.roster.getPersonList());
        sortedPersons = new SortedList<>(filteredPersons);
    }

    public ModelManager() {
        this(new AthleteRoster(), new UserPrefs());
    }

    //=========== UserPrefs ==================================================================================

    @Override
    public ReadOnlyUserPrefs getUserPrefs() {
        return userPrefs;
    }

    @Override
    public GuiSettings getGuiSettings() {
        return userPrefs.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        requireNonNull(guiSettings);
        userPrefs.setGuiSettings(guiSettings);
    }

    //=========== AthleteRoster ================================================================================

    @Override
    public void setRoster(ReadOnlyRoster roster) {
        this.roster.resetData(roster);
    }

    @Override
    public ReadOnlyRoster getRoster() {
        return roster;
    }

    @Override
    public boolean hasPerson(Person person) {
        requireNonNull(person);
        return roster.hasPerson(person);
    }

    @Override
    public void deletePerson(Person target) {
        roster.removePerson(target);
    }

    @Override
    public void addPerson(Person person) {
        roster.addPerson(person);
        updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
    }

    @Override
    public void setPerson(Person target, Person editedPerson) {
        requireAllNonNull(target, editedPerson);

        roster.setPerson(target, editedPerson);
    }

    //=========== Filtered Person List Accessors =============================================================

    /**
     * Returns an unmodifiable view of the list of {@code Person} backed by the internal list of
     * {@code roster}
     */
    @Override
    public ObservableList<Person> getFilteredPersonList() {
        return sortedPersons;
    }

    @Override
    public void updateFilteredPersonList(Predicate<Person> predicate) {
        requireNonNull(predicate);
        filteredPersons.setPredicate(predicate);
    }

    @Override
    public void updateSortedPersonList(Comparator<Person> comparator) {
        sortedPersons.setComparator(requireNonNull(comparator));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof ModelManager otherModelManager)) {
            return false;
        }

        return roster.equals(otherModelManager.roster)
                && userPrefs.equals(otherModelManager.userPrefs)
                && sortedPersons.equals(otherModelManager.sortedPersons);
    }

}
