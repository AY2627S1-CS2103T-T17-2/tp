package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.List;

/**
 * An immutable, coach-assigned competition age category.
 */
public final class AgeCategory {
    public static final String MESSAGE_CONSTRAINTS =
            "Age category must be Under 14, Under 16, Under 18, Under 20, or Open.";
    private static final List<String> CATEGORIES = List.of("Under 14", "Under 16", "Under 18", "Under 20", "Open");

    public final String value;

    /**
     * Constructs a category, ignoring case and normalizing whitespace.
     */
    public AgeCategory(String category) {
        requireNonNull(category);
        checkArgument(isValidAgeCategory(category), MESSAGE_CONSTRAINTS);
        value = CATEGORIES.stream().filter(candidate -> candidate.equalsIgnoreCase(normalize(category)))
                .findFirst().orElseThrow();
    }

    /**
     * Returns whether the input names a supported category.
     */
    public static boolean isValidAgeCategory(String category) {
        requireNonNull(category);
        return CATEGORIES.stream().anyMatch(candidate -> candidate.equalsIgnoreCase(normalize(category)));
    }

    private static String normalize(String category) {
        return category.trim().replaceAll("\\s+", " ");
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof AgeCategory category && value.equals(category.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
