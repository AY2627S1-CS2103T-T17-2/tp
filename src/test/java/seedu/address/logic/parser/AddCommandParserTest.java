package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.AddCommand;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Address;
import seedu.address.model.person.AgeCategory;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.PersonBuilder;

public class AddCommandParserTest {
    private final AddCommandParser parser = new AddCommandParser();
    private final String validArgs = " n/Avery Tan a/Under 14 p/91234567 e/avery.tan@example.com";

    @Test
    public void parse_requiredFieldsOnly_success() {
        Person expected = new PersonBuilder().withName("Avery Tan").withAgeCategory("Under 14")
                .withPhone("91234567").withEmail("avery.tan@example.com")
                .withAddress("").withRemark("").withTags().build();
        assertParseSuccess(parser, validArgs, new AddCommand(expected));
        assertParseSuccess(parser, "  e/avery.tan@example.com p/91234567 a/under   14 n/Avery Tan  ",
                new AddCommand(expected));
        assertParseSuccess(parser, "  E/avery.tan@example.com P/91234567 A/under 14 N/Avery Tan  ",
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
    public void parse_unknownParameter_failure() {
        assertParseFailure(parser, validArgs + " x/value",
                String.format(Messages.MESSAGE_UNKNOWN_PARAMETER, "x/", AddCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_slashInFreeText_success() {
        Person expected = new PersonBuilder().withName("Avery Tan").withAgeCategory("Under 14")
                .withPhone("91234567").withEmail("avery.tan@example.com")
                .withAddress("Level Two/Three").withRemark("Available Mon/Tue").withTags().build();

        assertParseSuccess(parser, validArgs + " addr/Level Two/Three r/Available Mon/Tue",
                new AddCommand(expected));
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
    public void parse_repeatedOptionalField_failure() {
        for (Prefix prefix : new Prefix[] {CliSyntax.PREFIX_ADDRESS, CliSyntax.PREFIX_REMARK}) {
            assertParseFailure(parser, validArgs + " " + prefix + "first " + prefix + "second",
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
    public void parse_allFieldsWithMultipleTags_success() throws Exception {
        Person expected = new PersonBuilder().withName("Avery Tan").withAgeCategory("Under 14")
                .withPhone("91234567").withEmail("avery.tan@example.com")
                .withAddress("123 Main Street").withRemark("Prefers morning training")
                .withTags("sprints", "relay").build();
        String args = validArgs + " addr/123 Main Street r/Prefers morning training t/sprints t/relay t/sprints";
        assertParseSuccess(parser, args, new AddCommand(expected));
        assertParseSuccess(parser, " t/relay r/Prefers morning training addr/123 Main Street"
                + validArgs + " t/sprints", new AddCommand(expected));

        // Person equality does not compare remarks, so verify the stored value explicitly.
        Model model = new ModelManager();
        parser.parse(args).execute(model);
        Person added = model.getAddressBook().getPersonList().get(0);
        assertEquals(expected.getRemark(), added.getRemark());
        assertEquals(expected.getTags(), added.getTags());
        assertEquals(expected.getAddress(), added.getAddress());
    }

    @Test
    public void parse_optionalFieldsIndependently_success() {
        Person base = new PersonBuilder().withName("Avery Tan").withAgeCategory("Under 14")
                .withPhone("91234567").withEmail("avery.tan@example.com")
                .withAddress("").withRemark("").withTags().build();
        assertParseSuccess(parser, validArgs + " addr/123 Main Street",
                new AddCommand(new PersonBuilder(base).withAddress("123 Main Street").build()));
        assertParseSuccess(parser, validArgs + " r/", new AddCommand(base));
        assertParseSuccess(parser, validArgs + " t/sprints",
                new AddCommand(new PersonBuilder(base).withTags("sprints").build()));
    }

    @Test
    public void parse_invalidOptionalValue_failure() {
        assertParseFailure(parser, validArgs + " addr/", Address.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, validArgs + " addr/   ", Address.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, validArgs + " t/", Tag.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, validArgs + " t/sprints t/invalid-tag", Tag.MESSAGE_CONSTRAINTS);
    }
}
