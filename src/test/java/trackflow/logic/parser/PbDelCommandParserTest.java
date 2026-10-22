package trackflow.logic.parser;

import static trackflow.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static trackflow.logic.parser.CommandParserTestUtil.assertParseFailure;
import static trackflow.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static trackflow.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static trackflow.testutil.TypicalIndexes.INDEX_SECOND_PERSON;

import org.junit.jupiter.api.Test;

import trackflow.logic.commands.PbDelCommand;

public class PbDelCommandParserTest {

    private final PbDelCommandParser parser = new PbDelCommandParser();

    @Test
    public void parse_twoIndexes_success() {
        assertParseSuccess(parser, "1 2", new PbDelCommand(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON));
        assertParseSuccess(parser, "  1   2  ", new PbDelCommand(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON));
    }

    @Test
    public void parse_invalid_failure() {
        String expected = String.format(MESSAGE_INVALID_COMMAND_FORMAT, PbDelCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "1", expected); // only one index
        assertParseFailure(parser, "1 2 3", expected); // too many
        assertParseFailure(parser, "", expected); // empty
        assertParseFailure(parser, "1 x", expected); // non-numeric
        assertParseFailure(parser, "0 2", expected); // non-positive
        assertParseFailure(parser, "-1 2", expected); // negative
    }
}
