// @@author qiyuannn
package trackflow.model.person;
// @@author

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
// @@author qiyuannn
import static trackflow.logic.commands.CommandTestUtil.VALID_ADDRESS_BOB;
import static trackflow.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static trackflow.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static trackflow.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static trackflow.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static trackflow.testutil.Assert.assertThrows;
import static trackflow.testutil.TypicalPersons.ALICE;
import static trackflow.testutil.TypicalPersons.BOB;
// @@author

import org.junit.jupiter.api.Test;

// @@author qiyuannn
import trackflow.testutil.PersonBuilder;
// @@author

public class PersonTest {

    @Test
    public void asObservableList_modifyList_throwsUnsupportedOperationException() {
        Person person = new PersonBuilder().build();
        assertThrows(UnsupportedOperationException.class, () -> person.getTags().remove(0));
    }

    @Test
    public void isSamePerson() {
        // same object -> returns true
        assertTrue(ALICE.isSamePerson(ALICE));

        // null -> returns false
        assertFalse(ALICE.isSamePerson(null));

        // Matching name and either contact identifies a duplicate, regardless of category.
        assertTrue(ALICE.isSamePerson(new PersonBuilder(ALICE).withPhone(VALID_PHONE_BOB).build()));
        assertTrue(ALICE.isSamePerson(new PersonBuilder(ALICE).withEmail(VALID_EMAIL_BOB).build()));
        assertFalse(ALICE.isSamePerson(new PersonBuilder(ALICE).withName(VALID_NAME_BOB).build()));
        assertTrue(ALICE.isSamePerson(new PersonBuilder(ALICE).withAgeCategory("Under 14").build()));

        assertFalse(ALICE.isSamePerson(new PersonBuilder(ALICE).withPhone(VALID_PHONE_BOB)
                .withEmail(VALID_EMAIL_BOB).build()));

        // Display case, surrounding spaces, and repeated name spaces do not change identity.
        Person normalizedAlice = new PersonBuilder(ALICE).withName("Alice   Pauline ")
                .withEmail(ALICE.getEmail().value.toUpperCase()).build();
        assertTrue(ALICE.isSamePerson(normalizedAlice));

        // Legacy data fields and remarks do not contribute to athlete identity.
        Person editedAlice = new PersonBuilder(ALICE).withAddress(VALID_ADDRESS_BOB)
                .withRemark("Different remark")
                .withTags(VALID_TAG_HUSBAND).build();
        assertTrue(ALICE.isSamePerson(editedAlice));
    }

    @Test
    public void hasSameContactDetails() {
        // null -> returns false
        assertFalse(ALICE.hasSameContactDetails(null));

        // same object -> returns true
        assertTrue(ALICE.hasSameContactDetails(ALICE));

        // different name, same phone -> returns true
        assertTrue(ALICE.hasSameContactDetails(new PersonBuilder(BOB).withPhone(ALICE.getPhone().value).build()));

        // different name, same email (case-insensitive) -> returns true
        assertTrue(ALICE.hasSameContactDetails(
                new PersonBuilder(BOB).withEmail(ALICE.getEmail().value.toUpperCase()).build()));

        // different phone and email -> returns false
        assertFalse(ALICE.hasSameContactDetails(BOB));
    }

    @Test
    public void equals() {
        // same values -> returns true
        Person aliceCopy = new PersonBuilder(ALICE).build();
        assertTrue(ALICE.equals(aliceCopy));
        assertEquals(ALICE.hashCode(), aliceCopy.hashCode());

        // same object -> returns true
        assertTrue(ALICE.equals(ALICE));

        // null -> returns false
        assertFalse(ALICE.equals(null));

        // different type -> returns false
        assertFalse(ALICE.equals(5));

        // different person -> returns false
        assertFalse(ALICE.equals(BOB));

        // different name -> returns false
        Person editedAlice = new PersonBuilder(ALICE).withName(VALID_NAME_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different phone -> returns false
        editedAlice = new PersonBuilder(ALICE).withPhone(VALID_PHONE_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different email -> returns false
        editedAlice = new PersonBuilder(ALICE).withEmail(VALID_EMAIL_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different address -> returns false
        editedAlice = new PersonBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different category -> returns false
        editedAlice = new PersonBuilder(ALICE).withAgeCategory("Under 14").build();
        assertFalse(ALICE.equals(editedAlice));

        // different remark -> returns false
        editedAlice = new PersonBuilder(ALICE).withRemark("Different remark").build();
        assertFalse(ALICE.equals(editedAlice));

        // different tags -> returns false
        editedAlice = new PersonBuilder(ALICE).withTags(VALID_TAG_HUSBAND).build();
        assertFalse(ALICE.equals(editedAlice));
    }

    @Test
    public void toStringMethod() {
        String expected = Person.class.getCanonicalName() + "{name=" + ALICE.getName()
                + ", ageCategory=" + ALICE.getAgeCategory()
                + ", phone=" + ALICE.getPhone()
                + ", email=" + ALICE.getEmail() + ", address=" + ALICE.getAddress()
                + ", remark=" + ALICE.getRemark() + ", tags=" + ALICE.getTags() + "}";
        assertEquals(expected, ALICE.toString());
    }
}
