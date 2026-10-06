package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class AgeCategoryPredicateTest {
    @Test
    public void test_matchesOnlyRecordedCategory() {
        AgeCategoryPredicate predicate = new AgeCategoryPredicate(new AgeCategory("under 14"));
        assertTrue(predicate.test(new PersonBuilder().withAgeCategory("Under 14").build()));
        for (String category : new String[] {"Under 16", "Under 18", "Under 20", "Open"}) {
            assertFalse(predicate.test(new PersonBuilder().withName("Under 14").withAgeCategory(category).build()));
        }
    }

    @Test
    public void equals_comparesCategory() {
        AgeCategoryPredicate predicate = new AgeCategoryPredicate(new AgeCategory("Open"));
        assertTrue(predicate.equals(predicate));
        assertEquals(predicate, new AgeCategoryPredicate(new AgeCategory("OPEN")));
        assertFalse(predicate.equals(new AgeCategoryPredicate(new AgeCategory("Under 14"))));
        assertFalse(predicate.equals(null));
        assertFalse(predicate.equals("Open"));
        assertThrows(NullPointerException.class, () -> new AgeCategoryPredicate(null));
    }
}
