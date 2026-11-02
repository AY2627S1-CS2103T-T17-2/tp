package trackflow.model.person;

import static java.util.Objects.requireNonNull;
import static trackflow.commons.util.AppUtil.checkArgument;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a Person's phone number in the athlete roster.
 * Guarantees: immutable; is valid as declared in {@link #isValidPhone(String)}
 */
public class Phone {

    public static final String MESSAGE_CONSTRAINTS =
            "Use digits separated by spaces or hyphens, e.g. 9123 4567. "
                    + "Separate a leading country code with a space (+65 91234567) or brackets ((+65)91234567); "
                    + "put nonempty bracketed notes before or after the number, e.g. 9123 4567 (ext 123).";

    public static final String MESSAGE_LENGTH =
            "Phone numbers must have 3–15 digits excluding notes, with at least 3 after a country code. "
                    + "Example: +65 9123 4567.";
    public static final String MESSAGE_ZERO = "Phone numbers cannot consist entirely of zeroes.";

    /**
     * Minimum number of significant digits a phone number must contain.
     */
    private static final int MINIMUM_DIGITS = 3;
    private static final int MAXIMUM_DIGITS = 15;
    private static final Pattern LEADING_COUNTRY_CODE = Pattern.compile(
            "^(?:\\(\\+\\d++\\) ?|\\+\\d++[ -]|\\+65(?=\\d))");

    private static final String SINGAPORE_COUNTRY_CODE = "65";
    private static final int SINGAPORE_LOCAL_LENGTH = 8;

    /**
     * A bracketed note such as "(HP)", "(Office)" or "(5)": any bracket group that is not a country code.
     * A bracket containing "+", such as "(+65)", is a country code and is deliberately excluded here.
     */
    private static final String BRACKETED_NOTE_REGEX = "\\([^+)]*\\)";

    /** A run of digits, optionally split into groups by single hyphens: "9123" or "9123-4567". */
    private static final String DIGIT_GROUP_REGEX = "\\d++(?:-\\d++)*+";

    /** A bracketed country code such as "(+65)". */
    private static final String COUNTRY_CODE_REGEX = "\\(\\+\\d++\\)";

    /** A non-empty bracketed note with balanced brackets, such as "(HP)" or "(65)". */
    private static final String NOTE_REGEX = "\\([^+()]++\\)";

    /**
     * Retains the original grammar for saved numbers; new values use CURRENT_VALIDATION_PATTERN.
     * Possessive repetitions avoid excessive backtracking on long pasted values.
     */
    private static final String VALIDATION_REGEX =
            "(?:\\+?" + DIGIT_GROUP_REGEX + "|" + COUNTRY_CODE_REGEX + "|" + NOTE_REGEX + ")"
                    + "(?: ?(?:" + DIGIT_GROUP_REGEX + "|" + COUNTRY_CODE_REGEX + "|" + NOTE_REGEX + "))*+";
    private static final Pattern VALIDATION_PATTERN = Pattern.compile(VALIDATION_REGEX);

    private static final Pattern CURRENT_VALIDATION_PATTERN = Pattern.compile(
            "(?:" + NOTE_REGEX + " ?)*+(?:\\+?" + DIGIT_GROUP_REGEX
                    + "|" + COUNTRY_CODE_REGEX + " ?" + DIGIT_GROUP_REGEX + ")"
                    + "(?: " + DIGIT_GROUP_REGEX + ")*+(?: ?" + NOTE_REGEX + ")*+");

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
     * Returns true if a given string is a valid phone number: a space-separated sequence of digit groups,
     * country codes and notes (see {@link #VALIDATION_REGEX}) with at least three significant digits. Setting
     * aside bracketed notes such as "(HP)", a bracketed country code such as "(+65)" is kept, so its digits
     * count towards the minimum.
     */
    public static boolean isValidPhone(String test) {
        requireNonNull(test);
        String normalised = test.trim().replaceAll("\\s+", " ");
        // Repetitions are possessive: the token boundaries do not need backtracking, even for long pasted input.
        return VALIDATION_PATTERN.matcher(normalised).matches()
                && countDigits(normalised.replaceAll(BRACKETED_NOTE_REGEX, "")) >= MINIMUM_DIGITS;
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
        String number = normalised.replaceAll(BRACKETED_NOTE_REGEX, "").trim();
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
     * Returns the significant digits of this phone number: the digits of the number itself and of a bracketed
     * country code such as "(+65)", but not the digits of a bracketed note such as "(HP)" or "(5)". This is the
     * key for value equality and sorting. Duplicate detection uses {@link #getComparisonKey()} to additionally
     * equate Singapore local numbers with their international form.
     */
    public String getSignificantDigits() {
        return significantDigitsOf(value);
    }

    /**
     * Returns the significant digits of {@code text}: its digits, keeping a bracketed country code such as
     * "(+65)" but excluding the digits of a bracketed note such as "(HP)" or "(5)". Value equality, sorting,
     * searching, and the duplicate comparison key share this reduction of spaces, hyphens, and bracketed notes.
     */
    static String significantDigitsOf(String text) {
        return text.replaceAll(BRACKETED_NOTE_REGEX, "").replaceAll("\\D", "");
    }

    /**
     * Returns a Singapore-default duplicate key; explicit country codes and the original value are preserved.
     */
    public String getComparisonKey() {
        String number = value.replaceAll(BRACKETED_NOTE_REGEX, "").trim();
        String digits = getSignificantDigits();
        boolean hasCountryCode = number.startsWith("+") || number.startsWith("(+");
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
