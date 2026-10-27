package trackflow.logic.commands;

import static java.util.Objects.requireNonNull;
import static trackflow.logic.parser.CliSyntax.PREFIX_ADDRESS;
// @@author qiyuannn
import static trackflow.logic.parser.CliSyntax.PREFIX_AGE_CATEGORY;
// @@author
import static trackflow.logic.parser.CliSyntax.PREFIX_EMAIL;
import static trackflow.logic.parser.CliSyntax.PREFIX_NAME;
import static trackflow.logic.parser.CliSyntax.PREFIX_PHONE;

import trackflow.commons.util.ToStringBuilder;
import trackflow.logic.Messages;
import trackflow.logic.commands.exceptions.CommandException;
import trackflow.model.Model;
import trackflow.model.person.Person;

/**
 * Adds an athlete to the roster.
 */
public class AddCommand extends Command {

    public static final String COMMAND_WORD = "add";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Adds an athlete to the roster. "
            + "Parameters: " + PREFIX_NAME + "NAME " + PREFIX_AGE_CATEGORY + "AGE_CATEGORY "
            + PREFIX_PHONE + "PHONE " + PREFIX_EMAIL + "EMAIL " + PREFIX_ADDRESS + "ADDRESS\n"
            + "Example: " + COMMAND_WORD + " n/Avery Tan a/Under 14 p/91234567 e/avery.tan@example.com "
            + "addr/123 Main Street";

    public static final String MESSAGE_SUCCESS = "New athlete added: %1$s";
    public static final String MESSAGE_DUPLICATE_PERSON = "This athlete already exists in the roster: %1$s.";

    private final Person toAdd;

    /**
     * Creates an AddCommand to add the specified {@code Person}.
     */
    public AddCommand(Person person) {
        requireNonNull(person);
        toAdd = person;
    }

    @Override
    public boolean changesRoster() {
        return true;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        if (model.hasPerson(toAdd)) {
            throw new CommandException(String.format(MESSAGE_DUPLICATE_PERSON, toAdd.getName()));
        }

        // @@author Austin-Chia
        String warning = Messages.getPossibleDuplicateWarning(toAdd, model.getRoster().getPersonList());
        // @@author
        model.addPerson(toAdd);
        return new CommandResult(String.format(MESSAGE_SUCCESS, Messages.formatAthlete(toAdd)) + warning);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof AddCommand otherAddCommand)) {
            return false;
        }

        return toAdd.equals(otherAddCommand.toAdd);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("toAdd", toAdd)
                .toString();
    }
}
