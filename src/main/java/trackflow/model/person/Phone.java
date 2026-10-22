package trackflow.model.person;

import static java.util.Objects.requireNonNull;
import static trackflow.commons.util.AppUtil.checkArgument;

/**
 * Represents a Person's phone number in the athlete roster.
 * Guarantees: immutable; is valid as declared in {@link #isValidPhone(String)}
 */
public class Phone {

    public static final String MESSAGE_CONSTRAINTS =
            "Phone numbers must contain at least 3 digits. They may also include spaces, the characters + - ( ), "
                    + "and notes in brackets such as (HP).";

    /** Minimum number of digits a phone number must contain. */
    private static final int MINIMUM_DIGITS = 3;

    /** A bracketed note such as "(HP)" or "(Office)". */
    private static final String BRACKETED_NOTE_REGEX = "\\([^)]*\\)";

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
     * Returns true if a given string is a valid phone number: it has at least three digits and, apart from
     * bracketed notes such as "(HP)", contains only digits, spaces, and the characters + - ( ).
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
     * Returns only the digits of this phone number, ignoring spaces, bracketed notes, and formatting characters.
     * Two phone numbers that share the same digits are treated as equal.
     */
    private String digitsOnly() {
        return value.replaceAll("\\D", "");
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

        return digitsOnly().equals(otherPhone.digitsOnly());
    }

    @Override
    public int hashCode() {
        return digitsOnly().hashCode();
    }

}
