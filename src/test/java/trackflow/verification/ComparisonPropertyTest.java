package trackflow.verification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Locale;
import java.util.Random;

import org.junit.jupiter.api.Test;

import trackflow.logic.parser.ParserUtil;
import trackflow.model.person.Person;
import trackflow.model.person.Phone;
import trackflow.testutil.PersonBuilder;

/**
 * Generated metamorphic properties use independent digit strings and contact buckets as their oracles.
 */
public class ComparisonPropertyTest {
    private static final int CASES = 500;

    @Test
    public void formattingChanges_preservePhoneValueEqualityAndSingaporeDuplicateKeys() throws Exception {
        long seed = Long.getLong("verification.seed", 2103L);
        Random random = new Random(seed);
        for (int iteration = 0; iteration < CASES; iteration++) {
            String digits = Integer.toString(80_000_000 + random.nextInt(20_000_000));
            String first = digits.substring(0, 4);
            String last = digits.substring(4);
            List<String> local = List.of(digits, first + " " + last, first + "-" + last,
                    "(Office 9) " + first + " " + last, digits + " (ext 123)");
            List<String> international = List.of("+65 " + digits, "+65" + digits, "(+65)" + digits,
                    "65 " + first + "-" + last, "(HP) (+65) " + first + " " + last + " (5)");
            String context = "seed=" + seed + ", iteration=" + iteration;
            Phone reference = ParserUtil.parsePhone(digits);
            Phone internationalReference = ParserUtil.parsePhone("+65 " + digits);
            for (String text : local) {
                Phone phone = ParserUtil.parsePhone(text);
                assertEquals(digits, phone.getSignificantDigits(), context + ": " + text);
                assertEquals(reference, phone, context);
                assertEquals(reference.hashCode(), phone.hashCode(), context);
                assertEquals("65" + digits, phone.getComparisonKey(), context);
            }
            for (String text : international) {
                Phone phone = ParserUtil.parsePhone(text);
                assertEquals("65" + digits, phone.getSignificantDigits(), context + ": " + text);
                assertEquals(internationalReference, phone, context);
                assertEquals(internationalReference.hashCode(), phone.hashCode(), context);
                assertEquals(reference.getComparisonKey(), phone.getComparisonKey(), context);
                assertFalse(reference.equals(phone), context);
            }
        }
    }

    @Test
    public void duplicateDetection_matchesIndependentNamePhoneAndEmailBuckets() {
        long seed = Long.getLong("verification.seed", 2103L) ^ 217L;
        Random random = new Random(seed);
        for (int iteration = 0; iteration < CASES; iteration++) {
            int leftName = random.nextInt(3);
            int rightName = random.nextInt(3);
            int leftPhone = random.nextInt(3);
            int rightPhone = random.nextInt(3);
            int leftEmail = random.nextInt(3);
            int rightEmail = random.nextInt(3);
            Person left = person(leftName, leftPhone, leftEmail, random.nextBoolean());
            Person right = person(rightName, rightPhone, rightEmail, random.nextBoolean());
            boolean expected = leftName == rightName && (leftPhone == rightPhone || leftEmail == rightEmail);
            String context = "seed=" + seed + ", iteration=" + iteration;
            assertEquals(expected, left.isSamePerson(right), context);
            assertEquals(expected, right.isSamePerson(left), context);
            assertTrue(left.isSamePerson(left), context);
            assertFalse(left.isSamePerson(null), context);
        }
    }

    private static Person person(int nameBucket, int phoneBucket, int emailBucket, boolean alternate) {
        String name = "Athlete " + nameBucket;
        String email = "contact" + emailBucket + "@example.com";
        String phone = Integer.toString(91_234_560 + phoneBucket);
        return new PersonBuilder().withName(alternate ? "ATHLETE   " + nameBucket : name)
                .withPhone(alternate ? "(+65) " + phone + " (HP)" : phone)
                .withEmail(alternate ? email.toUpperCase(Locale.ROOT) : email).build();
    }
}
