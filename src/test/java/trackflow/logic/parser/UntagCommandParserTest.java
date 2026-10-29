package trackflow.logic.parser;

import static trackflow.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static trackflow.logic.parser.CommandParserTestUtil.assertParseFailure;
import static trackflow.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static trackflow.model.util.SampleDataUtil.getTagSet;
import static trackflow.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.util.Set;

import org.junit.jupiter.api.Test;

import trackflow.logic.Messages;
import trackflow.logic.commands.UntagCommand;
import trackflow.model.tag.Tag;

public class UntagCommandParserTest {

    private static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, UntagCommand.MESSAGE_USAGE);

    private final UntagCommandParser parser = new UntagCommandParser();

    @Test
    public void parse_validArgs_returnsUntagCommand() {
        assertParseSuccess(parser, "1 t/100m t/Long Jump",
                new UntagCommand(INDEX_FIRST_PERSON, getTagSet("100m", "Long Jump")));
    }

    @Test
    public void parse_legacyNameLongerThanLimit_success() {
        // untag accepts stored legacy names that tag would reject, so they can still be removed
        String legacyName = "A".repeat(Tag.MAX_LENGTH + 1);
        assertParseSuccess(parser, "1 t/" + legacyName,
                new UntagCommand(INDEX_FIRST_PERSON, Set.of(Tag.fromStoredName(legacyName))));
    }

    @Test
    public void parse_extraWhitespaceAndDifferentCase_matchesStoredEvent() {
        assertParseSuccess(parser, "1 t/  long   JUMP ",
                new UntagCommand(INDEX_FIRST_PERSON, getTagSet("Long Jump")));
    }

    @Test
    public void parse_invalidArgs_failure() {
        assertParseFailure(parser, "1", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "t/100m", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "1 t/", Tag.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 t/100m*", Tag.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 t/" + "A".repeat(31) + " B", Tag.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 t/100m n/Alice",
                String.format(Messages.MESSAGE_UNKNOWN_PARAMETER, "n/", UntagCommand.MESSAGE_USAGE));
    }
}
