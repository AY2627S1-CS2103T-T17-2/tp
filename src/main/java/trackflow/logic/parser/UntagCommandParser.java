package trackflow.logic.parser;

import java.util.Set;

import trackflow.commons.core.index.Index;
import trackflow.logic.commands.UntagCommand;
import trackflow.logic.parser.exceptions.ParseException;
import trackflow.model.tag.Tag;

/**
 * Parses input arguments and creates a new {@code UntagCommand} object.
 */
public class UntagCommandParser extends EventTagCommandParser<UntagCommand> {

    /**
     * Parses a current event or a longer legacy alphanumeric name so that older events remain removable.
     */
    @Override
    protected Tag parseEvent(String eventName) throws ParseException {
        String normalizedName = eventName.trim().replaceAll("\\s+", " ");
        try {
            return Tag.fromStoredName(normalizedName);
        } catch (IllegalArgumentException e) {
            throw new ParseException(Tag.MESSAGE_CONSTRAINTS, e);
        }
    }

    @Override
    protected String getUsage() {
        return UntagCommand.MESSAGE_USAGE;
    }

    @Override
    protected UntagCommand createCommand(Index index, Set<Tag> events) {
        return new UntagCommand(index, events);
    }
}
