// @@author Austin-Chia
package trackflow.logic.commands;

import static java.util.Objects.requireNonNull;

import java.math.BigInteger;
import java.util.Comparator;

import trackflow.commons.util.ToStringBuilder;
import trackflow.model.Model;
import trackflow.model.person.Person;

/**
 * Sorts the displayed athlete list by a selected field and order, or restores saved roster order.
 * Preserves the current filter.
 * Changes only the display order; the stored roster order is unchanged.
 */
public class SortCommand extends Command {

    public static final String COMMAND_WORD = "sort";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Sorts the displayed athlete list.\n"
            + "Parameters: FIELD [ORDER] or reset\n"
            + "Available fields: name, age, phone, email, address\n"
            + "Available orders: asc, ascending, desc, descending (default: asc)\n"
            + "Use sort reset to restore saved roster order while retaining the current filter.\n"
            + "Example: " + COMMAND_WORD + " age desc";
    public static final String MESSAGE_INVALID_FIELD =
            "Unknown sort field: %1$s. Available fields: name, age, phone, email, address. "
                    + "Use sort reset to restore saved roster order.";
    public static final String MESSAGE_INVALID_ORDER =
            "Unknown sort order: %1$s. Use asc, ascending, desc, or descending.";
    public static final String MESSAGE_SUCCESS =
            "Sorted the displayed athlete list by %1$s in %2$s order.";
    public static final String MESSAGE_RESET_SUCCESS = "Restored saved roster order for the displayed athletes.";

    private static final Comparator<String> CASE_INSENSITIVE_TEXT_COMPARATOR = String.CASE_INSENSITIVE_ORDER;
    private static final Comparator<String> NUMERIC_TEXT_COMPARATOR =
            Comparator.comparing(phone -> new BigInteger(phone.replaceAll("\\D", "")));

    /** Both field and order are null only for a reset command created by {@link #reset()}. */
    private final SortField field;
    private final SortOrder order;

    /**
     * Creates a command that sorts by {@code field} in {@code order}.
     */
    public SortCommand(SortField field, SortOrder order) {
        this.field = requireNonNull(field);
        this.order = requireNonNull(order);
    }

    private SortCommand() {
        field = null;
        order = null;
    }

    /**
     * Creates a command that clears the active sort and restores saved roster order, retaining the filter.
     */
    public static SortCommand reset() {
        return new SortCommand();
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        if (field == null) {
            model.updateSortedPersonList(null);
            return new CommandResult(MESSAGE_RESET_SUCCESS);
        }
        Comparator<Person> comparator = getComparator(field);
        if (order == SortOrder.DESCENDING) {
            comparator = comparator.reversed();
        }
        model.updateSortedPersonList(comparator);
        return new CommandResult(String.format(MESSAGE_SUCCESS, field.displayName, order.displayName));
    }

    /**
     * Returns an ascending comparator: case-insensitive text, numeric phone values, or competition category rank.
     */
    private static Comparator<Person> getComparator(SortField field) {
        return switch (field) {
            case NAME -> Comparator.comparing(person -> person.getName().fullName, CASE_INSENSITIVE_TEXT_COMPARATOR);
            case AGE -> Comparator.comparingInt(person -> getAgeCategoryRank(person.getAgeCategory().value));
            case PHONE -> Comparator.comparing(person -> person.getPhone().value, NUMERIC_TEXT_COMPARATOR);
            case EMAIL -> Comparator.comparing(person -> person.getEmail().value, CASE_INSENSITIVE_TEXT_COMPARATOR);
            case ADDRESS -> Comparator.comparing(person -> person.getAddress().value, CASE_INSENSITIVE_TEXT_COMPARATOR);
        };
    }

    /**
     * Returns the competition order of a canonical category, with Open after the Under 20 category.
     *
     * @throws IllegalArgumentException If {@code ageCategory} is not a canonical supported category.
     */
    private static int getAgeCategoryRank(String ageCategory) {
        return switch (ageCategory) {
            case "Under 14" -> 0;
            case "Under 16" -> 1;
            case "Under 18" -> 2;
            case "Under 20" -> 3;
            case "Open" -> 4;
            default -> throw new IllegalArgumentException("Unsupported age category: " + ageCategory);
        };
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof SortCommand otherCommand
                && field == otherCommand.field
                && order == otherCommand.order;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("field", field)
                .add("order", order)
                .toString();
    }

    /**
     * Fields supported by the sort command.
     */
    public enum SortField {
        NAME("name"),
        AGE("age category"),
        PHONE("phone"),
        EMAIL("email"),
        ADDRESS("address");

        private final String displayName;

        SortField(String displayName) {
            this.displayName = displayName;
        }
    }

    /**
     * Directions supported by the sort command.
     */
    public enum SortOrder {
        ASCENDING("ascending"),
        DESCENDING("descending");

        private final String displayName;

        SortOrder(String displayName) {
            this.displayName = displayName;
        }
    }
}
