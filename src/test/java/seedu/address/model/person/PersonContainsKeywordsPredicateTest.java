package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.PersonContainsKeywordsPredicate.SearchField;
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
                "personal"); // remark

        for (String keyword : partialMatches) {
            PersonContainsKeywordsPredicate predicate = new PersonContainsKeywordsPredicate(List.of(keyword));
            assertTrue(predicate.test(ATHLETE), "Expected partial match for keyword: " + keyword);
        }
    }

    @Test
    public void test_keywordUsesDifferentCase_returnsTrue() {
        assertTrue(new PersonContainsKeywordsPredicate(List.of("AVERY")).test(ATHLETE));
        assertTrue(new PersonContainsKeywordsPredicate(List.of("PeRsOnAl")).test(ATHLETE));
    }

    @Test
    public void test_anyKeywordMatches_returnsTrue() {
        PersonContainsKeywordsPredicate predicate =
                new PersonContainsKeywordsPredicate(List.of("missing", "1234"));
        assertTrue(predicate.test(ATHLETE));
    }

    @Test
    public void test_nameScopedKeyword_doesNotMatchAgeCategory() {
        PersonContainsKeywordsPredicate predicate = new PersonContainsKeywordsPredicate(List.of(),
                Map.of(SearchField.NAME, List.of("under")));
        Person namedUnderwood = new PersonBuilder(ATHLETE)
                .withName("Underwood Tan")
                .withAgeCategory("Open")
                .build();

        assertFalse(predicate.test(ATHLETE));
        assertTrue(predicate.test(namedUnderwood));
    }

    @Test
    public void test_scopedKeywordsMatchOnlyTheirFields() {
        Map<SearchField, String> matchingValues = Map.of(
                SearchField.NAME, "avery",
                SearchField.AGE_CATEGORY, "under",
                SearchField.PHONE, "2345",
                SearchField.EMAIL, "tan@exam",
                SearchField.ADDRESS, "stad",
                SearchField.REMARK, "personal");

        matchingValues.forEach((field, keyword) -> assertTrue(
                new PersonContainsKeywordsPredicate(List.of(), Map.of(field, List.of(keyword))).test(ATHLETE),
                "Expected scoped match for " + field));
    }

    @Test
    public void test_scopedKeywordInWrongField_returnsFalse() {
        assertFalse(new PersonContainsKeywordsPredicate(List.of(),
                Map.of(SearchField.NAME, List.of("9123"))).test(ATHLETE));
        assertFalse(new PersonContainsKeywordsPredicate(List.of(),
                Map.of(SearchField.PHONE, List.of("avery"))).test(ATHLETE));
    }

    @Test
    public void test_noKeywordMatches_returnsFalse() {
        assertFalse(new PersonContainsKeywordsPredicate(List.of()).test(ATHLETE));
        assertFalse(new PersonContainsKeywordsPredicate(List.of("   ")).test(ATHLETE));
        assertFalse(new PersonContainsKeywordsPredicate(List.of("swimming", "9999")).test(ATHLETE));
        assertFalse(new PersonContainsKeywordsPredicate(List.of("sprints", "relay")).test(ATHLETE));
    }

    @Test
    public void toStringMethod() {
        List<String> keywords = List.of("keyword1", "keyword2");
        PersonContainsKeywordsPredicate predicate = new PersonContainsKeywordsPredicate(keywords);

        String expected = PersonContainsKeywordsPredicate.class.getCanonicalName()
                + "{keywords=" + keywords + ", fieldKeywords={}}";
        assertEquals(expected, predicate.toString());
    }
}
