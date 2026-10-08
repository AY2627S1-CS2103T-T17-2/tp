package seedu.address.logic;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles the logic for the command box autocomplete feature.
 */
public class AutocompleteEngine {

    private final List<String> commandTemplates = List.of(
            "add n/ a/ p/ e/ addr/",
            "clear",
            "delete ",
            "edit n/ a/ p/ e/ addr/",
            "exit",
            "filter a/",
            "find ",
            "help",
            "list",
            "remark r/"
    );

    /**
     * Constructs an {@code AutocompleteEngine}.
     */
    public AutocompleteEngine() {
    }

    /**
     * Returns a list of autocomplete suggestions based on the current input text.
     *
     * @param inputText The current text in the command box.
     * @return A list of suggested command strings.
     */
    public List<String> getSuggestions(String inputText) {
        String query = inputText == null ? "" : inputText.trim().toLowerCase();

        if (query.isEmpty()) {
            return List.of();
        }

        return commandTemplates.stream()
                .filter(template -> template.toLowerCase().startsWith(query))
                .collect(Collectors.toList());
    }

    /**
     * Returns the next missing prefix for the command in the given text.
     *
     * @param currentText The current text in the command box.
     * @return The next prefix to append (e.g., " p/"), or an empty string if none.
     */
    public String getNextPrefix(String currentText) {
        if (currentText == null || currentText.trim().isEmpty()) {
            return "";
        }
        String[] parts = currentText.trim().split("\\s+");
        String commandWord = parts[0].toLowerCase();

        String template = commandTemplates.stream()
                .filter(t -> t.toLowerCase().startsWith(commandWord + " ") || t.equalsIgnoreCase(commandWord))
                .findFirst()
                .orElse(null);

        if (template == null) {
            return "";
        }

        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(?:^|\\s)([a-zA-Z]+/)").matcher(template);
        while (m.find()) {
            String prefix = m.group(1);
            // Check if the current text already contains this prefix
            if (!currentText.contains(" " + prefix)) {
                return (currentText.endsWith(" ") ? "" : " ") + prefix;
            }
        }
        return "";
    }
}
