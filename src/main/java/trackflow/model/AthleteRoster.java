package trackflow.model;

import static java.util.Objects.requireNonNull;

import java.util.List;

import javafx.collections.ObservableList;
import trackflow.commons.util.ToStringBuilder;
import trackflow.model.person.Person;
import trackflow.model.person.UniquePersonList;

/**
 * Wraps all data at the athlete-roster level.
 * Duplicates are not allowed, as determined by {@link Person#isSamePerson(Person)}.
 */
public class AthleteRoster implements ReadOnlyRoster {

    private final UniquePersonList persons = new UniquePersonList();

    /**
     * Creates an empty athlete roster.
     */
    public AthleteRoster() {}

    /**
     * Creates a roster with the athletes from {@code toBeCopied}, preserving their order.
     */
    public AthleteRoster(ReadOnlyRoster toBeCopied) {
        this();
        resetData(toBeCopied);
    }

    //// list overwrite operations

    /**
     * Replaces the contents of the person list with {@code persons}.
     * {@code persons} must not contain duplicate persons.
     */
    public void setPersons(List<Person> persons) {
        this.persons.setPersons(persons);
    }

    /**
     * Resets the existing data of this {@code AthleteRoster} with {@code newData}.
     */
    public void resetData(ReadOnlyRoster newData) {
        requireNonNull(newData);

        setPersons(newData.getPersonList());
    }

    //// person-level operations

    /**
     * Returns true if a person with the same identity as {@code person} exists in the athlete roster.
     */
    public boolean hasPerson(Person person) {
        requireNonNull(person);
        return persons.contains(person);
    }

    /**
     * Adds a person to the athlete roster.
     * The person must not already exist in the athlete roster.
     */
    public void addPerson(Person p) {
        persons.add(p);
    }

    /**
     * Replaces the given person {@code target} in the list with {@code editedPerson}.
     * {@code target} must exist in the athlete roster.
     * The person identity of {@code editedPerson} must not be the same as another existing person in the
     * athlete roster.
     */
    public void setPerson(Person target, Person editedPerson) {
        requireNonNull(editedPerson);

        persons.setPerson(target, editedPerson);
    }

    /**
     * Removes {@code key} from this {@code AthleteRoster}.
     * {@code key} must exist in the athlete roster.
     */
    public void removePerson(Person key) {
        persons.remove(key);
    }

    //// util methods

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("persons", persons)
                .toString();
    }

    @Override
    public ObservableList<Person> getPersonList() {
        return persons.asUnmodifiableObservableList();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof AthleteRoster otherAthleteRoster)) {
            return false;
        }

        return persons.equals(otherAthleteRoster.persons);
    }

    @Override
    public int hashCode() {
        return persons.hashCode();
    }
}
