package trackflow.logic.parser;

import static trackflow.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static trackflow.logic.commands.SortCommand.SortField.ADDRESS;
import static trackflow.logic.commands.SortCommand.SortField.AGE;
import static trackflow.logic.commands.SortCommand.SortField.EMAIL;
import static trackflow.logic.commands.SortCommand.SortField.NAME;
import static trackflow.logic.commands.SortCommand.SortField.PHONE;
import static trackflow.logic.commands.SortCommand.SortOrder.ASCENDING;
import static trackflow.logic.commands.SortCommand.SortOrder.DESCENDING;
import static trackflow.logic.parser.CommandParserTestUtil.assertParseFailure;
import static trackflow.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import trackflow.logic.commands.SortCommand;

public class SortCommandParserTest {

    private final SortCommandParser parser = new SortCommandParser();

    @Test
    public void parse_reset_returnsResetCommand() {
        assertParseSuccess(parser, "reset", SortCommand.reset());
        assertParseSuccess(parser, "  ReSeT  ", SortCommand.reset());
    }

    @Test
    public void parse_resetWithExtraArguments_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, SortCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "reset asc", expectedMessage);
        assertParseFailure(parser, "reset desc", expectedMessage);
        assertParseFailure(parser, "reset name extra", expectedMessage);
    }

    @Test
    public void parse_fieldOnly_defaultsToAscending() {
        assertParseSuccess(parser, "name", new SortCommand(NAME, ASCENDING));
        assertParseSuccess(parser, "age", new SortCommand(AGE, ASCENDING));
        assertParseSuccess(parser, "phone", new SortCommand(PHONE, ASCENDING));
        assertParseSuccess(parser, "email", new SortCommand(EMAIL, ASCENDING));
        assertParseSuccess(parser, "address", new SortCommand(ADDRESS, ASCENDING));
    }

    @Test
    public void parse_fieldAndOrder_returnsSortCommand() {
        assertParseSuccess(parser, "name asc", new SortCommand(NAME, ASCENDING));
        assertParseSuccess(parser, "age ascending", new SortCommand(AGE, ASCENDING));
        assertParseSuccess(parser, "phone desc", new SortCommand(PHONE, DESCENDING));
        assertParseSuccess(parser, "email descending", new SortCommand(EMAIL, DESCENDING));
    }

    @Test
    public void parse_mixedCaseAndWhitespace_returnsSortCommand() {
        assertParseSuccess(parser, "  AgE   DeSc  ", new SortCommand(AGE, DESCENDING));
    }

    @Test
    public void parse_missingOrExtraArguments_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, SortCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "   ", expectedMessage);
        assertParseFailure(parser, "name asc extra", expectedMessage);
    }

    @Test
    public void parse_unknownField_throwsParseException() {
        assertParseFailure(parser, "height asc", String.format(SortCommand.MESSAGE_INVALID_FIELD, "height"));
    }

    @Test
    public void parse_unknownOrder_throwsParseException() {
        assertParseFailure(parser, "name upwards", String.format(SortCommand.MESSAGE_INVALID_ORDER, "upwards"));
    }
}
