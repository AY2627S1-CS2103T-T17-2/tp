package trackflow.logic;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Handles the logic for the command box autocomplete feature.
 */
public class AutocompleteEngine {

    private final List<String> commandTemplates = List.of(
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
            "remark INDEX r/"
    );

    /**
     * Constructs an {@code AutocompleteEngine}.
     */
    public AutocompleteEngine() {
    }

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
        if (trimmed.isEmpty() || trimmed.contains(" ") || trimmed.contains("\t")) {
            return List.of();
        }

        String query = trimmed.toLowerCase(Locale.ROOT);
        return commandTemplates.stream()
                .filter(template -> template.toLowerCase(Locale.ROOT).startsWith(query))
                .collect(Collectors.toList());
    }

    /**
     * Returns the next missing prefix for the command in the given text.
     * Prefix matching is case-insensitive.
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

        String template = commandTemplates.stream()
                .filter(t -> t.toLowerCase(Locale.ROOT).startsWith(commandWord + " ")
                        || t.equalsIgnoreCase(commandWord))
                .findFirst()
                .orElse(null);

        if (template == null) {
            return "";
        }

        Matcher m = Pattern.compile("(?:^|\\s)([a-zA-Z]+/)").matcher(template);
        while (m.find()) {
            String prefix = m.group(1);
            // Check case-insensitively if the current text already contains this prefix preceded by whitespace
            boolean isAlreadyPresent = Pattern.compile("(?i)(?:^|\\s)" + Pattern.quote(prefix))
                    .matcher(currentText)
                    .find();
            if (!isAlreadyPresent) {
                return (currentText.endsWith(" ") ? "" : " ") + prefix;
            }
        }
        return "";
    }
}
