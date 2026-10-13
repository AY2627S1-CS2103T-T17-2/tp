package trackflow.model;

import javafx.collections.ObservableList;
import trackflow.model.person.Person;

/**
 * Unmodifiable view of an athlete roster
 */
public interface ReadOnlyRoster {

    /**
     * Returns an unmodifiable view of the persons list.
     * This list will not contain any duplicate persons.
     */
    ObservableList<Person> getPersonList();

}
