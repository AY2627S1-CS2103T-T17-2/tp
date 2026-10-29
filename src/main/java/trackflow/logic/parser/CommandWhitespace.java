package trackflow.logic.parser;

/**
 * Defines syntax whitespace consistently, including non-breaking spaces pasted from documents.
 * Field contents are not rewritten; callers strip boundaries or split syntax tokens explicitly.
 */
public final class CommandWhitespace {

    static final String CHARACTERS = "\\p{javaWhitespace}\\p{javaSpaceChar}";
    public static final String SEPARATOR_REGEX = "[" + CHARACTERS + "]+";
    static final String TOKEN_BOUNDARY_REGEX = "(?<![^" + CHARACTERS + "])";

    private CommandWhitespace() {
    }

    /**
     * Returns whether a character is accepted as a separator in command syntax.
     */
    public static boolean isWhitespace(int character) {
        return Character.isWhitespace(character) || Character.isSpaceChar(character);
    }

    /**
     * Removes leading syntax whitespace, retaining any separator after the command word.
     */
    public static String stripLeading(String value) {
        int start = 0;
        while (start < value.length() && isWhitespace(value.charAt(start))) {
            start++;
        }
        return value.substring(start);
    }

    /**
     * Removes syntax whitespace at either end without changing whitespace inside the value.
     */
    public static String strip(String value) {
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
