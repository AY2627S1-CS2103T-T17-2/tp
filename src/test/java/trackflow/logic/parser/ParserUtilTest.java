package trackflow.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static trackflow.testutil.Assert.assertThrows;
import static trackflow.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.math.BigInteger;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.junit.jupiter.api.Test;

import trackflow.commons.core.index.Index;
import trackflow.logic.parser.exceptions.ParseException;
import trackflow.model.person.Address;
import trackflow.model.person.AgeCategory;
import trackflow.model.person.Email;
import trackflow.model.person.Name;
import trackflow.model.person.Phone;
import trackflow.model.tag.Tag;

public class ParserUtilTest {
    private static final String INVALID_NAME = "R@chel";
    private static final String INVALID_PHONE = "911a";
    private static final String INVALID_ADDRESS = " ";
    private static final String INVALID_EMAIL = "example.com";
    private static final String INVALID_AGE_CATEGORY = "Under 15";
    private static final String INVALID_TAG = "#friend";

    private static final String VALID_NAME = "Rachel Walker";
    private static final String VALID_PHONE = "123456";
    private static final String VALID_ADDRESS = "123 Main Street #0505";
    private static final String VALID_EMAIL = "rachel@example.com";
    private static final String VALID_AGE_CATEGORY = "Under 16";
    private static final String VALID_TAG_1 = "friend";
    private static final String VALID_TAG_2 = "neighbour";

    private static final String WHITESPACE = " \t\r\n";

