package seedu.address.model.person;

import java.util.Objects;

/**
 * Represents an optional remark about a person in the address book.
 */
public class Remark {

    public final String value;

    public Remark(String remark) {
        value = remark;
    }

    @Override
    public boolean equals(Object other) {
        return other == this
                || (other instanceof Remark otherRemark && value.equals(otherRemark.value));
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
