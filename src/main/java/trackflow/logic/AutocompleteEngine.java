// @@author antonydchakola-hub
package trackflow.logic;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// @@author
import trackflow.logic.parser.ArgumentMultimap;
import trackflow.logic.parser.ArgumentTokenizer;
import trackflow.logic.parser.Prefix;

/**
 * Handles the logic for the command box autocomplete feature.
 */
public class AutocompleteEngine {

    private static final List<String> COMMAND_TEMPLATES = List.of(
            "add n/ a/ p/ e/ addr/",
            "clear",
            "delete INDEX",
            "edit INDEX n/ a/ p/ e/ addr/",
            "exit",
            "filter a/",
            "find ",
            "sort",
            "help",
            "list",
            "remark INDEX r/",
            "tag INDEX t/",
            "untag INDEX t/"
    );

    /**
     * Returns a list of autocomplete suggestions based on the current input text.
     * Only suggests command templates when typing the initial command word.
     *
     * @param inputText The current text in the command box.
     * @return A list of suggested command strings.
     */
    public List<String> getSuggestions(String inputText) {
        if (inputText == null) {
            return List.of();
        }

        String trimmed = inputText.stripLeading();
        if (trimmed.isEmpty() || trimmed.chars().anyMatch(Character::isWhitespace)) {
            return List.of();
        }

        String query = trimmed.toLowerCase(Locale.ROOT);
        return COMMAND_TEMPLATES.stream()
                .filter(template -> template.toLowerCase(Locale.ROOT).startsWith(query))
                .toList();
    }

    /**
     * Returns the next missing prefix for the command in the given text.
     * Prefix matching is case-insensitive. Commands needing an index receive prefixes only after a numeric index.
     *
     * @param currentText The current text in the command box.
     * @return The next prefix to append (e.g., " p/"), or an empty string if none.
     */
    public String getNextPrefix(String currentText) {
        if (currentText == null || currentText.trim().isEmpty()) {
            return "";
        }
        String[] parts = currentText.trim().split("\\s+");
        String commandWord = parts[0].toLowerCase(Locale.ROOT);

        String template = COMMAND_TEMPLATES.stream()
                .filter(t -> t.toLowerCase(Locale.ROOT).startsWith(commandWord + " ")
                        || t.equalsIgnoreCase(commandWord))
                .findFirst()
                .orElse(null);

        if (template == null) {
            return "";
        }
        if (template.contains(" INDEX") && (parts.length < 2 || !parts[1].matches("[0-9]+"))) {
            return "";
        }

        Matcher prefixMatcher = Pattern.compile("(?:^|\\s)([a-zA-Z]+/)").matcher(template);
        List<Prefix> prefixes = prefixMatcher.results()
                .map(result -> new Prefix(result.group(1)))
                .toList();
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(currentText, prefixes.toArray(Prefix[]::new));
        for (Prefix prefix : prefixes) {
            if (arguments.getValue(prefix).isEmpty()) {
                return (currentText.endsWith(" ") ? "" : " ") + prefix.getPrefix();
            }
        }
        return "";
    }
}
