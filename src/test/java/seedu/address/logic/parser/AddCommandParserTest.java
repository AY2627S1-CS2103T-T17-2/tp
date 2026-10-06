package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.AddCommand;
import seedu.address.model.person.AgeCategory;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.testutil.PersonBuilder;

public class AddCommandParserTest {
    private final AddCommandParser parser = new AddCommandParser();
    private final String validArgs = " n/Avery Tan a/Under 14 p/91234567 e/avery.tan@example.com";

    @Test
    public void parse_allFieldsPresent_success() {
        Person expected = new PersonBuilder().withName("Avery Tan").withAgeCategory("Under 14")
                .withPhone("91234567").withEmail("avery.tan@example.com")
                .withAddress("").withRemark("").withTags().build();
        assertParseSuccess(parser, validArgs, new AddCommand(expected));
        assertParseSuccess(parser, "  e/avery.tan@example.com p/91234567 a/under   14 n/Avery Tan  ",
                new AddCommand(expected));
    }

    @Test
    public void parse_eachAgeCategory_success() {
        for (String category : new String[] {"Under 14", "Under 16", "Under 18", "Under 20", "Open"}) {
            Person expected = new PersonBuilder().withName("Avery Tan").withAgeCategory(category)
                    .withPhone("91234567").withEmail("avery.tan@example.com")
                    .withAddress("").withRemark("").withTags().build();
            assertParseSuccess(parser, validArgs.replace("Under 14", category), new AddCommand(expected));
        }
    }

    @Test
    public void parse_legacyAddressInsteadOfCategory_failure() {
        assertParseFailure(parser, validArgs.replace("Under 14", "123 Clementi Road"),
                AgeCategory.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_missingField_failure() {
        String expected = String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE);
        for (String field : new String[] {" n/Avery Tan", " a/Under 14", " p/91234567", " e/avery.tan@example.com"}) {
            assertParseFailure(parser, validArgs.replace(field, ""), expected);
        }
        assertParseFailure(parser, "unexpected" + validArgs, expected);
    }

    @Test
    public void parse_repeatedField_failure() {
        for (Prefix prefix : new Prefix[] {CliSyntax.PREFIX_NAME, CliSyntax.PREFIX_AGE_CATEGORY,
            CliSyntax.PREFIX_PHONE, CliSyntax.PREFIX_EMAIL}) {
            assertParseFailure(parser, validArgs + " " + prefix + "invalid",
                    Messages.getErrorMessageForDuplicatePrefixes(prefix));
        }
    }

    @Test
    public void parse_invalidValue_failure() {
        assertParseFailure(parser, validArgs.replace("Avery Tan", ""), Name.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, validArgs.replace("Avery Tan", "Avery-Tan"), Name.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, validArgs.replace("Under 14", ""), AgeCategory.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, validArgs.replace("Under 14", "Under 15"), AgeCategory.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, validArgs.replace("91234567", ""), Phone.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, validArgs.replace("91234567", "+65 9123 4567"), Phone.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, validArgs.replace("avery.tan@example.com", ""), Email.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, validArgs.replace("avery.tan@example.com", "invalid"), Email.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_unsupportedParameter_failure() {
        String expected = String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE);
        assertParseFailure(parser, validArgs + " t/sprints", expected);
        assertParseFailure(parser, validArgs + " r/note", expected);
        assertParseFailure(parser, validArgs + " t/", expected);
    }
}
