package trackflow.model.person;

import static java.util.Objects.requireNonNull;
import static trackflow.commons.util.AppUtil.checkArgument;

/**
 * Represents a Person's name in the athlete roster.
 * Guarantees: immutable; is valid as declared in {@link #isValidName(String)}
 */
public class Name {

    public static final String MESSAGE_CONSTRAINTS =
            "Names should be words of letters or digits separated by spaces, and may join words with a single "
                    + "hyphen or apostrophe (e.g. Mary-Jane, O'Brien). Names should not be blank, and should not "
                    + "start or end with a hyphen or apostrophe";

    /*
     * Words of letters or digits, separated by spaces or joined by a single hyphen or apostrophe, allowing names
     * such as "Mary-Jane" and "O'Brien" while rejecting "-Jane", "Jane-", and "Mary--Jane".
     * Trailing spaces remain valid so that previously saved names still load; commands trim them before parsing.
     */
    public static final String VALIDATION_REGEX = "\\p{Alnum}+(( +|[-'])\\p{Alnum}+)* *";

    public final String fullName;

    /**
     * Constructs a {@code Name}.
     *
     * @param name A valid name.
     */
    public Name(String name) {
        requireNonNull(name);
        checkArgument(isValidName(name), MESSAGE_CONSTRAINTS);
        fullName = name;
    }

    /**
     * Returns true if a given string is a valid name.
     */
    public static boolean isValidName(String test) {
        return test.matches(VALIDATION_REGEX);
    }


    @Override
    public String toString() {
        return fullName;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Name otherName)) {
            return false;
        }

        return fullName.equals(otherName.fullName);
    }

    @Override
    public int hashCode() {
        return fullName.hashCode();
    }

}
