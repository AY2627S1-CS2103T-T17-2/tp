package trackflow.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static trackflow.testutil.Assert.assertThrows;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import trackflow.model.person.PersonContainsKeywordsPredicate.SearchField;
import trackflow.testutil.PersonBuilder;

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
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new PersonContainsKeywordsPredicate(null));
        assertThrows(NullPointerException.class, () -> new PersonContainsKeywordsPredicate(List.of(), null));
    }

    @Test
    public void equals() {
        List<String> firstKeywordList = List.of("first");
        List<String> secondKeywordList = List.of("first", "second");

        PersonContainsKeywordsPredicate firstPredicate = new PersonContainsKeywordsPredicate(firstKeywordList);
        PersonContainsKeywordsPredicate secondPredicate = new PersonContainsKeywordsPredicate(secondKeywordList);

        assertTrue(firstPredicate.equals(firstPredicate));
        assertTrue(firstPredicate.equals(new PersonContainsKeywordsPredicate(firstKeywordList)));
        assertFalse(firstPredicate.equals(secondPredicate));
        assertFalse(firstPredicate.equals(new PersonContainsKeywordsPredicate(firstKeywordList,
                Map.of(SearchField.NAME, List.of("first")))));
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
                "rela"); // event

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
                SearchField.REMARK, "personal",
                SearchField.EVENT, "sprint");

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
    }

    @Test
    public void test_eventKeywordAcrossTwoEvents_returnsFalse() {
        // events are matched one at a time, so a keyword spanning two events must not match
        assertFalse(new PersonContainsKeywordsPredicate(List.of(),
                Map.of(SearchField.EVENT, List.of("sprintsrelay"))).test(ATHLETE));
        assertFalse(new PersonContainsKeywordsPredicate(List.of(),
                Map.of(SearchField.EVENT, List.of("personal"))).test(ATHLETE));
    }

    @Test
    public void test_combinedBroadAndScopedKeywords_matchesIfEitherMatches() {
        // broad matches, scoped does not -> returns true
        PersonContainsKeywordsPredicate broadMatchPredicate = new PersonContainsKeywordsPredicate(
                List.of("avery"), Map.of(SearchField.PHONE, List.of("0000")));
        assertTrue(broadMatchPredicate.test(ATHLETE));

        // broad does not match, scoped matches -> returns true
        PersonContainsKeywordsPredicate scopedMatchPredicate = new PersonContainsKeywordsPredicate(
                List.of("nonexistent"), Map.of(SearchField.PHONE, List.of("2345")));
        assertTrue(scopedMatchPredicate.test(ATHLETE));

        // neither matches -> returns false
        PersonContainsKeywordsPredicate noMatchPredicate = new PersonContainsKeywordsPredicate(
                List.of("nonexistent"), Map.of(SearchField.PHONE, List.of("0000")));
        assertFalse(noMatchPredicate.test(ATHLETE));
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
