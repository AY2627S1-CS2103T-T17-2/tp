package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ListCommand;

public class ListCommandParserTest {

    private final ListCommandParser parser = new ListCommandParser();

    @Test
    public void parse_noArguments_returnsListCommand() throws Exception {
        assertInstanceOf(ListCommand.class, parser.parse(""));
        assertInstanceOf(ListCommand.class, parser.parse("   \t\n"));
    }

    @Test
    public void parse_argumentsPresent_throwsParseException() {
        assertParseFailure(parser, "1", ListCommand.MESSAGE_USAGE);
        assertParseFailure(parser, " n/Avery Tan", ListCommand.MESSAGE_USAGE);
    }
}
