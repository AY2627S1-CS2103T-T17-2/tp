package trackflow.logic.parser;

import java.util.Set;

import trackflow.commons.core.index.Index;
import trackflow.logic.commands.TagCommand;
import trackflow.model.tag.Tag;

/**
 * Parses input arguments and creates a new {@code TagCommand} object.
 */
public class TagCommandParser extends EventTagCommandParser<TagCommand> {

    @Override
    protected String getUsage() {
        return TagCommand.MESSAGE_USAGE;
    }

    @Override
    protected TagCommand createCommand(Index index, Set<Tag> events) {
        return new TagCommand(index, events);
    }
}
