package seedu.address.logic.parser;

import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new {@code ListCommand} object.
 */
public class ListCommandParser implements Parser<ListCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the {@code ListCommand}.
     *
     * @throws ParseException If the input contains arguments.
     */
    public ListCommand parse(String arguments) throws ParseException {
        if (!arguments.trim().isEmpty()) {
            throw new ParseException(ListCommand.MESSAGE_USAGE);
        }
        return new ListCommand();
    }
}
