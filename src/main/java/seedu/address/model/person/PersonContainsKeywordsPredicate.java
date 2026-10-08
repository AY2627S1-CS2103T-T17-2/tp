package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Tests whether any keyword is a case-insensitive substring of any searchable field of a {@code Person}.
 */
public class PersonContainsKeywordsPredicate implements Predicate<Person> {
    private final List<String> keywords;
    private final Map<SearchField, List<String>> fieldKeywords;

    /**
     * Creates a predicate that matches a person when any keyword occurs in any searchable field.
     */
    public PersonContainsKeywordsPredicate(List<String> keywords) {
        this(keywords, Map.of());
    }

    /**
     * Creates a predicate with broad keywords and keywords restricted to specific fields.
     */
    public PersonContainsKeywordsPredicate(List<String> keywords,
            Map<SearchField, List<String>> fieldKeywords) {
        this.keywords = List.copyOf(requireNonNull(keywords));
        requireNonNull(fieldKeywords);
        EnumMap<SearchField, List<String>> copiedFieldKeywords = new EnumMap<>(SearchField.class);
        fieldKeywords.forEach((field, values) ->
                copiedFieldKeywords.put(requireNonNull(field), List.copyOf(requireNonNull(values))));
        this.fieldKeywords = Collections.unmodifiableMap(copiedFieldKeywords);
    }

    @Override
    public boolean test(Person person) {
        requireNonNull(person);
        boolean broadMatch = containsAnyKeyword(getSearchableText(person), keywords);
        boolean fieldMatch = fieldKeywords.entrySet().stream()
                .anyMatch(entry -> containsAnyKeyword(getFieldText(person, entry.getKey()), entry.getValue()));
        return broadMatch || fieldMatch;
    }

    private static boolean containsAnyKeyword(String text, List<String> keywords) {
        String normalizedText = text.toLowerCase(Locale.ROOT);
        return keywords.stream()
                .map(String::trim)
                .filter(keyword -> !keyword.isEmpty())
                .map(keyword -> keyword.toLowerCase(Locale.ROOT))
                .anyMatch(normalizedText::contains);
    }

    private static String getSearchableText(Person person) {
        return List.of(
                person.getName().fullName,
                person.getAgeCategory().value,
                person.getPhone().value,
                person.getEmail().value,
                person.getAddress().value,
                person.getRemark().value)
                .stream()
                .collect(Collectors.joining("\n"));
    }

    private static String getFieldText(Person person, SearchField field) {
        return switch (field) {
            case NAME -> person.getName().fullName;
            case AGE_CATEGORY -> person.getAgeCategory().value;
            case PHONE -> person.getPhone().value;
            case EMAIL -> person.getEmail().value;
            case ADDRESS -> person.getAddress().value;
            case REMARK -> person.getRemark().value;
        };
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        return other instanceof PersonContainsKeywordsPredicate otherPredicate
                && keywords.equals(otherPredicate.keywords)
                && fieldKeywords.equals(otherPredicate.fieldKeywords);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("keywords", keywords)
                .add("fieldKeywords", fieldKeywords)
                .toString();
    }

    /** Fields that a find keyword can be restricted to. */
    public enum SearchField {
        NAME,
        AGE_CATEGORY,
        PHONE,
        EMAIL,
        ADDRESS,
        REMARK
    }
}
