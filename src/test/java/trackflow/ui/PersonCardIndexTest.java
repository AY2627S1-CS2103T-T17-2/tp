package trackflow.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Locale;

import org.junit.jupiter.api.Test;

import trackflow.logic.parser.ParserUtil;

/**
 * Checks that displayed indexes remain usable command arguments under locales with non-ASCII numerals.
 */
public class PersonCardIndexTest {

    @Test
    public void formatDisplayedIndex_nonAsciiNumeralLocale_remainsAcceptedByParser() throws Exception {
        Locale previous = Locale.getDefault();
        try {
            for (String language : new String[] {"ar-EG", "fa-IR", "th-TH"}) {
                Locale.setDefault(Locale.forLanguageTag(language));
                assertEquals("01", PersonCard.formatDisplayedIndex(1));
                assertEquals("12", PersonCard.formatDisplayedIndex(12));
                assertEquals("123", PersonCard.formatDisplayedIndex(123));
                for (int index : new int[] {1, 12, 123}) {
                    assertEquals(index, ParserUtil.parseIndex(PersonCard.formatDisplayedIndex(index)).getOneBased());
                }
            }
        } finally {
            Locale.setDefault(previous);
        }
    }
}
