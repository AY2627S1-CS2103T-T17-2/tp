package trackflow.logic.parser;

import static trackflow.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static trackflow.logic.Messages.MESSAGE_UNKNOWN_PARAMETER;
import static trackflow.logic.parser.CommandParserTestUtil.assertParseFailure;
import static trackflow.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static trackflow.model.util.SampleDataUtil.getTagSet;
import static trackflow.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import trackflow.logic.commands.TagCommand;
import trackflow.model.tag.Tag;

public class TagCommandParserTest {

    private static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, TagCommand.MESSAGE_USAGE);

    private final TagCommandParser parser = new TagCommandParser();

    @Test
    public void parse_singleEvent_success() {
        assertParseSuccess(parser, "1 t/100m", new TagCommand(INDEX_FIRST_PERSON, getTagSet("100m")));
    }

    @Test
    public void parse_multipleEventsWithSpacesAndHyphens_success() {
        assertParseSuccess(parser, " 1 t/Long Jump t/110m-Hurdles t/4x100m ",
                new TagCommand(INDEX_FIRST_PERSON, getTagSet("Long Jump", "110m-Hurdles", "4x100m")));
    }

    @Test
    public void parse_nonEnglishLetters_success() {
        assertParseSuccess(parser, "1 t/Saut-Élan", new TagCommand(INDEX_FIRST_PERSON, getTagSet("Saut-Élan")));
    }

    @Test
    public void parse_extraWhitespaceInEvent_collapsed() {
        assertParseSuccess(parser, "1 t/  Long   Jump  ", new TagCommand(INDEX_FIRST_PERSON, getTagSet("Long Jump")));
    }

    @Test
    public void parse_repeatedEventDifferentCase_treatedAsOne() {
        assertParseSuccess(parser, "1 t/100m t/100M", new TagCommand(INDEX_FIRST_PERSON, getTagSet("100m")));
    }

    @Test
    public void parse_mixedCasePrefix_success() {
        assertParseSuccess(parser, "1 T/100m", new TagCommand(INDEX_FIRST_PERSON, getTagSet("100m")));
    }

    @Test
    public void parse_missingParts_failure() {
        // no arguments
        assertParseFailure(parser, "", MESSAGE_INVALID_FORMAT);

        // no index
        assertParseFailure(parser, "t/100m", MESSAGE_INVALID_FORMAT);

        // no event prefix
        assertParseFailure(parser, "1", MESSAGE_INVALID_FORMAT);

        // event without prefix
        assertParseFailure(parser, "1 100m", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_invalidIndex_failure() {
        assertParseFailure(parser, "0 t/100m", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "-1 t/100m", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "one t/100m", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_invalidEvent_failure() {
        // empty event
        assertParseFailure(parser, "1 t/", Tag.MESSAGE_CONSTRAINTS);

        // invalid symbol
        assertParseFailure(parser, "1 t/100m*", Tag.MESSAGE_CONSTRAINTS);

        // one valid and one invalid event
        assertParseFailure(parser, "1 t/100m t/", Tag.MESSAGE_CONSTRAINTS);

        // too long
        assertParseFailure(parser, "1 t/" + "a".repeat(Tag.MAX_LENGTH + 1), Tag.MESSAGE_CONSTRAINTS);

        // separators that are not a single space or a single hyphen
        assertParseFailure(parser, "1 t/Long - Jump", Tag.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 t/Long--Jump", Tag.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_unknownPrefix_failure() {
        assertParseFailure(parser, "1 t/100m n/Amy",
                String.format(MESSAGE_UNKNOWN_PARAMETER, "n/", TagCommand.MESSAGE_USAGE));
    }
}
