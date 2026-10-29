package trackflow.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static trackflow.testutil.Assert.assertThrows;

import java.time.Duration;

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
        assertFalse(Phone.isValidPhone("(91234567)")); // all digits are inside a note bracket
        assertFalse(Phone.isValidPhone("91234567 (")); // dangling open bracket
        assertFalse(Phone.isValidPhone("123 ()")); // empty bracket
        assertFalse(Phone.isValidPhone("(a(b)) 123")); // nested, unbalanced brackets
        assertFalse(Phone.isValidPhone("---123---")); // leading, trailing and repeated hyphens
        assertFalse(Phone.isValidPhone("91+23+45")); // "+" anywhere other than the start

        // valid phone numbers
        assertTrue(Phone.isValidPhone("911")); // exactly 3 digits
        assertTrue(Phone.isValidPhone("93121534"));
        assertTrue(Phone.isValidPhone("124293842033123")); // long phone numbers
        assertTrue(Phone.isValidPhone("9312 1534")); // spaces between digits
        assertTrue(Phone.isValidPhone("+65 9123 4567")); // country code and spaces
        assertTrue(Phone.isValidPhone("(+65) 9123 4567")); // bracketed country code counts
        assertTrue(Phone.isValidPhone("(65) 9123-4567")); // (65) has no +, so it is a note; 8 digits remain
        assertTrue(Phone.isValidPhone("9123 4567 (HP)")); // trailing note
        assertTrue(Phone.isValidPhone("200 (5)")); // trailing numeric note is ignored; 200 is enough
        assertTrue(Phone.isValidPhone("(+65)91234567")); // country code written against the digits
        assertTrue(Phone.isValidPhone("91234567(HP)")); // note written against the digits
        assertTrue(Phone.isValidPhone("9123-4567")); // hyphen-separated digit groups
    }

    @Test
    public void isValidPhone_pathologicalInput_evaluatesQuickly() {
        Duration timeout = Duration.ofMillis(200);

        // A long run of digits followed by an invalid character forces the match to fail. With greedy
        // quantifiers this backtracks quadratically; the possessive quantifiers keep it linear. These must
        // return quickly instead of freezing the UI thread on a pasted "phone number".
        String longDigits = "1".repeat(100_000) + "!";
        String longSpaced = "1 ".repeat(50_000) + "!";
        String longHyphened = "1-".repeat(50_000) + "!";

        assertTimeoutPreemptively(timeout, () -> assertFalse(Phone.isValidPhone(longDigits)));
        assertTimeoutPreemptively(timeout, () -> assertFalse(Phone.isValidPhone(longSpaced)));
        assertTimeoutPreemptively(timeout, () -> assertFalse(Phone.isValidPhone(longHyphened)));
    }

    @Test
    public void getSignificantDigits() {
        assertEquals("91234567", new Phone("9123 4567").getSignificantDigits()); // formatting ignored
        assertEquals("91234567", new Phone("9123 4567 (HP)").getSignificantDigits()); // textual note ignored
        assertEquals("200", new Phone("200 (5)").getSignificantDigits()); // numeric note ignored
        assertEquals("6591234567", new Phone("(+65) 9123 4567").getSignificantDigits()); // country code counts
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

        // digits inside a note are ignored, so a note does not change identity -> returns true
        assertTrue(new Phone("200 (5)").equals(new Phone("200")));
        assertFalse(new Phone("200 (5)").equals(new Phone("2005")));

        // a bracketed country code is part of the number -> returns true
        assertTrue(new Phone("(+65) 9123 4567").equals(new Phone("6591234567")));

        // different digits once country code is included -> returns false
        assertFalse(new Phone("+65 9123 4567").equals(new Phone("9123 4567")));
    }
}
