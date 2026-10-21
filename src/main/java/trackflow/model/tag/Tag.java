package trackflow.model.tag;

import static java.util.Objects.requireNonNull;
import static trackflow.commons.util.AppUtil.checkArgument;

import java.util.Locale;

/**
 * Represents a track event tag (e.g. {@code 100m}, {@code Long Jump}) of an athlete in the roster.
 * Tags are compared case-insensitively, but keep the casing they were created with for display.
 * Guarantees: immutable; name is valid as declared in {@link #isValidTagName(String)}
 */
public class Tag {

    public static final int MAX_LENGTH = 30;

    public static final String MESSAGE_CONSTRAINTS = "Events should be 1 to " + MAX_LENGTH + " characters long, "
            + "contain only letters, digits, spaces and hyphens, and start and end with a letter or digit "
            + "(e.g. 100m, Long Jump, 110m-Hurdles).";

    /*
     * Words of letters/digits, separated by a single space or hyphen.
     */
    public static final String VALIDATION_REGEX = "\\p{Alnum}+([ -]\\p{Alnum}+)*";

    public final String tagName;

    /**
     * Constructs a {@code Tag}.
     *
     * @param tagName A valid tag name.
     */
    public Tag(String tagName) {
        requireNonNull(tagName);
        checkArgument(isValidTagName(tagName), MESSAGE_CONSTRAINTS);
        this.tagName = tagName;
    }

    /**
     * Returns true if a given string is a valid tag name.
     */
    public static boolean isValidTagName(String test) {
        requireNonNull(test);
        return test.length() <= MAX_LENGTH && test.matches(VALIDATION_REGEX);
    }

    private String normalizedName() {
        return tagName.toLowerCase(Locale.ROOT);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Tag otherTag)) {
            return false;
        }

        return normalizedName().equals(otherTag.normalizedName());
    }

    @Override
    public int hashCode() {
        return normalizedName().hashCode();
    }

    /**
     * Formats state as text for viewing.
     */
    public String toString() {
        return '[' + tagName + ']';
    }

}
