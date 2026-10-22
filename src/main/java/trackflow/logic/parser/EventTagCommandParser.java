package trackflow.logic.parser;

import static java.util.Objects.requireNonNull;
import static trackflow.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static trackflow.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import trackflow.commons.core.index.Index;
import trackflow.logic.commands.EventTagCommand;
import trackflow.logic.parser.exceptions.ParseException;
import trackflow.model.tag.Tag;

/**
 * Parses arguments of the form {@code INDEX t/EVENT [t/EVENT]...} shared by the event tag commands.
 *
 * @param <T> the type of command created.
 */
public abstract class EventTagCommandParser<T extends EventTagCommand> implements Parser<T> {

    /**
     * Parses the given {@code String} of arguments and returns the command for execution.
     *
     * @throws ParseException if the user input does not conform to the expected format.
     */
    @Override
    public T parse(String args) throws ParseException {
        requireNonNull(args);
        ParserUtil.verifyNoUnknownPrefixes(args, getUsage(), PREFIX_TAG);
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_TAG);

        Index index;
        try {
            index = ParserUtil.parseIndex(argMultimap.getPreamble());
        } catch (ParseException pe) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, getUsage()), pe);
        }

        List<String> eventNames = argMultimap.getAllValues(PREFIX_TAG);
        if (eventNames.isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, getUsage()));
        }
        Set<Tag> events = new HashSet<>();
        for (String eventName : eventNames) {
            events.add(parseEvent(eventName));
        }
        assert !events.isEmpty();

        return createCommand(index, events);
    }

    /**
     * Parses a newly entered event using the current name constraints.
     * Removal parsers may override this to identify preserved legacy names.
     *
     * @throws ParseException If the event name is invalid.
     */
    protected Tag parseEvent(String eventName) throws ParseException {
        return ParserUtil.parseTag(eventName);
    }

    /**
     * Returns the usage message shown when the arguments are invalid.
     */
    protected abstract String getUsage();

    /**
     * Creates the command from successfully parsed arguments.
     */
    protected abstract T createCommand(Index index, Set<Tag> events);
}
