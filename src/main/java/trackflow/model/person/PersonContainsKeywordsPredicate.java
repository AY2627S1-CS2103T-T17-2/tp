package trackflow.model.person;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import trackflow.commons.util.ToStringBuilder;

/**
 * Tests whether any broad or field-specific keyword matches an athlete, ignoring case.
 * Broad keywords search name, age category, phone, email, address, and remark; field keywords search only their
 * selected field. The two kinds of matches are combined with OR, and empty keyword collections match no athletes.
 */
public class PersonContainsKeywordsPredicate implements Predicate<Person> {
    private final List<String> keywords;
    private final Map<SearchField, List<String>> fieldKeywords;

    /**
     * Creates a predicate that matches any broad keyword in any searchable field.
     * Copies the supplied list; null lists or elements are rejected. Blank keywords do not match.
     */
    public PersonContainsKeywordsPredicate(List<String> keywords) {
        this(keywords, Map.of());
    }

    /**
     * Creates a predicate that matches any broad keyword or any keyword in its specified field.
     * Defensively copies both collections and their nested lists. Null collections, keys, or elements are rejected.
     * Keywords are trimmed before case-insensitive substring matching; blank keywords do not match.
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
                person.getRemark().value,
                getEventsText(person))
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
            case EVENT -> getEventsText(person);
        };
    }

    /**
     * Returns the athlete's events on separate lines, so that a keyword cannot match across two events.
     */
    private static String getEventsText(Person person) {
        return person.getTags().stream()
                .map(event -> event.tagName)
                .collect(Collectors.joining("\n"));
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
        REMARK,
        EVENT
    }
}
