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
 * Tests a {@code Person} against find keywords using case-insensitive substring matching. An unprefixed keyword
 * matches when it occurs in any searchable field (name, age category, phone, email, address, remark, or event).
 * A prefixed keyword is matched only against its field, and different field prefixes are combined with AND, so a
 * person must satisfy every field criterion. When both unprefixed keywords and field criteria are given, a person
 * matches if any unprefixed keyword matches or every field criterion matches. Empty keyword collections match no
 * athletes.
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
     * Creates a predicate that matches any broad keyword or requires a match in every specified field.
     * Multiple alternatives within one field are combined with OR; distinct fields are combined with AND.
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
        if (keywords.isEmpty() && fieldKeywords.isEmpty()) {
            return false;
        }
        boolean matchesBroadKeywords = !keywords.isEmpty()
                && containsAnyKeyword(getSearchableText(person), keywords);
        // An absent field group must not make a broad-only search match everything through an empty allMatch.
        boolean matchesEveryField = !fieldKeywords.isEmpty()
                && fieldKeywords.entrySet().stream()
                        .allMatch(entry -> containsAnyKeyword(getFieldText(person, entry.getKey()), entry.getValue()));
        // Field criteria are combined with AND among themselves; broad keywords and field criteria are combined
        // with OR, so an athlete matches if any broad keyword matches or every field criterion matches.
        return matchesBroadKeywords || matchesEveryField;
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
     * Separates event names with line breaks, preventing ordinary single-line keywords from spanning events.
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
