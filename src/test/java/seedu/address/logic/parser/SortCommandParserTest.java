package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.commands.SortCommand.SortField.ADDRESS;
import static seedu.address.logic.commands.SortCommand.SortField.AGE;
import static seedu.address.logic.commands.SortCommand.SortField.EMAIL;
import static seedu.address.logic.commands.SortCommand.SortField.NAME;
import static seedu.address.logic.commands.SortCommand.SortField.PHONE;
import static seedu.address.logic.commands.SortCommand.SortOrder.ASCENDING;
import static seedu.address.logic.commands.SortCommand.SortOrder.DESCENDING;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.SortCommand;

public class SortCommandParserTest {

    private final SortCommandParser parser = new SortCommandParser();

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
