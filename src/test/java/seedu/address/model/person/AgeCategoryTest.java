package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class AgeCategoryTest {
    @Test
    public void constructor_validCategories_normalizesDisplay() {
        for (String category : new String[] {"Under 14", "Under 16", "Under 18", "Under 20", "Open"}) {
            assertTrue(AgeCategory.isValidAgeCategory(category));
            assertEquals(category, new AgeCategory(category.toUpperCase()).value);
        }
        assertEquals("Under 14", new AgeCategory("  under \t  14  ").value);
    }

    @Test
    public void constructor_invalidCategory_throws() {
        assertThrows(NullPointerException.class, () -> new AgeCategory(null));
        for (String category : new String[] {"", " ", "14", "U14", "Under 15"}) {
            assertFalse(AgeCategory.isValidAgeCategory(category));
            assertThrows(IllegalArgumentException.class, AgeCategory.MESSAGE_CONSTRAINTS, () ->
                new AgeCategory(category));
        }
    }

    @Test
    public void equals_normalizedCategories_match() {
        AgeCategory category = new AgeCategory("Under 14");
        assertEquals(category, new AgeCategory("under   14"));
        assertEquals(category.hashCode(), new AgeCategory("UNDER 14").hashCode());
        assertFalse(category.equals(new AgeCategory("Open")));
        assertFalse(category.equals(null));
        assertFalse(category.equals("Under 14"));
    }
}
