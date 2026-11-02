package trackflow.model;

import javafx.collections.ObservableList;
import trackflow.model.person.Person;

/**
 * Unmodifiable view of an athlete roster.
 */
public interface ReadOnlyRoster {

    /**
     * Returns an unmodifiable view of the persons list.
     * Stored-record duplicates are excluded; legacy Singapore-format pairs may be preserved.
     */
    ObservableList<Person> getPersonList();

}
