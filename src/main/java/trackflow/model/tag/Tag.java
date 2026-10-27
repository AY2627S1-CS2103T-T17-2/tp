package trackflow.model.tag;

import static java.util.Objects.requireNonNull;
import static trackflow.commons.util.AppUtil.checkArgument;

import java.util.Locale;

/**
 * Represents a track event tag (e.g. {@code 100m}, {@code Long Jump}) of an athlete in the roster.
 * Tags are compared case-insensitively, but keep the casing they were created with for display.
 * New names satisfy {@link #isValidTagName(String)}; stored legacy names may also be longer alphanumeric strings.
 * Guarantees: immutable; the original name is preserved.
 */
public class Tag {

    public static final int MAX_LENGTH = 30;

    public static final String MESSAGE_CONSTRAINTS = "Events should be 1 to " + MAX_LENGTH + " characters long and "
            + "made of words of letters or digits, separated by a single space or a single hyphen "
            + "(e.g. 100m, Long Jump, 110m-Hurdles; not Long - Jump or Long--Jump).";

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
        this(tagName, false);
    }

    private Tag(String tagName, boolean allowLegacyName) {
        requireNonNull(tagName);
        checkArgument(isValidTagName(tagName) || allowLegacyName && tagName.matches("\\p{Alnum}+"),
                MESSAGE_CONSTRAINTS);
        this.tagName = tagName;
    }

    /**
     * Restores a saved name without truncation, accepting current names and the older unlimited alphanumeric format.
     * Also supports identifying legacy events for removal; new events must use the public constructor.
     *
     * @param tagName Saved event name, preserved exactly; must not be null.
     * @return The restored tag.
     * @throws IllegalArgumentException If the name satisfies neither the current nor the legacy format.
     */
    public static Tag fromStoredName(String tagName) {
        return new Tag(tagName, true);
    }

    /**
     * Returns true if a given string is a valid tag name.
     */
    public static boolean isValidTagName(String test) {
        requireNonNull(test);
        return test.length() <= MAX_LENGTH && test.matches(VALIDATION_REGEX);
    }

    private String getNormalizedName() {
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

        return getNormalizedName().equals(otherTag.getNormalizedName());
    }

    @Override
    public int hashCode() {
        return getNormalizedName().hashCode();
    }

    /**
     * Formats state as text for viewing.
     */
    public String toString() {
        return '[' + tagName + ']';
    }

}
