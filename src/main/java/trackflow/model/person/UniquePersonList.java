// @@author Austin-Chia
package trackflow.model.person;
// @@author

import static java.util.Objects.requireNonNull;
// @@author Austin-Chia
import static trackflow.commons.util.CollectionUtil.requireAllNonNull;
// @@author

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
// @@author Austin-Chia
import trackflow.model.person.exceptions.DuplicatePersonException;
import trackflow.model.person.exceptions.PersonNotFoundException;
// @@author

/**
 * A list of persons that enforces uniqueness between its elements and does not allow nulls.
 * New records use {@code Person#isSamePerson(Person)} for duplicate prevention. Bulk restoration uses the older
 * stored-record rule so existing Singapore-format pairs remain readable; updates cannot introduce new pairs.
 * The removal of a person uses Person#equals(Object) so
 * as to ensure that the person with exactly the same fields will be removed.
 *
 * Supports a minimal set of list operations.
 *
 * @see Person#isSamePerson(Person)
 */
public class UniquePersonList implements Iterable<Person> {

    private final ObservableList<Person> internalList = FXCollections.observableArrayList();
    private final ObservableList<Person> internalUnmodifiableList =
            FXCollections.unmodifiableObservableList(internalList);

    /**
     * Returns true if the list contains an equivalent person as the given argument.
     */
    public boolean contains(Person toCheck) {
        requireNonNull(toCheck);
        return internalList.stream().anyMatch(toCheck::isSamePerson);
    }

    /**
     * Adds a person to the list.
     * The person must not already exist in the list.
     */
    public void add(Person toAdd) {
        requireNonNull(toAdd);
        if (contains(toAdd)) {
            throw new DuplicatePersonException();
        }
        internalList.add(toAdd);
    }

    /**
     * Replaces the person {@code target} in the list with {@code editedPerson}.
     * {@code target} must exist in the list.
     * The replacement must not introduce a duplicate relationship with another existing person.
     */
    public void setPerson(Person target, Person editedPerson) {
        requireAllNonNull(target, editedPerson);

        int index = internalList.indexOf(target);
        if (index == -1) {
            throw new PersonNotFoundException();
        }

        if (internalList.stream().anyMatch(person -> !person.equals(target)
                && editedPerson.introducesDuplicate(target, person))) {
            throw new DuplicatePersonException();
        }

        internalList.set(index, editedPerson);
    }

    /**
     * Removes the equivalent person from the list.
     * The person must exist in the list.
     */
    public void remove(Person toRemove) {
        requireNonNull(toRemove);
        if (!internalList.remove(toRemove)) {
            throw new PersonNotFoundException();
        }
    }

    public void setPersons(UniquePersonList replacement) {
        requireNonNull(replacement);
        internalList.setAll(replacement.internalList);
    }

    /**
     * Replaces the contents of this list with {@code persons}.
     * {@code persons} must not contain duplicates under the stored-record rule. Singapore-format pairs from
     * older rosters are preserved; add and edit still prevent introducing new duplicates.
     */
    public void setPersons(List<Person> persons) {
        requireAllNonNull(persons);
        if (!personsAreUnique(persons)) {
            throw new DuplicatePersonException();
        }

        internalList.setAll(persons);
    }

    /**
     * Returns the backing list as an unmodifiable {@code ObservableList}.
     */
    public ObservableList<Person> asUnmodifiableObservableList() {
        return internalUnmodifiableList;
    }

    @Override
    public Iterator<Person> iterator() {
        return internalList.iterator();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof UniquePersonList otherUniquePersonList)) {
            return false;
        }

        return internalList.equals(otherUniquePersonList.internalList);
    }

    @Override
    public int hashCode() {
        return internalList.hashCode();
    }

    @Override
    public String toString() {
        return internalList.toString();
    }

    /**
     * Returns true if {@code persons} contains only unique stored persons, using {@code Person#isSameStoredPerson}.
     * Two persons can only be the same if their names match (ignoring case and repeated spaces), so persons are
     * grouped by that normalized name and only compared within a group. This gives linear behavior for the
     * common case of distinct names while keeping exactly the same notion of duplicates as a full pairwise check.
     */
    private boolean personsAreUnique(List<Person> persons) {
        Map<String, List<Person>> personsByName = new HashMap<>();
        for (Person person : persons) {
            String nameKey = person.getName().fullName.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
            List<Person> sameNamePersons = personsByName.computeIfAbsent(nameKey, key -> new ArrayList<>());
            for (Person other : sameNamePersons) {
                if (person.isSameStoredPerson(other)) {
                    return false;
                }
            }
            sameNamePersons.add(person);
        }
        return true;
    }
}
