package trackflow.logic.parser;

import static trackflow.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static trackflow.logic.parser.CommandParserTestUtil.assertParseFailure;
import static trackflow.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import trackflow.logic.Messages;
import trackflow.logic.commands.AddCommand;
import trackflow.model.person.Address;
import trackflow.model.person.AgeCategory;
import trackflow.model.person.Email;
import trackflow.model.person.Name;
import trackflow.model.person.Person;
import trackflow.model.person.Phone;
import trackflow.model.tag.Tag;
import trackflow.testutil.PersonBuilder;

public class AddCommandParserTest {
    private final AddCommandParser parser = new AddCommandParser();
    private final String validArgs = " n/Avery Tan a/Under 14 p/91234567 e/avery.tan@example.com"
            + " addr/123 Main Street";

    @Test
    public void parse_allFieldsPresent_success() {
        Person expected = new PersonBuilder().withName("Avery Tan").withAgeCategory("Under 14")
                .withPhone("91234567").withEmail("avery.tan@example.com")
                .withAddress("123 Main Street").withRemark("").withTags().build();
        assertParseSuccess(parser, validArgs, new AddCommand(expected));
        assertParseSuccess(parser, "  e/avery.tan@example.com p/91234567 a/under   14"
                + " addr/123 Main Street n/Avery Tan  ",
                new AddCommand(expected));
        assertParseSuccess(parser, "  E/avery.tan@example.com P/91234567 A/under 14"
                + " ADDR/123 Main Street N/Avery Tan  ",
                new AddCommand(expected));
    }

    @Test
    public void parse_addressInAnyOrder_success() {
        Person expected = new PersonBuilder().withName("Avery Tan").withAgeCategory("Under 14")
                .withPhone("91234567").withEmail("avery.tan@example.com")
                .withAddress("123 Main Street").withRemark("").withTags().build();
        assertParseSuccess(parser, validArgs, new AddCommand(expected));
        assertParseSuccess(parser, " addr/123 Main Street" + validArgs.replace(" addr/123 Main Street", ""),
                new AddCommand(expected));
    }

    @Test
    public void parse_invalidOrRepeatedAddress_failure() {
        assertParseFailure(parser, validArgs.replace("addr/123 Main Street", "addr/"), Address.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, validArgs.replace("addr/123 Main Street", "addr/   "), Address.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, validArgs + " addr/456 Other Street",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_ADDRESS));
    }

    @Test
    public void parse_eachAgeCategory_success() {
        for (String category : new String[] {"Under 14", "Under 16", "Under 18", "Under 20", "Open"}) {
            Person expected = new PersonBuilder().withName("Avery Tan").withAgeCategory(category)
                    .withPhone("91234567").withEmail("avery.tan@example.com")
                    .withAddress("123 Main Street").withRemark("").withTags().build();
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
        for (String field : new String[] {" n/Avery Tan", " a/Under 14", " p/91234567", " e/avery.tan@example.com",
            " addr/123 Main Street"}) {
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
                .withAddress("Level Two/Three").withRemark("").withTags().build();

        assertParseSuccess(parser, validArgs.replace("123 Main Street", "Level Two/Three"), new AddCommand(expected));
    }

    @Test
    public void parse_repeatedField_failure() {
        for (Prefix prefix : new Prefix[] {CliSyntax.PREFIX_NAME, CliSyntax.PREFIX_AGE_CATEGORY,
            CliSyntax.PREFIX_PHONE, CliSyntax.PREFIX_EMAIL, CliSyntax.PREFIX_ADDRESS}) {
            assertParseFailure(parser, validArgs + " " + prefix + "invalid",
                    Messages.getErrorMessageForDuplicatePrefixes(prefix));
        }
        assertParseFailure(parser, validArgs + " r/first r/second",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_REMARK));
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
        assertParseFailure(parser, validArgs + " t/", Tag.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, validArgs + " t/100m*", Tag.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_optionalTagsAndRemark_success() {
        // single tag
        Person withSingleTag = new PersonBuilder().withName("Avery Tan").withAgeCategory("Under 14")
                .withPhone("91234567").withEmail("avery.tan@example.com")
                .withAddress("123 Main Street").withRemark("").withTags("100m").build();
        assertParseSuccess(parser, validArgs + " t/100m", new AddCommand(withSingleTag));

        // multiple tags in any position
        Person withTags = new PersonBuilder().withName("Avery Tan").withAgeCategory("Under 14")
                .withPhone("91234567").withEmail("avery.tan@example.com")
                .withAddress("123 Main Street").withRemark("").withTags("100m", "200m Relay").build();
        assertParseSuccess(parser, validArgs + " t/100m t/200m   Relay", new AddCommand(withTags));
        assertParseSuccess(parser, " t/100m t/200m Relay" + validArgs, new AddCommand(withTags));

        // repeated tag with different case is deduplicated
        assertParseSuccess(parser, validArgs + " t/100m t/100M", new AddCommand(withSingleTag));

        // remark present
        Person withRemark = new PersonBuilder().withName("Avery Tan").withAgeCategory("Under 14")
                .withPhone("91234567").withEmail("avery.tan@example.com")
                .withAddress("123 Main Street").withRemark("Team captain").withTags().build();
        assertParseSuccess(parser, validArgs + " r/Team captain", new AddCommand(withRemark));

        // empty remark
        Person withEmptyRemark = new PersonBuilder().withName("Avery Tan").withAgeCategory("Under 14")
                .withPhone("91234567").withEmail("avery.tan@example.com")
                .withAddress("123 Main Street").withRemark("").withTags().build();
        assertParseSuccess(parser, validArgs + " r/", new AddCommand(withEmptyRemark));

        // both multiple tags and remark
        Person withBoth = new PersonBuilder().withName("Avery Tan").withAgeCategory("Under 14")
                .withPhone("91234567").withEmail("avery.tan@example.com")
                .withAddress("123 Main Street").withRemark("Needs blocks").withTags("100m", "Long Jump").build();
        assertParseSuccess(parser, validArgs + " t/100m r/Needs blocks t/Long Jump", new AddCommand(withBoth));
    }
}
