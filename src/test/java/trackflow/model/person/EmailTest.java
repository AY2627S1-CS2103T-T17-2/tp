package trackflow.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static trackflow.testutil.Assert.assertThrows;

import java.time.Duration;

import org.junit.jupiter.api.Test;

public class EmailTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Email(null));
    }

    @Test
    public void constructor_invalidEmail_throwsIllegalArgumentException() {
        String invalidEmail = "";
        assertThrows(IllegalArgumentException.class, Email.MESSAGE_CONSTRAINTS, () -> new Email(invalidEmail));
    }

    @Test
    public void isValidEmail() {
        // null email
        assertThrows(NullPointerException.class, () -> Email.isValidEmail(null));

        // blank email
        assertFalse(Email.isValidEmail("")); // empty string
        assertFalse(Email.isValidEmail(" ")); // spaces only

        // missing parts
        assertFalse(Email.isValidEmail("@example.com")); // missing local part
        assertFalse(Email.isValidEmail("peterjackexample.com")); // missing '@' symbol
        assertFalse(Email.isValidEmail("peterjack@")); // missing domain name

        // invalid parts
        assertFalse(Email.isValidEmail("peterjack@-")); // invalid domain name
        assertFalse(Email.isValidEmail("peterjack@exam_ple.com")); // underscore in domain name
        assertFalse(Email.isValidEmail("peter jack@example.com")); // spaces in local part
        assertFalse(Email.isValidEmail("peterjack@exam ple.com")); // spaces in domain name
        assertFalse(Email.isValidEmail(" peterjack@example.com")); // leading space
        assertFalse(Email.isValidEmail("peterjack@example.com ")); // trailing space
        assertFalse(Email.isValidEmail("peterjack@@example.com")); // double '@' symbol
        assertFalse(Email.isValidEmail("peter@jack@example.com")); // '@' symbol in local part
        assertFalse(Email.isValidEmail("-peterjack@example.com")); // local part starts with a hyphen
        assertFalse(Email.isValidEmail("peterjack-@example.com")); // local part ends with a hyphen
        assertFalse(Email.isValidEmail("peter..jack@example.com")); // local part has two consecutive periods
        assertFalse(Email.isValidEmail("peterjack@example@com")); // '@' symbol in domain name
        assertFalse(Email.isValidEmail("peterjack@.example.com")); // domain name starts with a period
        assertFalse(Email.isValidEmail("peterjack@example.com.")); // domain name ends with a period
        assertFalse(Email.isValidEmail("peterjack@-example.com")); // domain name starts with a hyphen
        assertFalse(Email.isValidEmail("peterjack@example.com-")); // domain name ends with a hyphen
        assertFalse(Email.isValidEmail("peterjack@example.c")); // top level domain has less than two chars

        // valid email
        assertTrue(Email.isValidEmail("PeterJack_1190@example.com")); // underscore in local part
        assertTrue(Email.isValidEmail("PeterJack.1190@example.com")); // period in local part
        assertTrue(Email.isValidEmail("PeterJack+1190@example.com")); // '+' symbol in local part
        assertTrue(Email.isValidEmail("PeterJack-1190@example.com")); // hyphen in local part
        assertTrue(Email.isValidEmail("a@bc")); // minimal
        assertTrue(Email.isValidEmail("test@localhost")); // alphabets only
        assertTrue(Email.isValidEmail("123@145")); // numeric local part and domain name
        assertTrue(Email.isValidEmail("a1+be.d@example1.com")); // mixture of alphanumeric and special characters
        assertTrue(Email.isValidEmail("peter_jack@very-very-very-long-example.com")); // long domain name
        assertTrue(Email.isValidEmail("if.you.dream.it_you.can.do.it@example.com")); // long local part
        assertTrue(Email.isValidEmail("e1234567@u.nus.edu")); // more than one period in domain
        assertTrue(Email.isValidEmail("test@example.a-b")); // hyphen in top-level domain label
        assertTrue(Email.isValidEmail("test@example.a-b-c")); // multiple hyphens in top-level domain label
        assertTrue(Email.isValidEmail("test@example.co-op")); // hyphenated domain label
        assertFalse(Email.isValidEmail("test@example.a--b")); // consecutive hyphens in domain label
        assertFalse(Email.isValidEmail("test@example.a-b-")); // trailing hyphen in domain label
        assertFalse(Email.isValidEmail("test@example.-a-b")); // leading hyphen in domain label

        // additional special character boundaries in local part
        assertFalse(Email.isValidEmail(".peterjack@example.com")); // local part starts with a period
        assertFalse(Email.isValidEmail("peterjack.@example.com")); // local part ends with a period
        assertFalse(Email.isValidEmail("+peterjack@example.com")); // local part starts with a plus
        assertFalse(Email.isValidEmail("peterjack+@example.com")); // local part ends with a plus
        assertFalse(Email.isValidEmail("_peterjack@example.com")); // local part starts with an underscore
        assertFalse(Email.isValidEmail("peterjack_@example.com")); // local part ends with an underscore
        assertFalse(Email.isValidEmail("peter.+jack@example.com")); // consecutive special characters (.+)
        assertFalse(Email.isValidEmail("peter._jack@example.com")); // consecutive special characters (._)
        assertFalse(Email.isValidEmail("peter+-jack@example.com")); // consecutive special characters (+-)
        assertFalse(Email.isValidEmail("peter__jack@example.com")); // consecutive underscores
        assertFalse(Email.isValidEmail("peter++jack@example.com")); // consecutive pluses

        // additional domain boundaries
        assertFalse(Email.isValidEmail("peterjack@sub.-domain.com")); // domain label starts with a hyphen
        assertFalse(Email.isValidEmail("peterjack@sub-.domain.com")); // domain label ends with a hyphen
        assertFalse(Email.isValidEmail("peterjack@sub--domain.com")); // consecutive hyphens in domain label
        assertFalse(Email.isValidEmail("peterjack@example..com")); // consecutive periods in domain name
        assertFalse(Email.isValidEmail("a@b")); // single-character domain without period
        assertFalse(Email.isValidEmail("@bc")); // empty local part
        assertFalse(Email.isValidEmail("a@")); // empty domain

        // additional valid email structures
        assertTrue(Email.isValidEmail("peterjack@local-host")); // single domain label with hyphen
        assertTrue(Email.isValidEmail("a.b.c@example.com")); // multiple periods in local part
        assertTrue(Email.isValidEmail("a+b-c_d.e@example.com")); // all special characters in local part
        assertTrue(Email.isValidEmail("test@sub-domain.example.com")); // hyphen in intermediate domain label
    }

    @Test
    public void isValidEmail_pathologicalInputs_evaluatesQuicklyWithoutStackOverflow() {
        Duration timeout = Duration.ofMillis(200);

        // Pathological long inputs
        String longLocalWithoutAt = "a".repeat(5000);
        String longDomain = "a@" + "b".repeat(5000);
        String longLocalWithSpecialChars = "a" + ".a".repeat(2500);
        String longDomainWithHyphens = "a@a" + "-a".repeat(2500) + ".com";
        String longDomainWithPeriods = "a@" + "a.".repeat(2500) + "com";

        // Adversarial backtracking patterns with trailing invalid characters
        String adversarialDomain = "a@" + "b".repeat(50) + "!";
        String adversarialDomainHyphens = "a@" + "b-b".repeat(25) + "-";
        String adversarialLocal = "a" + ".a".repeat(50) + "!";
        String adversarialNearMax = "a@" + "b".repeat(250) + "!";

        assertTimeoutPreemptively(timeout, () -> assertFalse(Email.isValidEmail(longLocalWithoutAt)));
        assertTimeoutPreemptively(timeout, () -> assertFalse(Email.isValidEmail(longDomain)));
        assertTimeoutPreemptively(timeout, () -> assertFalse(Email.isValidEmail(longLocalWithSpecialChars)));
        assertTimeoutPreemptively(timeout, () -> assertFalse(Email.isValidEmail(longDomainWithHyphens)));
        assertTimeoutPreemptively(timeout, () -> assertFalse(Email.isValidEmail(longDomainWithPeriods)));
        assertTimeoutPreemptively(timeout, () -> assertFalse(Email.isValidEmail(adversarialDomain)));
        assertTimeoutPreemptively(timeout, () -> assertFalse(Email.isValidEmail(adversarialDomainHyphens)));
        assertTimeoutPreemptively(timeout, () -> assertFalse(Email.isValidEmail(adversarialLocal)));
        assertTimeoutPreemptively(timeout, () -> assertFalse(Email.isValidEmail(adversarialNearMax)));

        // Verify VALIDATION_REGEX itself evaluates without StackOverflowError or freezing
        java.util.regex.Pattern regexPattern = java.util.regex.Pattern.compile(Email.VALIDATION_REGEX);
        assertTimeoutPreemptively(timeout, () -> assertFalse(regexPattern.matcher(longLocalWithoutAt).matches()));
        assertTimeoutPreemptively(timeout, () ->
                assertFalse(regexPattern.matcher(longLocalWithSpecialChars).matches()));
        assertTimeoutPreemptively(timeout, () ->
                assertFalse(regexPattern.matcher("a@" + "a.".repeat(2500)).matches()));
        assertTimeoutPreemptively(timeout, () -> assertFalse(regexPattern.matcher(adversarialNearMax).matches()));
    }

    @Test
    public void isValidEmail_unicodeInputs_rejectedQuickly() {
        Duration timeout = Duration.ofMillis(200);

        // Unicode and surrogate pair sequences in local-part and domain
        assertFalse(Email.isValidEmail("test\uD83D\uDE00@example.com"));
        assertFalse(Email.isValidEmail("\u4E2D\u6587@example.com"));
        assertFalse(Email.isValidEmail("user@\u4E2D\u6587.com"));
        assertFalse(Email.isValidEmail("user@example.\uD83D\uDE00"));
        assertFalse(Email.isValidEmail("a\u0300@example.com"));

        // Pathological Unicode inputs
        String longSurrogatePair = "\uD83D\uDE00".repeat(2500);
        String longCjk = "a@" + "\u4E2D".repeat(5000);
        assertTimeoutPreemptively(timeout, () -> assertFalse(Email.isValidEmail(longSurrogatePair)));
        assertTimeoutPreemptively(timeout, () -> assertFalse(Email.isValidEmail(longCjk)));
    }

    @Test
    public void isValidEmail_boundaryLengths() {
        // At max length (254 chars): "a@" + 250 'b's + "cd" = 254 chars
        String maxValidEmail = "a@" + "b".repeat(250) + "cd";
        assertEquals(Email.MAX_LENGTH, maxValidEmail.length());
        assertTrue(Email.isValidEmail(maxValidEmail));

        // Just above max length (255 chars): "a@" + 251 'b's + "cd" = 255 chars
        String exceedMaxEmail = "a@" + "b".repeat(251) + "cd";
        assertEquals(Email.MAX_LENGTH + 1, exceedMaxEmail.length());
        assertFalse(Email.isValidEmail(exceedMaxEmail));
    }

    @Test
    public void constructor_pathologicalInput_throwsIllegalArgumentExceptionQuickly() {
        String longInput = "a".repeat(5000);
        assertTimeoutPreemptively(Duration.ofMillis(200), () ->
                assertThrows(IllegalArgumentException.class, Email.MESSAGE_CONSTRAINTS, () -> new Email(longInput)));
    }

    @Test
    public void equals() {
        Email email = new Email("valid@email");

        // same values -> returns true
        assertTrue(email.equals(new Email("valid@email")));
        assertEquals(email.hashCode(), new Email("valid@email").hashCode());
        assertEquals("valid@email", email.toString());

        // same object -> returns true
        assertTrue(email.equals(email));

        // null -> returns false
        assertFalse(email.equals(null));

        // different types -> returns false
        assertFalse(email.equals(5.0f));

        // different values -> returns false
        assertFalse(email.equals(new Email("other.valid@email")));
    }
}
