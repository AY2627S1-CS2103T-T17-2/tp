package trackflow.model.person;

import static java.util.Objects.requireNonNull;
import static trackflow.commons.util.AppUtil.checkArgument;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a Person's phone number in the athlete roster.
 * Guarantees: immutable; is valid as declared in {@link #isValidPhone(String)}
 *
 * <p>Bracketed text is interpreted by position. A bracketed code written before the number, such as
 * {@code (+65)} or {@code (212)}, is part of the number, so {@code (212) 1234} has the digits {@code 2121234}.
 * Bracketed text written after the number, such as {@code (HP)} or {@code (ext 5)}, is a note and is ignored by
 * comparison, sorting and digit searches. A bracketed note that contains no plain digit sequence may also be
 * written before the number, where it is likewise ignored.
 */
public class Phone {

    public static final String MESSAGE_CONSTRAINTS =
            "Use digits separated by spaces or hyphens, e.g. 9123 4567. "
                    + "A bracketed code before the number is part of it, e.g. (+65) 9123 4567 or (212) 555 0199; "
                    + "separate an unbracketed country code with a space (+65 9123 4567). "
                    + "Put nonempty bracketed notes after the number, e.g. 9123 4567 (ext 123).";

    public static final String MESSAGE_LENGTH =
            "Phone numbers must have 3–15 digits excluding notes, with at least 3 after a country or area code. "
                    + "Example: +65 9123 4567.";
    public static final String MESSAGE_ZERO = "Phone numbers cannot consist entirely of zeroes.";

    /**
     * Minimum number of significant digits a phone number must contain.
     */
    private static final int MINIMUM_DIGITS = 3;
    private static final int MAXIMUM_DIGITS = 15;

    private static final String SINGAPORE_COUNTRY_CODE = "65";
    private static final int SINGAPORE_LOCAL_LENGTH = 8;

    /** A run of digits, optionally split into groups by single hyphens: "9123" or "9123-4567". */
    private static final String DIGIT_GROUP_REGEX = "\\d++(?:-\\d++)*+";

    /** A bracketed country or area code written before the number, such as "(+65)" or "(212)". */
    private static final String CODE_REGEX = "\\(\\+?\\d++\\)";

    /** A non-empty bracketed note with balanced brackets and no plus sign, such as "(HP)" or "(ext 123)". */
    private static final String NOTE_REGEX = "\\([^+()]++\\)";

    /** A note written before the number: any bracket group that is not a bracketed code. */
    private static final String LEADING_NOTE_REGEX = "\\((?!\\+?\\d++\\))[^+()]++\\)";

    /** A note for new values, which must also contain a non-space character: "(HP)" but not "( )". */
    private static final String NONBLANK_NOTE_REGEX = "\\((?=[^+()]*[^+()\\s])[^+()]++\\)";

    /** A note before the number in new values: non-blank and not a bracketed code. */
    private static final String LEADING_NONBLANK_NOTE_REGEX =
            "\\((?!\\+?\\d++\\))(?=[^+()]*[^+()\\s])[^+()]++\\)";

    /**
     * Notes before the number, which contribute no digits.
     */
    private static final Pattern LEADING_NOTES_PATTERN = Pattern.compile("^(?:" + LEADING_NOTE_REGEX + " ?)++");

    /**
     * Notes after the number, which contribute no digits. The preceding digit keeps a bracketed code that is
     * written on its own, such as a search keyword "(212)", from being treated as a note.
     */
    private static final Pattern TRAILING_NOTES_PATTERN = Pattern.compile("(?<=\\d)(?: ?" + NOTE_REGEX + ")++$");

    /**
     * A country or area code at the start of the number: bracketed with an optional plus, or unbracketed with a
     * plus and separated from the subscriber number by a space or hyphen. The compact Singapore form "+65" is
     * also recognised without a separator.
     */
    private static final Pattern LEADING_COUNTRY_CODE = Pattern.compile(
            "^(?:" + CODE_REGEX + " ?|\\+\\d++[ -]|\\+" + SINGAPORE_COUNTRY_CODE + "(?=\\d))");

    /**
     * Any bracket group that is not an explicit "(+65)"-style country code. Used only by the legacy grammar's
     * digit count, which older saved rosters rely on.
     */
    private static final String LEGACY_BRACKETED_NOTE_REGEX = "\\([^+)]*\\)";

    /** A bracketed country code with an explicit plus, the only bracketed code the legacy grammar knew. */
    private static final String LEGACY_COUNTRY_CODE_REGEX = "\\(\\+\\d++\\)";

    /**
     * Retains the original grammar for saved numbers; new values use CURRENT_VALIDATION_PATTERN.
     * Possessive repetitions avoid excessive backtracking on long pasted values.
     */
    private static final String VALIDATION_REGEX =
            "(?:\\+?" + DIGIT_GROUP_REGEX + "|" + LEGACY_COUNTRY_CODE_REGEX + "|" + NOTE_REGEX + ")"
                    + "(?: ?(?:" + DIGIT_GROUP_REGEX + "|" + LEGACY_COUNTRY_CODE_REGEX + "|" + NOTE_REGEX + "))*+";
    private static final Pattern VALIDATION_PATTERN = Pattern.compile(VALIDATION_REGEX);

    /**
     * New input: optional notes, then the number with at most one leading code, then optional notes.
     * No bracketed text may split the digit groups.
     */
    private static final Pattern CURRENT_VALIDATION_PATTERN = Pattern.compile(
            "(?:" + LEADING_NONBLANK_NOTE_REGEX + " ?)*+(?:\\+?" + DIGIT_GROUP_REGEX
                    + "|" + CODE_REGEX + " ?" + DIGIT_GROUP_REGEX + ")"
                    + "(?: " + DIGIT_GROUP_REGEX + ")*+(?: ?" + NONBLANK_NOTE_REGEX + ")*+");

    public final String value;

    /**
     * Constructs a {@code Phone}.
     *
     * @param phone A valid phone number.
     */
    public Phone(String phone) {
        requireNonNull(phone);
        checkArgument(isValidPhone(phone), MESSAGE_CONSTRAINTS);
        value = phone.trim().replaceAll("\\s+", " ");
    }

    /**
     * Returns true if a given string is a valid saved phone number: a space-separated sequence of digit groups,
     * country codes and notes (see {@link #VALIDATION_REGEX}) with at least three digits outside bracketed notes.
     * This is the lenient grammar that older saved rosters rely on; new input is checked with
     * {@link #isValidNewPhone(String)}.
     */
    public static boolean isValidPhone(String test) {
        requireNonNull(test);
        String normalised = test.trim().replaceAll("\\s+", " ");
        // Repetitions are possessive: the token boundaries do not need backtracking, even for long pasted input.
        return VALIDATION_PATTERN.matcher(normalised).matches()
                && countDigits(normalised.replaceAll(LEGACY_BRACKETED_NOTE_REGEX, "")) >= MINIMUM_DIGITS;
    }

    /**
     * Validates new command input while keeping older saved phone formats readable.
     */
    public static boolean isValidNewPhone(String test) {
        return getNewPhoneValidationError(test).isEmpty();
    }

    /**
     * Returns an actionable error for new command input, or an empty string if valid.
     * Saved values retain their original validation rules to avoid making older rosters unreadable.
     */
    public static String getNewPhoneValidationError(String test) {
        requireNonNull(test);
        String normalised = test.trim().replaceAll("\\s+", " ");
        if (!CURRENT_VALIDATION_PATTERN.matcher(normalised).matches()) {
            return MESSAGE_CONSTRAINTS;
        }
        String number = numberOf(normalised);
        Matcher countryCode = LEADING_COUNTRY_CODE.matcher(number);
        if (number.startsWith("+") && !countryCode.find()) {
            return MESSAGE_CONSTRAINTS;
        }
        String subscriber = countryCode.replaceFirst("");
        String digits = significantDigitsOf(number);
        if (digits.length() > MAXIMUM_DIGITS || countDigits(subscriber) < MINIMUM_DIGITS) {
            return MESSAGE_LENGTH;
        }
        if (subscriber.replaceAll("\\D", "").chars().allMatch(digit -> digit == '0')) {
            return MESSAGE_ZERO;
        }
        return "";
    }

    /**
     * Returns the number of digit characters in {@code text}.
     */
    private static int countDigits(String text) {
        int digits = 0;
        for (int i = 0; i < text.length(); i++) {
            if (Character.isDigit(text.charAt(i))) {
                digits++;
            }
        }
        return digits;
    }

    /**
     * Returns {@code text} without the notes before and after the number, keeping a leading bracketed code.
     * Bracketed text between digit groups, which only the legacy grammar allows, is left in place; its letters are
     * dropped by the digit reduction anyway.
     */
    private static String numberOf(String text) {
        String number = LEADING_NOTES_PATTERN.matcher(text).replaceFirst("");
        return TRAILING_NOTES_PATTERN.matcher(number).replaceFirst("");
    }

    /**
     * Returns the significant digits of this phone number: the digits of the number itself and of a bracketed
     * code written before it, such as "(+65)" or "(212)", but not the digits of a bracketed note written after
     * it, such as "(ext 5)". This is the key for value equality and sorting. Duplicate detection uses
     * {@link #getComparisonKey()} to additionally equate Singapore local numbers with their international form.
     */
    public String getSignificantDigits() {
        return significantDigitsOf(value);
    }

    /**
     * Returns the significant digits of {@code text}: its digits, keeping a bracketed code before the number such
     * as "(+65)" or "(212)" but excluding the digits of a bracketed note after it such as "(5)". Value equality,
     * sorting, searching, and the duplicate comparison key share this reduction of spaces, hyphens, and notes.
     */
    static String significantDigitsOf(String text) {
        return numberOf(text).replaceAll("\\D", "");
    }

    /**
     * Returns a Singapore-default duplicate key; explicit country or area codes and the original value are preserved.
     */
    public String getComparisonKey() {
        String number = numberOf(value);
        String digits = getSignificantDigits();
        boolean hasCountryCode = number.startsWith("+") || number.startsWith("(");
        if (!hasCountryCode && digits.length() == SINGAPORE_LOCAL_LENGTH) {
            return SINGAPORE_COUNTRY_CODE + digits;
        }
        return digits;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Phone otherPhone)) {
            return false;
        }

        return getSignificantDigits().equals(otherPhone.getSignificantDigits());
    }

    @Override
    public int hashCode() {
        return getSignificantDigits().hashCode();
    }

}
