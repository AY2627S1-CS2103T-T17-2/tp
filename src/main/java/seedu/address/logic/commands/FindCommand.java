package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.Model;
import seedu.address.model.person.PersonContainsKeywordsPredicate;

/**
 * Finds athletes whose searchable fields partially match any keyword.
 */
public class FindCommand extends Command {

    public static final String COMMAND_WORD = "find";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Finds athletes when any recorded field contains "
            + "any specified keyword (case-insensitive and partial matches are allowed).\n"
            + "Parameters: KEYWORD [MORE_KEYWORDS]...\n"
            + "Example: " + COMMAND_WORD + " avery 9123 under";
    public static final String MESSAGE_SUCCESS = "Displaying %1$d matching athletes.";
    public static final String MESSAGE_SUCCESS_SINGLE = "Displaying 1 matching athlete.";
    public static final String MESSAGE_NO_MATCHES = "No matching athletes found.";

    private final PersonContainsKeywordsPredicate predicate;

    public FindCommand(PersonContainsKeywordsPredicate predicate) {
        this.predicate = requireNonNull(predicate);
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredPersonList(predicate);
        int athleteCount = model.getFilteredPersonList().size();
        if (athleteCount == 0) {
            return new CommandResult(MESSAGE_NO_MATCHES);
        } else if (athleteCount == 1) {
            return new CommandResult(MESSAGE_SUCCESS_SINGLE);
        }
        return new CommandResult(String.format(MESSAGE_SUCCESS, athleteCount));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof FindCommand otherFindCommand)) {
            return false;
        }

        return predicate.equals(otherFindCommand.predicate);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("predicate", predicate)
                .toString();
    }
}
