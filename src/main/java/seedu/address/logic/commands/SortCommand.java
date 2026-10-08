package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.math.BigInteger;
import java.util.Comparator;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Sorts the displayed athlete list by a selected field and order.
 */
public class SortCommand extends Command {

    public static final String COMMAND_WORD = "sort";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Sorts the displayed athlete list.\n"
            + "Parameters: FIELD [ORDER]\n"
            + "Available fields: name, age, phone, email, address\n"
            + "Available orders: asc, ascending, desc, descending (default: asc)\n"
            + "Example: " + COMMAND_WORD + " age desc";
    public static final String MESSAGE_INVALID_FIELD =
            "Unknown sort field: %1$s. Available fields: name, age, phone, email, address.";
    public static final String MESSAGE_INVALID_ORDER =
            "Unknown sort order: %1$s. Use asc, ascending, desc, or descending.";
    public static final String MESSAGE_SUCCESS =
            "Sorted the displayed athlete list by %1$s in %2$s order.";

    private static final Comparator<String> CASE_INSENSITIVE_TEXT_COMPARATOR = String.CASE_INSENSITIVE_ORDER;
    private static final Comparator<String> NUMERIC_TEXT_COMPARATOR =
            Comparator.comparing(BigInteger::new);

    private final SortField field;
    private final SortOrder order;

    /**
     * Creates a command that sorts by {@code field} in {@code order}.
     */
    public SortCommand(SortField field, SortOrder order) {
        this.field = requireNonNull(field);
        this.order = requireNonNull(order);
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        Comparator<Person> comparator = getComparator(field);
        if (order == SortOrder.DESCENDING) {
            comparator = comparator.reversed();
        }
        model.updateSortedPersonList(comparator);
        return new CommandResult(String.format(MESSAGE_SUCCESS, field.displayName, order.displayName));
    }

    private static Comparator<Person> getComparator(SortField field) {
        return switch (field) {
            case NAME -> Comparator.comparing(person -> person.getName().fullName, CASE_INSENSITIVE_TEXT_COMPARATOR);
            case AGE -> Comparator.comparingInt(person -> getAgeCategoryRank(person.getAgeCategory().value));
            case PHONE -> Comparator.comparing(person -> person.getPhone().value, NUMERIC_TEXT_COMPARATOR);
            case EMAIL -> Comparator.comparing(person -> person.getEmail().value, CASE_INSENSITIVE_TEXT_COMPARATOR);
            case ADDRESS -> Comparator.comparing(person -> person.getAddress().value, CASE_INSENSITIVE_TEXT_COMPARATOR);
        };
    }

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

    /** Fields supported by the sort command. */
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

    /** Directions supported by the sort command. */
    public enum SortOrder {
        ASCENDING("ascending"),
        DESCENDING("descending");

        private final String displayName;

        SortOrder(String displayName) {
            this.displayName = displayName;
        }
    }
}
