package trackflow.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    public void getSuggestions_emptyInput_returnsEmptyList() {
        List<String> suggestions = autocompleteEngine.getSuggestions("");
        assertTrue(suggestions.isEmpty());
    }

    @Test
    public void getSuggestions_whitespaceOnly_returnsEmptyList() {
        List<String> suggestions = autocompleteEngine.getSuggestions("   ");
        assertTrue(suggestions.isEmpty());

        suggestions = autocompleteEngine.getSuggestions("\t");
        assertTrue(suggestions.isEmpty());

        suggestions = autocompleteEngine.getSuggestions("add\t");
        assertTrue(suggestions.isEmpty());
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
    public void getSuggestions_trailingWhitespace_returnsEmptyList() {
        // Issue 1A fix: Once the user enters a trailing space after command word, do not suggest command templates
        List<String> suggestions = autocompleteEngine.getSuggestions("add ");
        assertTrue(suggestions.isEmpty());

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
        assertEquals("", autocompleteEngine.getNextPrefix("help"));
    }

    @Test
    public void getNextPrefix_addCommandWithoutTrailingSpace_returnsPrefixedWithSpace() {
        assertEquals(" n/", autocompleteEngine.getNextPrefix("add"));
    }

    @Test
    public void getNextPrefix_addCommandWithTrailingSpace_returnsPrefixWithoutLeadingSpace() {
        assertEquals("n/", autocompleteEngine.getNextPrefix("add "));
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
    public void getNextPrefix_uppercasePrefixes_matchesCaseInsensitively() {
        // Issue 1B fix: Uppercase prefixes should be recognized so that already entered prefixes are not repeated
        assertEquals(" a/", autocompleteEngine.getNextPrefix("add N/John"));
        assertEquals(" p/", autocompleteEngine.getNextPrefix("add N/John A/Under 20"));
        assertEquals(" e/", autocompleteEngine.getNextPrefix("add n/John a/Under 20 P/91234567"));
        assertEquals(" addr/", autocompleteEngine.getNextPrefix("add n/John a/Under 20 p/91234567 E/j@e.com"));
        assertEquals("", autocompleteEngine.getNextPrefix("add N/John A/Under 20 P/91234567 E/j@e.com ADDR/NUS"));
    }

    @Test
    public void getNextPrefix_outOfOrderPrefixes_returnsFirstMissingTemplatePrefix() {
        // Age is present, name is missing
        assertEquals(" n/", autocompleteEngine.getNextPrefix("add a/Under 20"));

        // Name and address are present, age is missing
        assertEquals(" a/", autocompleteEngine.getNextPrefix("add n/John addr/NUS"));

        // Name, age, email present, phone is missing
        assertEquals(" p/", autocompleteEngine.getNextPrefix("add n/John a/Under 20 e/j@e.com"));

        // Optional t/ and r/ prefixes in add do not interfere with suggesting missing required prefixes
        assertEquals(" a/", autocompleteEngine.getNextPrefix("add n/John t/100m r/Captain"));
        assertEquals("", autocompleteEngine.getNextPrefix(
                "add n/John a/Under 20 p/91234567 e/j@e.com addr/NUS t/100m r/Captain"));
    }

    @Test
    public void getNextPrefix_editCommand_returnsPrefixes() {
        assertEquals(" n/", autocompleteEngine.getNextPrefix("edit 1"));
        assertEquals(" a/", autocompleteEngine.getNextPrefix("edit 1 n/Alex"));
        assertEquals("", autocompleteEngine.getNextPrefix("edit 1 n/Alex a/Under 20 p/91234567 e/a@b.com addr/NUS"));
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
