package trackflow.model.tag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static trackflow.testutil.Assert.assertThrows;

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
    public void toString_keepsOriginalCase() {
        assertEquals("[Long Jump]", new Tag("Long Jump").toString());
    }

}
