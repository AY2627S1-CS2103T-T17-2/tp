package seedu.address.logic.parser;

import seedu.address.logic.commands.ListAthleteCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new {@code ListAthleteCommand} object.
 */
public class ListAthleteCommandParser implements Parser<ListAthleteCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the {@code ListAthleteCommand}.
     *
     * @throws ParseException If the input contains arguments.
     */
    public ListAthleteCommand parse(String arguments) throws ParseException {
        if (!arguments.trim().isEmpty()) {
            throw new ParseException(ListAthleteCommand.MESSAGE_USAGE);
        }
        return new ListAthleteCommand();
    }
}
