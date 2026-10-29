package trackflow.model.tag;

import static java.util.Objects.requireNonNull;
import static trackflow.commons.util.AppUtil.checkArgument;

import java.text.Normalizer;

import trackflow.commons.util.StringUtil;

/**
 * Represents a track event tag (e.g. {@code 100m}, {@code Long Jump}) of an athlete in the roster.
 * Tags are compared case-insensitively, but keep the casing they were created with for display.
 * Names are stored in composed Unicode form, so an accent typed as a separate mark matches the single-letter spelling.
 * New names satisfy {@link #isValidTagName(String)}; stored legacy names may also be longer alphanumeric strings.
 * Guarantees: immutable; the original capitalisation is preserved, with equivalent accent spellings composed.
 */
public class Tag {

    public static final int MAX_LENGTH = 30;

    public static final String MESSAGE_CONSTRAINTS = "Events should be 1 to " + MAX_LENGTH
            + " characters long and made of words of letters or digits, "
            + "separated by a single space or a single hyphen "
            + "(e.g. 100m, Long Jump, 110m-Hurdles; not Long - Jump or Long--Jump). "
            + "A word may also contain an apostrophe or a decimal point between digits (e.g. Women's 100m, "
            + "Hammer 7.26kg).";

    /**
     * A word of letters/digits, which may contain an apostrophe or a decimal point between two digits.
     * {@code (?U)} makes letters and digits include non-English ones, such as {@code É} in {@code Saut-Élan}.
     */
    private static final String WORD_REGEX = "\\p{Alnum}++(?:(?:'|(?<=\\d)\\.(?=\\d))\\p{Alnum}++)*+";

    /**
     * Words separated by a single space or hyphen.
     */
    public static final String VALIDATION_REGEX = "(?U)" + WORD_REGEX + "(?:[ -]" + WORD_REGEX + ")*+";

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
        String composedName = compose(tagName);
        checkArgument(isValidTagName(composedName) || allowLegacyName && composedName.matches("\\p{Alnum}+"),
                MESSAGE_CONSTRAINTS);
        this.tagName = composedName;
    }

    /**
     * Restores a saved name without truncation, accepting current names and the older unlimited alphanumeric format.
     * Also supports identifying legacy events for removal; new events must use the public constructor.
     *
     * @param tagName Saved event name, preserved apart from Unicode composition; must not be null.
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
        String composedName = compose(test);
        return composedName.length() <= MAX_LENGTH && composedName.matches(VALIDATION_REGEX);
    }

    /**
     * Returns the name with each letter and its accent marks combined into one character where possible,
     * e.g. {@code E} followed by a combining acute accent becomes {@code É}.
     */
    private static String compose(String name) {
        return Normalizer.normalize(name, Normalizer.Form.NFC);
    }

    private String getNormalizedName() {
        return StringUtil.normalizeForCaseInsensitiveMatching(tagName);
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
