package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.Model;
import seedu.address.model.person.AgeCategory;
import seedu.address.model.person.AgeCategoryPredicate;

/**
 * Lists athletes in one age category, replacing the current filter.
 */
public class FilterCommand extends Command {
    public static final String COMMAND_WORD = "filter";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Lists athletes in the specified age category.\n"
            + "Parameters: a/AGE_CATEGORY\n"
            + "Example: " + COMMAND_WORD + " a/Under 14";
    public static final String MESSAGE_SUCCESS = "Displaying %1$d athletes in age category %2$s.";
    public static final String MESSAGE_SUCCESS_SINGLE = "Displaying 1 athlete in age category %1$s.";
    public static final String MESSAGE_NO_MATCHES = "No athletes found in age category %1$s (0 matches).";

    private final AgeCategory ageCategory;

    public FilterCommand(AgeCategory ageCategory) {
        this.ageCategory = requireNonNull(ageCategory);
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredPersonList(new AgeCategoryPredicate(ageCategory));
        int count = model.getFilteredPersonList().size();
        String feedback = count == 0
                ? String.format(MESSAGE_NO_MATCHES, ageCategory)
                : count == 1
                        ? String.format(MESSAGE_SUCCESS_SINGLE, ageCategory)
                        : String.format(MESSAGE_SUCCESS, count, ageCategory);
        return new CommandResult(feedback);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof FilterCommand command
                && ageCategory.equals(command.ageCategory);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("ageCategory", ageCategory).toString();
    }
}
