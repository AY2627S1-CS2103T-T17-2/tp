package trackflow.logic.parser;

/**
 * Defines syntax whitespace consistently, including non-breaking spaces pasted from documents.
 * Field contents are not rewritten; callers strip boundaries or split syntax tokens explicitly.
 */
final class CommandWhitespace {

    static final String CHARACTERS = "\\p{javaWhitespace}\\p{javaSpaceChar}";
    static final String SEPARATOR_REGEX = "[" + CHARACTERS + "]+";
    static final String TOKEN_BOUNDARY_REGEX = "(?<![^" + CHARACTERS + "])";

    private CommandWhitespace() {
    }

    static boolean isWhitespace(char character) {
        return Character.isWhitespace(character) || Character.isSpaceChar(character);
    }

    /**
     * Removes syntax whitespace at either end without changing whitespace inside the value.
     */
    static String strip(String value) {
        int start = 0;
        int end = value.length();
        while (start < end && isWhitespace(value.charAt(start))) {
            start++;
        }
        while (end > start && isWhitespace(value.charAt(end - 1))) {
            end--;
        }
        return value.substring(start, end);
    }
}
