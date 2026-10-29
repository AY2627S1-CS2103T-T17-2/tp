// @@author antonydchakola-hub
package trackflow.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import trackflow.logic.commands.EditCommand;
import trackflow.logic.parser.TrackFlowParser;

public class AutocompleteEngineTest {

    private AutocompleteEngine autocompleteEngine;

    @BeforeEach
    public void setUp() {
        autocompleteEngine = new AutocompleteEngine();
    }

    // ========================================
    // Tests for getSuggestions
    // ========================================

    @Test
    public void getSuggestions_nullInput_returnsEmptyList() {
        List<String> suggestions = autocompleteEngine.getSuggestions(null);
        assertTrue(suggestions.isEmpty());
    }

    @Test
    // @@author
    public void getSuggestions_emptyOrWhitespaceOnly_returnsEmptyList() {
        for (String input : List.of("", "   ", "\t")) {
            assertTrue(autocompleteEngine.getSuggestions(input).isEmpty());
        }
    }

    @Test
    public void getSuggestions_partialCommandWord_returnsMatchingSuggestions() {
        List<String> suggestions = autocompleteEngine.getSuggestions("a");
        assertEquals(List.of("add n/ a/ p/ e/ addr/"), suggestions);

        suggestions = autocompleteEngine.getSuggestions("d");
        assertEquals(List.of("delete INDEX"), suggestions);

        suggestions = autocompleteEngine.getSuggestions("ed");
        assertEquals(List.of("edit INDEX n/ a/ p/ e/ addr/"), suggestions);

        suggestions = autocompleteEngine.getSuggestions("r");
        assertEquals(List.of("remark INDEX r/"), suggestions);

        suggestions = autocompleteEngine.getSuggestions("ta");
        assertEquals(List.of("tag INDEX t/"), suggestions);

        // sort shows its supported fields and orders
        suggestions = autocompleteEngine.getSuggestions("so");
        assertEquals(List.of("sort name|age|phone|email|address [asc|desc]"), suggestions);
        suggestions = autocompleteEngine.getSuggestions("sort");
        assertEquals(List.of("sort name|age|phone|email|address [asc|desc]"), suggestions);

        suggestions = autocompleteEngine.getSuggestions("u");
        assertEquals(List.of("untag INDEX t/"), suggestions);
    }

    @Test
    public void getNextPrefix_tagCommands_suggestsEventPrefix() {
        assertEquals(" t/", autocompleteEngine.getNextPrefix("tag 1"));
        assertEquals("t/", autocompleteEngine.getNextPrefix("untag 1 "));
        assertEquals("", autocompleteEngine.getNextPrefix("tag 1 t/100m"));
    }

    @Test
    public void getSuggestions_multipleMatches_returnsAllMatches() {
        List<String> suggestions = autocompleteEngine.getSuggestions("f");
        assertEquals(List.of("filter a/", "find "), suggestions);
    }

    @Test
    public void getSuggestions_caseInsensitive_returnsMatchingSuggestions() {
        List<String> uppercaseSuggestions = autocompleteEngine.getSuggestions("ADD");
        assertEquals(List.of("add n/ a/ p/ e/ addr/"), uppercaseSuggestions);

        List<String> mixedCaseSuggestions = autocompleteEngine.getSuggestions("ClEaR");
        assertEquals(List.of("clear"), mixedCaseSuggestions);
    }

    @Test
    public void getSuggestions_leadingWhitespace_returnsMatchingSuggestions() {
        List<String> suggestions = autocompleteEngine.getSuggestions("   add");
        assertEquals(List.of("add n/ a/ p/ e/ addr/"), suggestions);
    }

    @Test
    public void getSuggestions_unicodeWhitespace_distinguishesLeadingAndTrailingSeparators() {
        for (String separator : List.of("\u00a0", "\u2003", "\u2007", "\u202f", "\u3000")) {
            assertEquals(List.of("edit INDEX n/ a/ p/ e/ addr/"),
                    autocompleteEngine.getSuggestions(separator + "ed"));
            assertTrue(autocompleteEngine.getSuggestions("edit" + separator).isEmpty());
            assertTrue(autocompleteEngine.getSuggestions(separator).isEmpty());
        }
    }

