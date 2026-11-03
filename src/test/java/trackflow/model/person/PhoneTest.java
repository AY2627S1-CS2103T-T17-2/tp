package trackflow.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static trackflow.testutil.Assert.assertThrows;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.Test;

public class PhoneTest {

    // EP: null phone
    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Phone(null));
    }

    // EP: invalid phone (empty string)
    @Test
    public void constructor_invalidPhone_throwsIllegalArgumentException() {
        String invalidPhone = ""; // Boundary value
        assertThrows(IllegalArgumentException.class, Phone.MESSAGE_CONSTRAINTS, () -> new Phone(invalidPhone));
    }

    @Test
    public void isValidPhone() {
        // EP: null phone number
        assertThrows(NullPointerException.class, () -> Phone.isValidPhone(null));

        // EP: empty or blank strings
        assertFalse(Phone.isValidPhone("")); // Boundary value
        assertFalse(Phone.isValidPhone(" "));

        // EP: less than 3 digits (insufficient significant digits)
        assertFalse(Phone.isValidPhone("91")); // Boundary value

        // EP: non-numeric characters and misplaced letters
        assertFalse(Phone.isValidPhone("phone"));
        assertFalse(Phone.isValidPhone("9011p041"));

        // EP: digits inside note brackets without separate significant digits
        assertFalse(Phone.isValidPhone("(HP)"));
        assertFalse(Phone.isValidPhone("(91234567)"));

        // EP: malformed brackets (dangling, empty, nested/unbalanced)
        assertFalse(Phone.isValidPhone("91234567 ("));
        assertFalse(Phone.isValidPhone("123 ()"));
        assertFalse(Phone.isValidPhone("(a(b)) 123"));

        // EP: invalid hyphen placement and misplaced plus signs
        assertFalse(Phone.isValidPhone("---123---"));
        assertFalse(Phone.isValidPhone("91+23+45"));
        for (String invalid : List.of("(+65) (+65) 9123", "+65 (+65) 9123", "9123 (+65) 4567",
                "9123 (HP) 4567", "9123(5)4567", "(+65)", "(+65) (HP)")) {
            assertFalse(Phone.isValidNewPhone(invalid), invalid);
        }

        // EP: more than one country code, or a country code after the number
        assertFalse(Phone.isValidNewPhone("(+65) (+65) 9123"));
        assertFalse(Phone.isValidNewPhone("(+65) +65 9123"));
        assertFalse(Phone.isValidNewPhone("9123 4567 (+65)"));

        // EP: note splitting the digit groups of the number
        assertFalse(Phone.isValidNewPhone("9123 (HP) 4567"));

        // EP: valid minimum length phone number (exactly 3 digits)
        assertTrue(Phone.isValidPhone("911")); // Boundary value

        // EP: valid typical phone numbers and digit formats (spaces, long digit strings)
        assertTrue(Phone.isValidPhone("93121534"));
        assertTrue(Phone.isValidPhone("124293842033123"));
        assertTrue(Phone.isValidPhone("9312 1534"));

        // EP: valid phone numbers with country codes (+65, (+65))
        assertTrue(Phone.isValidPhone("+65 9123 4567"));
        assertTrue(Phone.isValidPhone("(+65) 9123 4567"));

        // EP: valid phone numbers with notes and formatting (brackets, hyphens)
        assertTrue(Phone.isValidPhone("(65) 9123-4567"));
        assertTrue(Phone.isValidPhone("9123 4567 (HP)"));
        assertTrue(Phone.isValidPhone("200 (5)"));
        assertTrue(Phone.isValidPhone("(+65)91234567"));
        assertTrue(Phone.isValidPhone("91234567(HP)"));
        assertTrue(Phone.isValidPhone("9123-4567"));
        assertTrue(Phone.isValidPhone("(HP) 9123 4567"));
        assertTrue(Phone.isValidPhone("(+65) 9123 4567 (HP) (Dad)"));
    }

    @Test
    public void isValidNewPhone_currentFormatsAcceptedLegacyFormatsRemainReadable() {
        for (String phone : List.of("911", "(65) 9123-4567", "(HP) 9123 4567", "(212) 555 0199",
                "(+65)91234567", "+65 9123 4567", "91234567(HP)", "(+65) 9123 4567 (HP) (Dad)")) {
            assertTrue(Phone.isValidNewPhone(phone), phone);
        }
        // EP: a bracketed code before the number needs a subscriber number after it, and counts as a code
        assertEquals(Phone.MESSAGE_CONSTRAINTS, Phone.getNewPhoneValidationError("(91234567)"));
        assertEquals(Phone.MESSAGE_LENGTH, Phone.getNewPhoneValidationError("(212) 1"));
        assertEquals(Phone.MESSAGE_CONSTRAINTS, Phone.getNewPhoneValidationError("(212) (HP) 1234"));
        for (String phone : List.of("9123 (HP) 4567", "(+65) (+65) 9123", "9123 4567 (+65)",
                "( ) 9123 4567", "9123 4567 ( )")) {
            assertTrue(Phone.isValidPhone(phone), phone);
            assertEquals(phone, new Phone(phone).value);
            assertFalse(Phone.isValidNewPhone(phone), phone);
        }
    }

    // EP: long digit sequences and repeated hyphen groups under timeout (performance & robustness)
    @Test
    public void isValidPhone_longDigitSequences_doesNotOverflowOrStall() {
        String manyGroups = "123 ".repeat(3000).trim();
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            assertTrue(Phone.isValidPhone(manyGroups));
            assertTrue(Phone.isValidPhone("123-".repeat(3000) + "456"));
            assertFalse(Phone.isValidPhone(manyGroups + "x"));
            assertFalse(Phone.isValidPhone("1".repeat(30000) + "x"));
        });
    }

    @Test
    public void getPhoneKeys() {
        // EP: standard phone number (spaces ignored)
        assertEquals("91234567", new Phone("9123 4567").getSignificantDigits());

        // EP: textual note ignored
        assertEquals("91234567", new Phone("9123 4567 (HP)").getSignificantDigits());

        // EP: numeric note ignored with minimum 3 significant digits
        assertEquals("200", new Phone("200 (5)").getSignificantDigits()); // Boundary value

        // EP: country code counted in significant digits
        assertEquals("6591234567", new Phone("(+65) 9123 4567").getSignificantDigits());

        // EP: bracketed digits before the number are a code; the same bracket after the number is a note
        assertEquals("2121234", new Phone("(212) 1234").getSignificantDigits());
        assertEquals("1234", new Phone("1234 (212)").getSignificantDigits());
        assertEquals("6591234567", new Phone("(65) 9123 4567").getSignificantDigits());
        assertEquals("6591234567", new Phone("(65) 9123 4567").getComparisonKey());
        assertEquals("2121234567", new Phone("(212) 123-4567").getComparisonKey());

        // EP: a bracketed note without a plain digit sequence before the number contributes no digits
        assertEquals("91234567", new Phone("(HP) 9123 4567").getSignificantDigits());
        assertEquals("91234567", new Phone("(Mum 2) 9123 4567").getSignificantDigits());
        for (String phone : List.of("91234567", "9123-4567 (HP)", "+65 9123 4567",
                "(+65)91234567", "65 9123 4567")) {
            assertEquals("6591234567", new Phone(phone).getComparisonKey());
        }
        assertEquals("4491234567", new Phone("+44 91234567").getComparisonKey());
        assertEquals("12345678", new Phone("+12345678").getComparisonKey());
        assertEquals("65200", new Phone("(+65) 200").getComparisonKey());
        assertEquals("200", new Phone("200 (5)").getComparisonKey());
    }

    // EP: phone constructor collapses multiple and leading/trailing whitespace
    @Test
    public void constructor_collapsesWhitespace() {
        assertTrue(new Phone("9123  4567").value.equals("9123 4567"));
        assertTrue(new Phone("  9123 4567 (HP) ").value.equals("9123 4567 (HP)"));
    }

    @Test
    public void equals() {
        Phone phone = new Phone("999");

        // EP: symmetric equality (different instance with same phone digits) & string representation
        assertTrue(phone.equals(new Phone("999")));
        assertEquals(phone.hashCode(), new Phone("999").hashCode());
        assertEquals("999", phone.toString());

        // EP: reflexive equality (same object instance)
        assertTrue(phone.equals(phone));

        // EP: null comparison
        assertFalse(phone.equals(null));

        // EP: different object type
        assertFalse(phone.equals(5.0f));

        // EP: different phone digits
        assertFalse(phone.equals(new Phone("995")));

        // EP: same significant digits with different whitespace formatting or notes (treated as equal)
        assertTrue(new Phone("9123 4567 (HP)").equals(new Phone("91234567")));
        assertTrue(new Phone("9123 4567").hashCode() == new Phone("91234567").hashCode());

        // EP: numeric note digits ignored for identity comparison
        assertTrue(new Phone("200 (5)").equals(new Phone("200")));
        assertFalse(new Phone("200 (5)").equals(new Phone("2005")));

        // EP: bracketed country code included in significant digits comparison
        assertTrue(new Phone("(+65) 9123 4567").equals(new Phone("6591234567")));
        assertTrue(new Phone("(65) 9123 4567").equals(new Phone("6591234567")));

        // EP: different bracketed area codes before the same subscriber number are different numbers
        assertFalse(new Phone("(555) 123-4567").equals(new Phone("(212) 123-4567")));
        assertFalse(new Phone("(212) 123-4567").equals(new Phone("123-4567")));

        // EP: different digits once country code is included
        assertFalse(new Phone("+65 9123 4567").equals(new Phone("9123 4567")));
    }
}
