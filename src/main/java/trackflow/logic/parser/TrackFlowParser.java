package trackflow.logic.parser;

import static trackflow.logic.Messages.MESSAGE_EMPTY_COMMAND;
import static trackflow.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static trackflow.logic.Messages.MESSAGE_UNKNOWN_COMMAND;

import java.util.Locale;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import trackflow.commons.core.LogsCenter;
import trackflow.logic.commands.AddCommand;
import trackflow.logic.commands.ClearCommand;
import trackflow.logic.commands.Command;
import trackflow.logic.commands.DeleteCommand;
import trackflow.logic.commands.EditCommand;
import trackflow.logic.commands.ExitCommand;
import trackflow.logic.commands.FilterCommand;
import trackflow.logic.commands.FindCommand;
import trackflow.logic.commands.HelpCommand;
import trackflow.logic.commands.ListAthleteCommand;
import trackflow.logic.commands.RemarkCommand;
import trackflow.logic.commands.SortCommand;
import trackflow.logic.commands.TagCommand;
import trackflow.logic.commands.UntagCommand;
import trackflow.logic.parser.exceptions.ParseException;

/**
 * Parses user input.
 */
public class TrackFlowParser {

    /**
     * Separates one command word from its arguments, including arguments pasted across lines.
     * The arguments are passed to one command parser; this does not execute a batch of commands.
     */
    private static final Pattern BASIC_COMMAND_FORMAT =
            Pattern.compile("(?<commandWord>\\S+)(?<arguments>.*)", Pattern.DOTALL);
    private static final Logger logger = LogsCenter.getLogger(TrackFlowParser.class);

    /**
     * Parses user input into command for execution.
     *
     * @param userInput full user input string
     * @return the command based on the user input
     * @throws ParseException if the user input does not conform to the expected format
     */
    public Command parseCommand(String userInput) throws ParseException {
        final Matcher matcher = BASIC_COMMAND_FORMAT.matcher(userInput.trim());
        if (!matcher.matches()) {
            throw new ParseException(MESSAGE_EMPTY_COMMAND);
        }

        final String commandWord = matcher.group("commandWord");
        final String normalizedCommandWord = commandWord.toLowerCase(Locale.ROOT);
        final String arguments = matcher.group("arguments");

        // Note to developers: Change LOG_LEVEL in LogsCenter to enable lower level (i.e., FINE, FINER and lower)
        // log messages such as the one below.
        // Lower level log messages are used sparingly to minimize noise in the code.
        logger.fine("Command word: " + commandWord + "; Arguments: " + arguments);

        return switch (normalizedCommandWord) {
            case AddCommand.COMMAND_WORD -> new AddCommandParser().parse(arguments);
            case EditCommand.COMMAND_WORD -> new EditCommandParser().parse(arguments);
            case DeleteCommand.COMMAND_WORD -> new DeleteCommandParser().parse(arguments);
            case ClearCommand.COMMAND_WORD -> parseNoArguments(arguments, new ClearCommand(),
                    ClearCommand.MESSAGE_USAGE);
            case FilterCommand.COMMAND_WORD -> new FilterCommandParser().parse(arguments);
            case FindCommand.COMMAND_WORD -> new FindCommandParser().parse(arguments);
            case ListAthleteCommand.COMMAND_WORD -> new ListAthleteCommandParser().parse(arguments);
            case ExitCommand.COMMAND_WORD -> parseNoArguments(arguments, new ExitCommand(), ExitCommand.MESSAGE_USAGE);
            case HelpCommand.COMMAND_WORD -> parseNoArguments(arguments, new HelpCommand(), HelpCommand.MESSAGE_USAGE);
            case RemarkCommand.COMMAND_WORD -> new RemarkCommandParser().parse(arguments);
            case SortCommand.COMMAND_WORD -> new SortCommandParser().parse(arguments);
            case TagCommand.COMMAND_WORD -> new TagCommandParser().parse(arguments);
            case UntagCommand.COMMAND_WORD -> new UntagCommandParser().parse(arguments);
            default -> {
                logger.finer("This user input caused a ParseException: " + userInput);
                throw new ParseException(String.format(MESSAGE_UNKNOWN_COMMAND, commandWord));
            }
        };
    }

    private static Command parseNoArguments(String arguments, Command command, String messageUsage)
            throws ParseException {
        if (!arguments.trim().isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, messageUsage));
        }
        return command;
    }

}
