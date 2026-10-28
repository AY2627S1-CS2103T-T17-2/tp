package trackflow.model.person;

import static java.util.Objects.requireNonNull;
import static trackflow.commons.util.AppUtil.checkArgument;

/**
 * Represents a Person's phone number in the athlete roster.
 * Guarantees: immutable; is valid as declared in {@link #isValidPhone(String)}
 */
public class Phone {

    public static final String MESSAGE_CONSTRAINTS =
            "Phone numbers must contain at least 3 digits. They may also include spaces and the characters + - ( ). "
                    + "A bracketed country code such as (+65) counts as part of the number; other bracketed text "
                    + "such as (HP) is treated as a note and is ignored.";

    /** Minimum number of significant digits a phone number must contain. */
    private static final int MINIMUM_DIGITS = 3;

    /**
     * A bracketed note such as "(HP)", "(Office)" or "(5)": any bracket group that is not a country code.
     * A bracket containing "+", such as "(+65)", is a country code and is deliberately excluded here.
     */
    private static final String BRACKETED_NOTE_REGEX = "\\([^+)]*\\)";

    /** The characters allowed once bracketed notes are set aside: digits, spaces, and + - ( ). */
    private static final String ALLOWED_CHARACTERS_REGEX = "[0-9 +\\-()]*";

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
     * Returns true if a given string is a valid phone number: setting aside bracketed notes such as "(HP)",
     * it contains only digits, spaces, and the characters + - ( ), and has at least three significant digits.
     * A bracketed country code such as "(+65)" is kept, so its digits count towards the minimum.
     */
    public static boolean isValidPhone(String test) {
        requireNonNull(test);
        String withoutNotes = test.replaceAll(BRACKETED_NOTE_REGEX, "");
        return withoutNotes.matches(ALLOWED_CHARACTERS_REGEX) && countDigits(withoutNotes) >= MINIMUM_DIGITS;
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
     * canonical key for comparing phone numbers; two numbers with the same significant digits are treated as
     * equal, and callers (such as the phone sort) should order numbers by this value rather than the raw text.
     */
    public String getSignificantDigits() {
        return value.replaceAll(BRACKETED_NOTE_REGEX, "").replaceAll("\\D", "");
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