    @Test
    public void parseAgeCategory_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseAgeCategory(null));
    }

    @Test
    public void parseAgeCategory_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, AgeCategory.MESSAGE_CONSTRAINTS, () ->
                ParserUtil.parseAgeCategory(INVALID_AGE_CATEGORY));
    }

    @Test
    public void parseAgeCategory_validValue_returnsNormalizedAgeCategory() throws Exception {
        AgeCategory expected = new AgeCategory(VALID_AGE_CATEGORY);
        assertEquals(expected, ParserUtil.parseAgeCategory(VALID_AGE_CATEGORY));
        assertEquals(expected, ParserUtil.parseAgeCategory(WHITESPACE + "under   16" + WHITESPACE));
    }

    @Test
    public void parseIndex_invalidInput_throwsParseException() {
        assertThrows(ParseException.class, ParserUtil.MESSAGE_INVALID_INDEX, () -> ParserUtil.parseIndex("10 a"));
    }

    @Test
    public void parseIndex_validInput_success() throws Exception {
        // No whitespaces
        assertEquals(INDEX_FIRST_PERSON, ParserUtil.parseIndex("1"));

        // Leading and trailing whitespaces
        assertEquals(INDEX_FIRST_PERSON, ParserUtil.parseIndex("  1  "));
        assertEquals(INDEX_FIRST_PERSON, ParserUtil.parseIndex("0001"));
    }

    @Test
    public void parseIndex_extremeDigitSequences_validatesEntireInput() throws Exception {
        assertEquals(INDEX_FIRST_PERSON, ParserUtil.parseIndex("0".repeat(10000) + "1"));
        assertEquals(Index.fromOneBased(Integer.MAX_VALUE), ParserUtil.parseIndex("9".repeat(10000)));
        assertEquals(Index.fromOneBased(Integer.MAX_VALUE), ParserUtil.parseIndex("2147483648"));
        assertEquals(Index.fromOneBased(Integer.MAX_VALUE), ParserUtil.parseIndex("2147483647"));
        for (String invalid : new String[] {"0".repeat(10000), "9".repeat(10000) + "x", "-1", "+1", "1.0"}) {
            assertThrows(ParseException.class, ParserUtil.MESSAGE_INVALID_INDEX, () ->
                    ParserUtil.parseIndex(invalid));
        }
    }

    @Test
    public void parseIndex_randomDigitSequences_matchesBoundedBigInteger() throws Exception {
        Random random = new Random(2103);
        BigInteger maximumIndex = BigInteger.valueOf(Integer.MAX_VALUE);
        for (int sample = 0; sample < 500; sample++) {
            StringBuilder digits = new StringBuilder();
            for (int digit = 0, length = 1 + random.nextInt(100); digit < length; digit++) {
                digits.append(random.nextInt(10));
            }
            String input = digits.toString();
            BigInteger number = new BigInteger(input);
            if (number.signum() == 0) {
                assertThrows(ParseException.class, () -> ParserUtil.parseIndex(input));
            } else {
                assertEquals(number.min(maximumIndex).intValue(), ParserUtil.parseIndex(input).getOneBased());
            }
            assertThrows(ParseException.class, () -> ParserUtil.parseIndex(input + "x"));
        }
    }

    @Test
    public void verifyNoUnknownPrefixes_realAndEscapedTokens_useSameBoundaries() throws Exception {
        for (String boundary : List.of("", " ", "\t", "\r\n", "\f", "\u2003")) {
            for (char letter = 'a'; letter <= 'z'; letter++) {
                String token = String.valueOf(letter);
                String unknown = boundary + token + "/value";
                assertThrows(ParseException.class, () ->
                        ParserUtil.verifyNoUnknownPrefixes(unknown, "usage"));
                ParserUtil.verifyNoUnknownPrefixes(unknown, "usage", new Prefix(token + "/"));
                ParserUtil.verifyNoUnknownPrefixes(boundary + token + "//value", "usage");
                ParserUtil.verifyNoUnknownPrefixes(boundary + token + "////value", "usage");
            }
            assertThrows(ParseException.class, () ->
                    ParserUtil.verifyNoUnknownPrefixes(boundary + "AdDr/value", "usage"));
            ParserUtil.verifyNoUnknownPrefixes(boundary + "AdDr/value", "usage", new Prefix("addr/"));
            ParserUtil.verifyNoUnknownPrefixes(boundary + "AdDr//value", "usage");
        }
    }

    @Test
    public void verifyNoUnknownPrefixes_escapedTokenBeforeRealUnknown_stillRejectsUnknown() {
        for (String unknown : List.of("x/", "X/value", "addr/value", "r/value")) {
            assertThrows(ParseException.class, () ->
                    ParserUtil.verifyNoUnknownPrefixes("x//literal " + unknown, "usage"));
        }
    }

    @Test
    public void verifyNoUnknownPrefixes_urlsPathsAndPunctuation_areOrdinaryText() throws Exception {
        for (String text : List.of("https://example.com/n/value?p//other", "file:///home/coach",
                "x://host/path", "//server/share", "C:\\Training", "Mon/Tue", "wrong/value",
                "(x/value)", "\"x/value\"", "'x/value'", "\\x/value", "\u00a0x/value", "\u200bx/value")) {
            ParserUtil.verifyNoUnknownPrefixes(text, "usage");
        }
        // Quotes are ordinary characters, so whitespace inside them still introduces a parameter.
        assertThrows(ParseException.class, () ->
                ParserUtil.verifyNoUnknownPrefixes("\"some text x/value\"", "usage"));
    }

    @Test
    public void parseIndex_nullAndNonAsciiDigits_areNotValidIndexes() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseIndex(null));
        for (String input : List.of("", "null", "NaN", "Infinity", "\u0661", "\uff11", "1\u0000", "1\u200b",
                "1 0", "1\t0", "9".repeat(10000) + "\uff11")) {
            assertThrows(ParseException.class, ParserUtil.MESSAGE_INVALID_INDEX, () ->
                    ParserUtil.parseIndex(input));
        }
    }

    @Test
    public void parseName_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseName((String) null));
    }

    @Test
    public void parseName_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, Name.MESSAGE_CONSTRAINTS, () -> ParserUtil.parseName(INVALID_NAME));
    }

    @Test
    public void parseName_validValueWithoutWhitespace_returnsName() throws Exception {
        Name expectedName = new Name(VALID_NAME);
        assertEquals(expectedName, ParserUtil.parseName(VALID_NAME));
    }

    @Test
    public void parseName_validValueWithWhitespace_returnsTrimmedName() throws Exception {
        String nameWithWhitespace = WHITESPACE + VALID_NAME + WHITESPACE;
        Name expectedName = new Name(VALID_NAME);
        assertEquals(expectedName, ParserUtil.parseName(nameWithWhitespace));
    }

    @Test
    public void parsePhone_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parsePhone((String) null));
    }

    @Test
    public void parsePhone_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, Phone.MESSAGE_CONSTRAINTS, () -> ParserUtil.parsePhone(INVALID_PHONE));
    }

    @Test
    public void parsePhone_validValueWithoutWhitespace_returnsPhone() throws Exception {
        Phone expectedPhone = new Phone(VALID_PHONE);
        assertEquals(expectedPhone, ParserUtil.parsePhone(VALID_PHONE));
    }

    @Test
    public void parsePhone_validValueWithWhitespace_returnsTrimmedPhone() throws Exception {
        String phoneWithWhitespace = WHITESPACE + VALID_PHONE + WHITESPACE;
        Phone expectedPhone = new Phone(VALID_PHONE);
        assertEquals(expectedPhone, ParserUtil.parsePhone(phoneWithWhitespace));
    }

    @Test
    public void parseAddress_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseAddress((String) null));
    }

    @Test
    public void parseAddress_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, Address.MESSAGE_CONSTRAINTS, () ->
                ParserUtil.parseAddress(INVALID_ADDRESS));
    }

    @Test
    public void parseAddress_validValueWithoutWhitespace_returnsAddress() throws Exception {
        Address expectedAddress = new Address(VALID_ADDRESS);
        assertEquals(expectedAddress, ParserUtil.parseAddress(VALID_ADDRESS));
    }

    @Test
    public void parseAddress_validValueWithWhitespace_returnsTrimmedAddress() throws Exception {
        String addressWithWhitespace = WHITESPACE + VALID_ADDRESS + WHITESPACE;
        Address expectedAddress = new Address(VALID_ADDRESS);
        assertEquals(expectedAddress, ParserUtil.parseAddress(addressWithWhitespace));
    }

    @Test
    public void parseEmail_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseEmail((String) null));
    }

    @Test
    public void parseEmail_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, Email.MESSAGE_CONSTRAINTS, () -> ParserUtil.parseEmail(INVALID_EMAIL));
    }

    @Test
    public void parseEmail_validValueWithoutWhitespace_returnsEmail() throws Exception {
        Email expectedEmail = new Email(VALID_EMAIL);
        assertEquals(expectedEmail, ParserUtil.parseEmail(VALID_EMAIL));
    }

    @Test
    public void parseEmail_validValueWithWhitespace_returnsTrimmedEmail() throws Exception {
        String emailWithWhitespace = WHITESPACE + VALID_EMAIL + WHITESPACE;
        Email expectedEmail = new Email(VALID_EMAIL);
        assertEquals(expectedEmail, ParserUtil.parseEmail(emailWithWhitespace));
    }

    @Test
    public void parseTag_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseTag(null));
    }

    @Test
    public void parseTag_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, Tag.MESSAGE_CONSTRAINTS, () -> ParserUtil.parseTag(INVALID_TAG));
    }

    @Test
    public void parseTag_validValueWithoutWhitespace_returnsTag() throws Exception {
        Tag expectedTag = new Tag(VALID_TAG_1);
        assertEquals(expectedTag, ParserUtil.parseTag(VALID_TAG_1));
    }

    @Test
    public void parseTag_validValueWithWhitespace_returnsTrimmedTag() throws Exception {
        String tagWithWhitespace = WHITESPACE + VALID_TAG_1 + WHITESPACE;
        Tag expectedTag = new Tag(VALID_TAG_1);
        assertEquals(expectedTag, ParserUtil.parseTag(tagWithWhitespace));
    }

    @Test
    public void parseTag_repeatedInnerWhitespace_collapsesToSingleSpace() throws Exception {
        assertEquals("Long Jump", ParserUtil.parseTag("Long  \t Jump").tagName);
    }

    @Test
    public void parseTags_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseTags(null));
    }

    @Test
    public void parseTags_collectionWithInvalidTags_throwsParseException() {
        assertThrows(ParseException.class, Tag.MESSAGE_CONSTRAINTS, () ->
                ParserUtil.parseTags(List.of(VALID_TAG_1, INVALID_TAG)));
    }

    @Test
    public void parseTags_emptyCollection_returnsEmptySet() throws Exception {
        assertTrue(ParserUtil.parseTags(List.of()).isEmpty());
    }

    @Test
    public void parseTags_collectionWithValidTags_returnsTagSet() throws Exception {
        Set<Tag> actualTagSet = ParserUtil.parseTags(List.of(VALID_TAG_1, VALID_TAG_2));
        Set<Tag> expectedTagSet = Set.of(new Tag(VALID_TAG_1), new Tag(VALID_TAG_2));

        assertEquals(expectedTagSet, actualTagSet);
    }
}
