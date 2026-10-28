// @@author -
// Unassigned sections are adapted from SE-EDU AddressBook-Level3.
package trackflow.testutil;

import trackflow.model.AthleteRoster;
import trackflow.model.person.Person;

/**
 * A utility class to help with building AthleteRoster objects.
 * Example usage: <br>
 *     {@code AthleteRoster ab = new AthleteRosterBuilder().withPerson("John", "Doe").build();}
 */
public class AthleteRosterBuilder {

    private AthleteRoster roster;

    public AthleteRosterBuilder() {
        roster = new AthleteRoster();
    }

    /**
     * Adds a new {@code Person} to the {@code AthleteRoster} that we are building.
     */
    public AthleteRosterBuilder withPerson(Person person) {
        roster.addPerson(person);
        return this;
    }

    public AthleteRoster build() {
        return roster;
    }
}
// @@author
