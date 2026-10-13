package trackflow.logic.parser;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static trackflow.logic.parser.CommandParserTestUtil.assertParseFailure;

import org.junit.jupiter.api.Test;

import trackflow.logic.commands.ListAthleteCommand;

public class ListAthleteCommandParserTest {

    private final ListAthleteCommandParser parser = new ListAthleteCommandParser();

    @Test
    public void parse_noArguments_returnsListAthleteCommand() throws Exception {
        assertInstanceOf(ListAthleteCommand.class, parser.parse(""));
        assertInstanceOf(ListAthleteCommand.class, parser.parse("   \t\n"));
    }

    @Test
    public void parse_argumentsPresent_throwsParseException() {
        assertParseFailure(parser, "1", ListAthleteCommand.MESSAGE_USAGE);
        assertParseFailure(parser, " n/Avery Tan", ListAthleteCommand.MESSAGE_USAGE);
    }
}