    @Test
    public void getNextPrefix_unicodeWhitespace_agreesWithCommandParser() throws Exception {
        TrackFlowParser parser = new TrackFlowParser();
        for (String separator : List.of("\u00a0", "\u2003", "\u2007", "\u202f", "\u3000")) {
            String command = separator + "edit" + separator + "1";
            assertEquals(" n/", autocompleteEngine.getNextPrefix(command));
            assertEquals("n/", autocompleteEngine.getNextPrefix(command + separator));
            assertEquals(" a/", autocompleteEngine.getNextPrefix(command + separator + "n/Avery"));
            assertEquals(EditCommand.class, parser.parseCommand(command + separator + "n/Avery").getClass());
        }
    }

    @Test
    public void getSuggestions_trailingWhitespace_returnsEmptyList() {
        // Issue 1A fix: Once the user enters a trailing space after command word, do not suggest command templates
        List<String> suggestions = autocompleteEngine.getSuggestions("add ");
        assertTrue(suggestions.isEmpty());

        suggestions = autocompleteEngine.getSuggestions("add\t");
        assertTrue(suggestions.isEmpty());

        // other whitespace after the command word also ends command-word suggestions
        assertTrue(autocompleteEngine.getSuggestions("add\n").isEmpty());
        assertTrue(autocompleteEngine.getSuggestions("add ").isEmpty()); // em space

        suggestions = autocompleteEngine.getSuggestions("clear ");
        assertTrue(suggestions.isEmpty());
    }

    @Test
    public void getSuggestions_withArguments_returnsEmptyList() {
        List<String> suggestions = autocompleteEngine.getSuggestions("add n/John");
        assertTrue(suggestions.isEmpty());

        suggestions = autocompleteEngine.getSuggestions("filter a/Under 20");
        assertTrue(suggestions.isEmpty());
    }

    @Test
    public void getSuggestions_unknownCommand_returnsEmptyList() {
        List<String> suggestions = autocompleteEngine.getSuggestions("xyz");
        assertTrue(suggestions.isEmpty());
    }

    // ========================================
    // Tests for getNextPrefix
    // ========================================

    @Test
    public void getNextPrefix_nullOrEmptyInput_returnsEmptyString() {
        assertEquals("", autocompleteEngine.getNextPrefix(null));
        assertEquals("", autocompleteEngine.getNextPrefix(""));
        assertEquals("", autocompleteEngine.getNextPrefix("   "));
    }

    @Test
    public void getNextPrefix_unknownCommand_returnsEmptyString() {
        assertEquals("", autocompleteEngine.getNextPrefix("unknown"));
        assertEquals("", autocompleteEngine.getNextPrefix("unknown n/John"));
    }

    @Test
    public void getNextPrefix_commandWithoutPrefixes_returnsEmptyString() {
        assertEquals("", autocompleteEngine.getNextPrefix("clear"));
        assertEquals("", autocompleteEngine.getNextPrefix("list"));
        assertEquals("", autocompleteEngine.getNextPrefix("exit"));
        assertEquals("", autocompleteEngine.getNextPrefix("delete"));
        assertEquals("", autocompleteEngine.getNextPrefix("find"));
        assertEquals("", autocompleteEngine.getNextPrefix("sort"));
        assertEquals("", autocompleteEngine.getNextPrefix("sort name ")); // sort arguments are not prefixes
        assertEquals("", autocompleteEngine.getNextPrefix("help"));
    }

    @Test
    public void getNextPrefix_addCommandWithAndWithoutTrailingSpace_returnsNextMissingPrefix() {
        assertEquals(" n/", autocompleteEngine.getNextPrefix("add"));
        assertEquals("n/", autocompleteEngine.getNextPrefix("add "));
        assertEquals("n/", autocompleteEngine.getNextPrefix("ADD "));
    }

    @Test
    public void getNextPrefix_addCommandSequentialPrefixes_returnsNextMissingPrefix() {
        // Starting with name prefix
        assertEquals(" a/", autocompleteEngine.getNextPrefix("add n/John"));
        assertEquals("a/", autocompleteEngine.getNextPrefix("add n/John "));

        // With name and age
        assertEquals(" p/", autocompleteEngine.getNextPrefix("add n/John a/Under 20"));

        // With name, age, phone
        assertEquals(" e/", autocompleteEngine.getNextPrefix("add n/John a/Under 20 p/91234567"));

        // With name, age, phone, email
        assertEquals(" addr/", autocompleteEngine.getNextPrefix("add n/John a/Under 20 p/91234567 e/j@e.com"));

        // All prefixes present
        assertEquals("", autocompleteEngine.getNextPrefix("add n/John a/Under 20 p/91234567 e/j@e.com addr/NUS"));
    }

