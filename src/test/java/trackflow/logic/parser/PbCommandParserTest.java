package trackflow.logic.parser;

import static trackflow.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static trackflow.logic.parser.CommandParserTestUtil.assertParseFailure;
import static trackflow.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static trackflow.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import trackflow.logic.Messages;
import trackflow.logic.commands.PbCommand;
import trackflow.model.person.Performance;

public class PbCommandParserTest {

    private final PbCommandParser parser = new PbCommandParser();

    @Test
    public void parse_allFieldsPresent_success() {
        assertParseSuccess(parser, "1 t/100m v/12.34 d/2026-10-22",
                new PbCommand(INDEX_FIRST_PERSON, new Performance("100m", "12.34", LocalDate.parse("2026-10-22"))));
    }

    @Test
    public void parse_withoutDate_success() {
        assertParseSuccess(parser, "1 t/Long Jump v/6.21m",
                new PbCommand(INDEX_FIRST_PERSON, new Performance("Long Jump", "6.21m", null)));
    }

    @Test
    public void parse_fieldsInAnyOrder_success() {
        assertParseSuccess(parser, "1 v/12.34 t/100m",
                new PbCommand(INDEX_FIRST_PERSON, new Performance("100m", "12.34", null)));
    }

    @Test
    public void parse_missingParts_failure() {
        String expected = String.format(MESSAGE_INVALID_COMMAND_FORMAT, PbCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "1 t/100m", expected); // missing value
        assertParseFailure(parser, "1 v/12.34", expected); // missing event
        assertParseFailure(parser, "t/100m v/12.34", expected); // missing index
        assertParseFailure(parser, "0 t/100m v/12.34", expected); // non-positive index
        assertParseFailure(parser, "x t/100m v/12.34", expected); // non-numeric index
    }

    @Test
    public void parse_invalidValues_failure() {
        assertParseFailure(parser, "1 t/ v/12.34", Performance.EVENT_MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 t/100m v/notatime", Performance.RESULT_MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 t/100m v/1:70", Performance.RESULT_MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 t/100m v/12.34 d/2026-13-40", Performance.DATE_MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_duplicateOrUnknownPrefix_failure() {
        assertParseFailure(parser, "1 t/100m v/12.34 v/12.10",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_VALUE));
        assertParseFailure(parser, "1 t/100m v/12.34 p/999",
                String.format(Messages.MESSAGE_UNKNOWN_PARAMETER, "p/", PbCommand.MESSAGE_USAGE));
    }
}
