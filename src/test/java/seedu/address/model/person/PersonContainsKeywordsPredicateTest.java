package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class PersonContainsKeywordsPredicateTest {

    private static final Person ATHLETE = new PersonBuilder()
            .withName("Avery Tan")
            .withAgeCategory("Under 16")
            .withPhone("91234567")
            .withEmail("avery.tan@example.com")
            .withAddress("National Stadium")
            .withRemark("100m personal best")
            .withTags("Sprints", "Relay")
            .build();

    @Test
    public void equals() {
        List<String> firstKeywordList = List.of("first");
        List<String> secondKeywordList = List.of("first", "second");

        PersonContainsKeywordsPredicate firstPredicate = new PersonContainsKeywordsPredicate(firstKeywordList);
        PersonContainsKeywordsPredicate secondPredicate = new PersonContainsKeywordsPredicate(secondKeywordList);

        assertTrue(firstPredicate.equals(firstPredicate));
        assertTrue(firstPredicate.equals(new PersonContainsKeywordsPredicate(firstKeywordList)));
        assertFalse(firstPredicate.equals(secondPredicate));
        assertFalse(firstPredicate.equals(1));
        assertFalse(firstPredicate.equals(null));
    }

    @Test
    public void test_partialKeywordMatchesEachSearchableField_returnsTrue() {
        List<String> partialMatches = List.of(
                "ver", // name
                "nder", // age category
                "2345", // phone
                "tan@exam", // email
                "stad", // address
                "personal", // remark
                "print", // tag
                "ela"); // another tag

        for (String keyword : partialMatches) {
            PersonContainsKeywordsPredicate predicate = new PersonContainsKeywordsPredicate(List.of(keyword));
            assertTrue(predicate.test(ATHLETE), "Expected partial match for keyword: " + keyword);
        }
    }

    @Test
    public void test_keywordUsesDifferentCase_returnsTrue() {
        assertTrue(new PersonContainsKeywordsPredicate(List.of("AVERY")).test(ATHLETE));
        assertTrue(new PersonContainsKeywordsPredicate(List.of("sPrInTs")).test(ATHLETE));
    }

    @Test
    public void test_anyKeywordMatches_returnsTrue() {
        PersonContainsKeywordsPredicate predicate =
                new PersonContainsKeywordsPredicate(List.of("missing", "1234"));
        assertTrue(predicate.test(ATHLETE));
    }

    @Test
    public void test_noKeywordMatches_returnsFalse() {
        assertFalse(new PersonContainsKeywordsPredicate(List.of()).test(ATHLETE));
        assertFalse(new PersonContainsKeywordsPredicate(List.of("   ")).test(ATHLETE));
        assertFalse(new PersonContainsKeywordsPredicate(List.of("swimming", "9999")).test(ATHLETE));
    }

    @Test
    public void toStringMethod() {
        List<String> keywords = List.of("keyword1", "keyword2");
        PersonContainsKeywordsPredicate predicate = new PersonContainsKeywordsPredicate(keywords);

        String expected = PersonContainsKeywordsPredicate.class.getCanonicalName() + "{keywords=" + keywords + "}";
        assertEquals(expected, predicate.toString());
    }
}
