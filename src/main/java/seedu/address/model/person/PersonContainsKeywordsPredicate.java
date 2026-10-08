package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Tests whether any keyword is a case-insensitive substring of any searchable field of a {@code Person}.
 */
public class PersonContainsKeywordsPredicate implements Predicate<Person> {
    private final List<String> keywords;

    /**
     * Creates a predicate that matches a person when any keyword occurs in any searchable field.
     */
    public PersonContainsKeywordsPredicate(List<String> keywords) {
        this.keywords = List.copyOf(requireNonNull(keywords));
    }

    @Override
    public boolean test(Person person) {
        requireNonNull(person);
        String searchableText = getSearchableText(person).toLowerCase(Locale.ROOT);
        return keywords.stream()
                .map(String::trim)
                .filter(keyword -> !keyword.isEmpty())
                .map(keyword -> keyword.toLowerCase(Locale.ROOT))
                .anyMatch(searchableText::contains);
    }

    private static String getSearchableText(Person person) {
        Stream<String> fields = Stream.of(
                person.getName().fullName,
                person.getAgeCategory().value,
                person.getPhone().value,
                person.getEmail().value,
                person.getAddress().value,
                person.getRemark().value);
        Stream<String> tags = person.getTags().stream().map(tag -> tag.tagName);
        return Stream.concat(fields, tags).collect(Collectors.joining("\n"));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        return other instanceof PersonContainsKeywordsPredicate otherPredicate
                && keywords.equals(otherPredicate.keywords);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("keywords", keywords).toString();
    }
}