    @Test
    public void getNextPrefix_uppercaseCommandAndPrefixes_matchesCaseInsensitively() {
        // Issue 1B fix: Uppercase prefixes should be recognized so that already entered prefixes are not repeated
        assertEquals("n/", autocompleteEngine.getNextPrefix("EDIT 1 "));
        assertEquals(" a/", autocompleteEngine.getNextPrefix("add N/John"));
        assertEquals(" p/", autocompleteEngine.getNextPrefix("add N/John A/Under 20"));
        assertEquals(" e/", autocompleteEngine.getNextPrefix("add n/John a/Under 20 P/91234567"));
        assertEquals(" addr/", autocompleteEngine.getNextPrefix("add n/John a/Under 20 p/91234567 E/j@e.com"));
        assertEquals("", autocompleteEngine.getNextPrefix("add N/John A/Under 20 P/91234567 E/j@e.com ADDR/NUS"));
    }

    @Test
    public void getNextPrefix_escapedPrefixes_notCountedAsParameters() {
        assertEquals(" n/", autocompleteEngine.getNextPrefix("add n//literal"));
        assertEquals(" r/", autocompleteEngine.getNextPrefix("remark 1 r//literal"));
        assertEquals(" a/", autocompleteEngine.getNextPrefix("filter a//Open"));
        assertEquals(" e/", autocompleteEngine.getNextPrefix("add n/Alice a/Open p/91234567 addr/Block A//B"));
    }

    @Test
    public void getNextPrefix_outOfOrderPrefixes_returnsFirstMissingTemplatePrefix() {
        // Age is present, name is missing
        assertEquals(" n/", autocompleteEngine.getNextPrefix("add a/Under 20"));

        // Name and address are present, age is missing
        assertEquals(" a/", autocompleteEngine.getNextPrefix("add n/John addr/NUS"));

        // Name, age, email present, phone is missing
        assertEquals(" p/", autocompleteEngine.getNextPrefix("add n/John a/Under 20 e/j@e.com"));
    }

    @Test
    public void getNextPrefix_editCommand_returnsPrefixes() {
        assertEquals(" n/", autocompleteEngine.getNextPrefix("edit 1"));
        assertEquals(" a/", autocompleteEngine.getNextPrefix("edit 1 n/Alex"));
        assertEquals("", autocompleteEngine.getNextPrefix("edit 1 n/Alex a/Under 20 p/91234567 e/a@b.com addr/NUS"));
    }

    @Test
    public void getNextPrefix_missingOrNonNumericIndex_doesNotInsertPrefixes() {
        for (String command : List.of("edit", "remark", "tag", "untag")) {
            assertEquals("", autocompleteEngine.getNextPrefix(command));
            assertEquals("", autocompleteEngine.getNextPrefix(command + " "));
            assertEquals("", autocompleteEngine.getNextPrefix(command + " INDEX"));
            assertEquals("", autocompleteEngine.getNextPrefix(command + " n/Avery"));
        }
        assertEquals(" n/", autocompleteEngine.getNextPrefix("EDIT 0001"));
    }

    @Test
    public void getNextPrefix_filterCommand_returnsPrefix() {
        assertEquals("a/", autocompleteEngine.getNextPrefix("filter "));
        assertEquals(" a/", autocompleteEngine.getNextPrefix("filter"));
        assertEquals("", autocompleteEngine.getNextPrefix("filter a/Under 20"));
        assertEquals("", autocompleteEngine.getNextPrefix("filter A/Under 20"));
    }

    @Test
    public void getNextPrefix_remarkCommand_returnsPrefix() {
        assertEquals("r/", autocompleteEngine.getNextPrefix("remark 1 "));
        assertEquals(" r/", autocompleteEngine.getNextPrefix("remark 1"));
        assertEquals("", autocompleteEngine.getNextPrefix("remark 1 r/Likes coffee"));
        assertEquals("", autocompleteEngine.getNextPrefix("remark 1 R/Likes coffee"));
    }
}
