package trackflow.logic.parser;

import java.util.Set;

import trackflow.commons.core.index.Index;
import trackflow.logic.commands.UntagCommand;
import trackflow.model.tag.Tag;

/**
 * Parses input arguments and creates a new {@code UntagCommand} object.
 */
public class UntagCommandParser extends EventTagCommandParser<UntagCommand> {

    @Override
    protected String getUsage() {
        return UntagCommand.MESSAGE_USAGE;
    }

    @Override
    protected UntagCommand createCommand(Index index, Set<Tag> events) {
        return new UntagCommand(index, events);
    }
}
