package trackflow.logic.parser;

import trackflow.logic.commands.Command;
import trackflow.logic.parser.exceptions.ParseException;

/**
 * Represents a Parser that is able to parse user input into a {@code Command} of type {@code T}.
 *
 * @param <T> Type of command produced by this parser.
 */
public interface Parser<T extends Command> {

    /**
     * Parses {@code userInput} into a command and returns it.
     *
     * @param userInput Non-null input arguments for the command.
     * @return The parsed command, ready for execution.
     * @throws ParseException If the input violates the command's format or field constraints.
     */
    T parse(String userInput) throws ParseException;
}
