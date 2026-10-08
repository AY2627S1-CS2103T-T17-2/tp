package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_EMAIL_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_NAME_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_PHONE_DESC;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_AMY;
import static seedu.address.logic.parser.CliSyntax.PREFIX_AGE_CATEGORY;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.EditCommand;
import seedu.address.model.person.AgeCategory;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.testutil.EditPersonDescriptorBuilder;

public class EditCommandParserTest {

    private static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditCommand.MESSAGE_USAGE);

    private EditCommandParser parser = new EditCommandParser();

    @Test
    public void parse_missingParts_failure() {
        assertParseFailure(parser, VALID_NAME_AMY, MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "1", EditCommand.MESSAGE_NOT_EDITED);
        assertParseFailure(parser, "", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_invalidPreamble_failure() {
        for (String preamble : List.of("-5", "0", "abc", "2147483648", "1 some random string", "1 i/string")) {
            assertParseFailure(parser, preamble + NAME_DESC_AMY, MESSAGE_INVALID_FORMAT);
        }
    }

    @Test
    public void parse_invalidValue_failure() {
        assertParseFailure(parser, "1" + INVALID_NAME_DESC, Name.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1" + INVALID_PHONE_DESC, Phone.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1" + INVALID_EMAIL_DESC, Email.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 n/", Name.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 p/", Phone.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 e/", Email.MESSAGE_CONSTRAINTS);
        for (String category : List.of("", "Under 15", "123 Main Street")) {
            assertParseFailure(parser, "1 a/" + category, AgeCategory.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_allFieldsSpecified_success() {
        String input = "1" + PHONE_DESC_AMY + " a/Under 16" + EMAIL_DESC_AMY + NAME_DESC_AMY;
        EditCommand expected = new EditCommand(INDEX_FIRST_PERSON, new EditPersonDescriptorBuilder()
                .withName(VALID_NAME_AMY).withAgeCategory("Under 16")
                .withPhone(VALID_PHONE_AMY).withEmail(VALID_EMAIL_AMY).build());
        assertParseSuccess(parser, input, expected);
    }

    @Test
    public void parse_someFieldsSpecified_success() {
        EditCommand expected = new EditCommand(INDEX_FIRST_PERSON, new EditPersonDescriptorBuilder()
                .withPhone(VALID_PHONE_AMY).withEmail(VALID_EMAIL_AMY).build());
        assertParseSuccess(parser, "1" + PHONE_DESC_AMY + EMAIL_DESC_AMY, expected);
    }

    @Test
    public void parse_oneFieldSpecified_success() {
        assertParseSuccess(parser, "1" + NAME_DESC_AMY, new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withName(VALID_NAME_AMY).build()));
        assertParseSuccess(parser, "1" + PHONE_DESC_AMY, new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_AMY).build()));
        assertParseSuccess(parser, "1" + EMAIL_DESC_AMY, new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withEmail(VALID_EMAIL_AMY).build()));
        for (String category : List.of("Under 14", "Under 16", "Under 18", "Under 20", "Open")) {
            EditCommand expected = new EditCommand(INDEX_FIRST_PERSON,
                    new EditPersonDescriptorBuilder().withAgeCategory(category).build());
            assertParseSuccess(parser, "1 a/" + category, expected);
            assertParseSuccess(parser, "1 a/  " + category.toLowerCase(java.util.Locale.ROOT)
                    .replace(" ", "   ") + "  ", expected);
        }
    }

    @Test
    public void parse_repeatedFields_failure() {
        for (Prefix prefix : List.of(PREFIX_NAME, PREFIX_AGE_CATEGORY, PREFIX_PHONE, PREFIX_EMAIL)) {
            assertParseFailure(parser, "1 " + prefix + " " + prefix + "invalid",
                    Messages.getErrorMessageForDuplicatePrefixes(prefix));
        }
        assertParseFailure(parser, "1 n/Amy n/Bob a/Open a/Under 16 p/123 p/456 e/a@b.com e/b@b.com",
                Messages.getErrorMessageForDuplicatePrefixes(
                        PREFIX_NAME, PREFIX_AGE_CATEGORY, PREFIX_PHONE, PREFIX_EMAIL));
    }

    @Test
    public void parse_unsupportedFields_failure() {
        for (String unsupported : List.of("t/", "t/friend", "r/", "r/fast runner")) {
            assertParseFailure(parser, "1 " + unsupported, MESSAGE_INVALID_FORMAT);
            assertParseFailure(parser, "1 n/Amy " + unsupported, MESSAGE_INVALID_FORMAT);
            assertParseFailure(parser, "1 " + unsupported + " a/Open", MESSAGE_INVALID_FORMAT);
        }
    }
}
