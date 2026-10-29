package trackflow.model.tag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static trackflow.testutil.Assert.assertThrows;

import java.util.Locale;

import org.junit.jupiter.api.Test;

public class TagTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Tag(null));
    }

    @Test
    public void constructor_invalidTagName_throwsIllegalArgumentException() {
        String invalidTagName = "";
        assertThrows(IllegalArgumentException.class, () -> new Tag(invalidTagName));
    }

    @Test
    public void isValidTagName() {
        // null tag name
        assertThrows(NullPointerException.class, () -> Tag.isValidTagName(null));

        // invalid tag names
        assertFalse(Tag.isValidTagName("")); // empty string
        assertFalse(Tag.isValidTagName(" ")); // spaces only
        assertFalse(Tag.isValidTagName(" 100m")); // leading space
        assertFalse(Tag.isValidTagName("100m ")); // trailing space
        assertFalse(Tag.isValidTagName("Long  Jump")); // repeated space
        assertFalse(Tag.isValidTagName("-100m")); // leading hyphen
        assertFalse(Tag.isValidTagName("100m-")); // trailing hyphen
        assertFalse(Tag.isValidTagName("Long - Jump")); // spaces around a hyphen
        assertFalse(Tag.isValidTagName("Long -Jump")); // space before a hyphen
        assertFalse(Tag.isValidTagName("Long- Jump")); // space after a hyphen
        assertFalse(Tag.isValidTagName("Long--Jump")); // repeated hyphen
        assertFalse(Tag.isValidTagName("4x100m/relay")); // slash
        assertFalse(Tag.isValidTagName("100m*")); // other symbol
        assertFalse(Tag.isValidTagName("a".repeat(Tag.MAX_LENGTH + 1))); // just over the length limit

        // valid tag names
        assertTrue(Tag.isValidTagName("a")); // shortest
        assertTrue(Tag.isValidTagName("a".repeat(Tag.MAX_LENGTH))); // at the length limit
        assertTrue(Tag.isValidTagName("100m")); // letters and digits
        assertTrue(Tag.isValidTagName("4x100m"));
        assertTrue(Tag.isValidTagName("Long Jump")); // single space
        assertTrue(Tag.isValidTagName("110m-Hurdles")); // single hyphen
        assertTrue(Tag.isValidTagName("Saut-Élan")); // non-English letter
        assertTrue(Tag.isValidTagName("Ünder18 Sprint")); // non-English letter with a digit

        // apostrophes and decimal points inside words
        assertTrue(Tag.isValidTagName("Women's 100m"));
        assertTrue(Tag.isValidTagName("Hammer 7.26kg"));
        assertTrue(Tag.isValidTagName("0.84m Hurdles"));
        assertTrue(Tag.isValidTagName("Men's 4x400m-Relay"));
        assertFalse(Tag.isValidTagName("'100m")); // leading apostrophe
        assertFalse(Tag.isValidTagName("100m'")); // trailing apostrophe
        assertFalse(Tag.isValidTagName("Women''s")); // repeated apostrophe
        assertFalse(Tag.isValidTagName("7.kg")); // decimal point not followed by a digit
        assertFalse(Tag.isValidTagName(".5kg")); // decimal point not after a digit
        assertFalse(Tag.isValidTagName("Shot.Put")); // decimal point between letters
        assertFalse(Tag.isValidTagName("7..26kg")); // repeated decimal point
        assertFalse(Tag.isValidTagName("Women 's")); // space before an apostrophe

        // non-English symbols are still rejected
        assertFalse(Tag.isValidTagName("100m€"));
        assertFalse(Tag.isValidTagName("Élan!"));
    }

    @Test
    public void fromStoredName_legacyAlphanumeric_preservesNameWithoutAllowingNewLongTags() {
        for (int length : new int[] {Tag.MAX_LENGTH + 1, 1000}) {
            String legacyName = "A".repeat(length);
            Tag restored = Tag.fromStoredName(legacyName);
            assertEquals(legacyName, restored.tagName);
            assertEquals(restored, Tag.fromStoredName(legacyName.toLowerCase(Locale.ROOT)));
            assertEquals(restored.hashCode(),
                    Tag.fromStoredName(legacyName.toLowerCase(Locale.ROOT)).hashCode());
            assertThrows(IllegalArgumentException.class, () -> new Tag(legacyName));
        }
        assertEquals(new Tag("Long Jump"), Tag.fromStoredName("Long Jump"));
    }

    @Test
    public void fromStoredName_invalidName_rejectsWithoutTrimmingOrRelaxingLegacyFormat() {
        assertThrows(NullPointerException.class, () -> Tag.fromStoredName(null));
        for (String name : new String[] {"", " ", " 100m", "100m ", "100m*", "Long  Jump",
            "A".repeat(31) + " B", "A".repeat(31) + "-B"}) {
            assertThrows(IllegalArgumentException.class, () -> Tag.fromStoredName(name));
        }
    }

    @Test
    public void messageConstraints_describesSeparatorRule() {
        // the message must state the rule that rejects spaced or repeated hyphens
        assertTrue(Tag.MESSAGE_CONSTRAINTS.contains("single space or a single hyphen"));
        assertTrue(Tag.MESSAGE_CONSTRAINTS.contains("not Long - Jump"));
    }

    @Test
    public void equals() {
        Tag tag = new Tag("Long Jump");

        // same values -> returns true
        assertTrue(tag.equals(new Tag("Long Jump")));

        // different case -> returns true
        assertTrue(tag.equals(new Tag("long jump")));
        assertEquals(tag.hashCode(), new Tag("LONG JUMP").hashCode());

        // same object -> returns true
        assertTrue(tag.equals(tag));

        // null -> returns false
        assertFalse(tag.equals(null));

        // different types -> returns false
        assertFalse(tag.equals("Long Jump"));

        // different values -> returns false
        assertNotEquals(tag, new Tag("High Jump"));
    }

    @Test
    public void constructor_decomposedAccent_storedAsSingleLetter() {
        String decomposedName = "Élan"; // E followed by a combining acute accent
        assertTrue(Tag.isValidTagName(decomposedName));
        assertEquals("Élan", new Tag(decomposedName).tagName);
        assertEquals("Élan", Tag.fromStoredName(decomposedName).tagName);
        assertEquals(new Tag("Élan"), new Tag(decomposedName));
    }

    @Test
    public void equals_lettersWithSeveralLowerCaseForms_returnsTrue() {
        // the Greek capital sigma has two lower-case forms, σ and the word-final ς
        Tag capitalSigma = new Tag("Σ");
        for (String name : new String[] {"σ", "ς"}) {
            assertEquals(capitalSigma, new Tag(name));
            assertEquals(capitalSigma.hashCode(), new Tag(name).hashCode());
        }
    }

    @Test
    public void toString_keepsOriginalCase() {
        assertEquals("[Long Jump]", new Tag("Long Jump").toString());
    }

}
