package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.FindCommand;
import seedu.address.model.person.PersonContainsKeywordsPredicate;
import seedu.address.model.person.PersonContainsKeywordsPredicate.SearchField;

public class FindCommandParserTest {

    private FindCommandParser parser = new FindCommandParser();

    @Test
    public void parse_emptyArg_throwsParseException() {
        assertParseFailure(parser, "     ", String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_validArgs_returnsFindCommand() {
        // no leading and trailing whitespaces
        FindCommand expectedFindCommand =
                new FindCommand(new PersonContainsKeywordsPredicate(List.of("Alice", "9123")));
        assertParseSuccess(parser, "Alice 9123", expectedFindCommand);

        // multiple whitespaces between keywords
        assertParseSuccess(parser, " \n Alice \n \t 9123  \t", expectedFindCommand);
    }

    @Test
    public void parse_fieldScopedArgs_returnsFindCommand() {
        PersonContainsKeywordsPredicate predicate = new PersonContainsKeywordsPredicate(List.of(), Map.of(
                SearchField.NAME, List.of("und"),
                SearchField.AGE_CATEGORY, List.of("Open"),
                SearchField.REMARK, List.of("injured")));

        assertParseSuccess(parser, " n/und a/Open r/injured", new FindCommand(predicate));
        assertParseSuccess(parser, " N/und A/Open R/injured", new FindCommand(predicate));
    }

    @Test
    public void parse_broadAndFieldScopedArgs_returnsFindCommand() {
        PersonContainsKeywordsPredicate predicate = new PersonContainsKeywordsPredicate(
                List.of("avery", "9123"), Map.of(SearchField.EMAIL, List.of("example.com")));

        assertParseSuccess(parser, " avery 9123 e/example.com", new FindCommand(predicate));
    }

    @Test
    public void parse_emptyFieldValue_throwsParseException() {
        assertParseFailure(parser, " n/",
                String.format(FindCommand.MESSAGE_EMPTY_FIELD, PREFIX_NAME, FindCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_repeatedFieldPrefix_throwsParseException() {
        assertParseFailure(parser, " n/avery n/tan",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME));
    }

    @Test
    public void parse_unknownParameter_throwsParseException() {
        assertParseFailure(parser, " x/value",
                String.format(Messages.MESSAGE_UNKNOWN_PARAMETER, "x/", FindCommand.MESSAGE_USAGE));
        assertParseFailure(parser, " t/sprint",
                String.format(Messages.MESSAGE_UNKNOWN_PARAMETER, "t/", FindCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_slashInScopedRemark_returnsFindCommand() {
        PersonContainsKeywordsPredicate predicate = new PersonContainsKeywordsPredicate(List.of(),
                Map.of(SearchField.REMARK, List.of("Available Mon/Tue")));

        assertParseSuccess(parser, " r/Available Mon/Tue", new FindCommand(predicate));
    }

}
