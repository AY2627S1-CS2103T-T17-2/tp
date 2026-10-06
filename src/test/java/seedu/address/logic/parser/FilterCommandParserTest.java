package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.FilterCommand;
import seedu.address.model.person.AgeCategory;

public class FilterCommandParserTest {
    private final FilterCommandParser parser = new FilterCommandParser();

    @Test
    public void parse_supportedCategories_success() {
        for (String category : new String[] {"Under 14", "Under 16", "Under 18", "Under 20", "Open"}) {
            assertParseSuccess(parser, " a/" + category, new FilterCommand(new AgeCategory(category)));
        }
        assertParseSuccess(parser, "  a/uNdEr   14  ", new FilterCommand(new AgeCategory("Under 14")));
    }

    @Test
    public void parse_missingPrefixOrUnexpectedPreamble_failure() {
        for (String input : new String[] {"", "  ", " Under 14", " A/Under 14", " extra a/Open", " n/Amy a/Open"}) {
            assertParseFailure(parser, input,
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, FilterCommand.MESSAGE_USAGE));
        }
    }

    @Test
    public void parse_invalidCategoryOrExtraArguments_failure() {
        for (String input : new String[] {"", "Under 15", "14", "Under", "Open extra", "Open n/Amy", "Open t/sprint",
            "Open x/other", "Under 14, Under 16"}) {
            assertParseFailure(parser, " a/" + input, AgeCategory.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_duplicatePrefix_failure() {
        assertParseFailure(parser, " a/Open a/Under 14",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_AGE_CATEGORY));
        assertParseFailure(parser, " a/Open a/Open",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_AGE_CATEGORY));
    }
}
