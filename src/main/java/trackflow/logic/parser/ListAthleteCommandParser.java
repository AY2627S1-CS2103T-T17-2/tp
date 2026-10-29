package trackflow.logic.parser;

import trackflow.logic.commands.ListAthleteCommand;
import trackflow.logic.parser.exceptions.ParseException;

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
        if (!CommandWhitespace.strip(arguments).isEmpty()) {
            throw new ParseException(ListAthleteCommand.MESSAGE_USAGE);
        }
        return new ListAthleteCommand();
    }
}
