package trackflow.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static trackflow.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class NameTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Name(null));
    }

    @Test
    public void constructor_invalidName_throwsIllegalArgumentException() {
        String invalidName = "";
        assertThrows(IllegalArgumentException.class, Name.MESSAGE_CONSTRAINTS, () -> new Name(invalidName));
    }

    @Test
    public void isValidName() {
        // null name
        assertThrows(NullPointerException.class, () -> Name.isValidName(null));

        // invalid name
        assertFalse(Name.isValidName("")); // empty string
        assertFalse(Name.isValidName(" ")); // spaces only
        assertFalse(Name.isValidName("^")); // only non-alphanumeric characters
        assertFalse(Name.isValidName("peter*")); // contains non-alphanumeric characters
        assertFalse(Name.isValidName("-Jane")); // starts with a hyphen
        assertFalse(Name.isValidName("'Brien")); // starts with an apostrophe
        assertFalse(Name.isValidName("Jane-")); // ends with a hyphen
        assertFalse(Name.isValidName("O'")); // ends with an apostrophe
        assertFalse(Name.isValidName("Mary--Jane")); // repeated hyphen
        assertFalse(Name.isValidName("A--''")); // only separators after the first letter
        assertFalse(Name.isValidName("Mary - Jane")); // spaces around a hyphen
        assertFalse(Name.isValidName("Jane- ")); // ends with a hyphen before a trailing space

        // valid name
        assertTrue(Name.isValidName("peter jack")); // alphabets only
        assertTrue(Name.isValidName("12345")); // numbers only
        assertTrue(Name.isValidName("peter the 2nd")); // alphanumeric characters
        assertTrue(Name.isValidName("Capital Tan")); // with capital letters
        assertTrue(Name.isValidName("David Roger Jackson Ray Jr 2nd")); // long names
        assertTrue(Name.isValidName("Mary-Jane")); // with a hyphen
        assertTrue(Name.isValidName("O'Brien")); // with an apostrophe
        assertTrue(Name.isValidName("Jean-Luc O'Neil")); // hyphen and apostrophe in different words
        assertTrue(Name.isValidName("Lee  Ann")); // repeated spaces between words
        assertTrue(Name.isValidName("A")); // single letter
    }

    @Test
    public void equals() {
        Name name = new Name("Valid Name");

        // same values -> returns true
        assertTrue(name.equals(new Name("Valid Name")));
        assertEquals(name.hashCode(), new Name("Valid Name").hashCode());
        assertEquals("Valid Name", name.toString());

        // same object -> returns true
        assertTrue(name.equals(name));

        // null -> returns false
        assertFalse(name.equals(null));

        // different types -> returns false
        assertFalse(name.equals(5.0f));

        // different values -> returns false
        assertFalse(name.equals(new Name("Other Valid Name")));
    }
}
