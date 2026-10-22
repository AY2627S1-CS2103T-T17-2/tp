package trackflow.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static trackflow.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class PhoneTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Phone(null));
    }

    @Test
    public void constructor_invalidPhone_throwsIllegalArgumentException() {
        String invalidPhone = "";
        assertThrows(IllegalArgumentException.class, Phone.MESSAGE_CONSTRAINTS, () -> new Phone(invalidPhone));
    }

    @Test
    public void isValidPhone() {
        // null phone number
        assertThrows(NullPointerException.class, () -> Phone.isValidPhone(null));

        // invalid phone numbers
        assertFalse(Phone.isValidPhone("")); // empty string
        assertFalse(Phone.isValidPhone(" ")); // spaces only
        assertFalse(Phone.isValidPhone("91")); // less than 3 digits
        assertFalse(Phone.isValidPhone("phone")); // non-numeric
        assertFalse(Phone.isValidPhone("9011p041")); // letters outside brackets
        assertFalse(Phone.isValidPhone("(HP)")); // a note but no digits

        // valid phone numbers
        assertTrue(Phone.isValidPhone("911")); // exactly 3 digits
        assertTrue(Phone.isValidPhone("93121534"));
        assertTrue(Phone.isValidPhone("124293842033123")); // long phone numbers
        assertTrue(Phone.isValidPhone("9312 1534")); // spaces between digits
        assertTrue(Phone.isValidPhone("+65 9123 4567")); // country code and spaces
        assertTrue(Phone.isValidPhone("(65) 9123-4567")); // brackets and hyphen
        assertTrue(Phone.isValidPhone("9123 4567 (HP)")); // trailing note
    }

    @Test
    public void constructor_collapsesWhitespace() {
        assertTrue(new Phone("9123  4567").value.equals("9123 4567"));
        assertTrue(new Phone("  9123 4567 (HP) ").value.equals("9123 4567 (HP)"));
    }

    @Test
    public void equals() {
        Phone phone = new Phone("999");

        // same values -> returns true
        assertTrue(phone.equals(new Phone("999")));
        assertEquals(phone.hashCode(), new Phone("999").hashCode());
        assertEquals("999", phone.toString());

        // same object -> returns true
        assertTrue(phone.equals(phone));

        // null -> returns false
        assertFalse(phone.equals(null));

        // different types -> returns false
        assertFalse(phone.equals(5.0f));

        // different values -> returns false
        assertFalse(phone.equals(new Phone("995")));

        // same digits, different formatting and notes -> returns true
        assertTrue(new Phone("9123 4567 (HP)").equals(new Phone("91234567")));
        assertTrue(new Phone("9123 4567").hashCode() == new Phone("91234567").hashCode());

        // different digits once country code is included -> returns false
        assertFalse(new Phone("+65 9123 4567").equals(new Phone("9123 4567")));
    }
}
