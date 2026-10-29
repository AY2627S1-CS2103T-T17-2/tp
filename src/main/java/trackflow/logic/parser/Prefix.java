package trackflow.logic.parser;

import static java.util.Objects.requireNonNull;

/**
 * A prefix that marks the beginning of an argument in an arguments string.
 * For example, {@code n/} in {@code add n/Avery Tan ...}.
 */
public class Prefix {
    private final String prefix;

    /**
     * Constructs a prefix using the supplied text. An empty prefix represents the argument preamble.
     *
     * @throws NullPointerException If the prefix text is null.
     */
    public Prefix(String prefix) {
        this.prefix = requireNonNull(prefix);
    }

    public String getPrefix() {
        return prefix;
    }

    @Override
    public String toString() {
        return getPrefix();
    }

    @Override
    public int hashCode() {
        return prefix.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Prefix otherPrefix)) {
            return false;
        }

        return prefix.equals(otherPrefix.prefix);
    }
}
