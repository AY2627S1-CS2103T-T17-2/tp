package trackflow.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import trackflow.model.person.Person;
import trackflow.testutil.PersonBuilder;

/**
 * Checks that shared-contact warnings distinguish records and identify the matching contact fields.
 */
public class MessagesTest {

    @Test
    public void getPossibleDuplicateWarning_noSharedContacts_returnsEmpty() {
        Person added = new PersonBuilder().withName("Blake Tan")
                .withPhone("91111111").withEmail("blake@example.com").build();
        Person other = new PersonBuilder().withName("Alex Tan")
                .withPhone("92222222").withEmail("alex@example.com").build();

        assertEquals("", Messages.getPossibleDuplicateWarning(added, List.of()));
        assertEquals("", Messages.getPossibleDuplicateWarning(added, List.of(other)));
    }

    @Test
    public void getPossibleDuplicateWarning_sameNamedMatches_identifiesEachRecordAndField() {
        Person phoneMatch = new PersonBuilder().withName("Alex Tan")
                .withPhone("91111111").withEmail("alex1@example.com").build();
        Person emailMatch = new PersonBuilder().withName("Alex Tan")
                .withPhone("92222222").withEmail("alex2@example.com").build();
        Person unrelated = new PersonBuilder().withName("Other Athlete")
                .withPhone("93333333").withEmail("other@example.com").build();
        Person added = new PersonBuilder().withName("Blake Tan")
                .withPhone("91111111").withEmail("alex2@example.com").build();

        String expected = "\nWarning: possible duplicate. Shared contact details:\n"
                + "- Alex Tan; Age category: Open; Phone: 91111111; Email: alex1@example.com; Matches: phone\n"
                + "- Alex Tan; Age category: Open; Phone: 92222222; Email: alex2@example.com; Matches: email\n"
                + "Please review the records.";
        assertEquals(expected, Messages.getPossibleDuplicateWarning(added,
                List.of(phoneMatch, unrelated, emailMatch)));
    }

    @Test
    public void getPossibleDuplicateWarning_formattedPhoneWithNumericNotes_identifiesPhoneMatch() {
        Person added = new PersonBuilder().withName("Blake Tan")
                .withPhone("(+65) 9123 4567 (Office 1)").withEmail("blake@example.com").build();
        Person other = new PersonBuilder().withName("Alex Tan")
                .withPhone("+65 91234567 (Office 2)").withEmail("alex@example.com").build();

        assertEquals(String.format(Messages.MESSAGE_POSSIBLE_DUPLICATE,
                "- Alex Tan; Age category: Open; Phone: +65 91234567 (Office 2); "
                        + "Email: alex@example.com; Matches: phone"),
                Messages.getPossibleDuplicateWarning(added, List.of(other)));
    }

    @Test
    public void getPossibleDuplicateWarning_emailCaseDiffers_identifiesEmailMatch() {
        Person added = new PersonBuilder().withName("Blake Tan")
                .withPhone("91111111").withEmail("COACH@example.com").build();
        Person other = new PersonBuilder().withName("Alex Tan")
                .withPhone("92222222").withEmail("coach@example.com").build();

        assertEquals(String.format(Messages.MESSAGE_POSSIBLE_DUPLICATE,
                "- Alex Tan; Age category: Open; Phone: 92222222; Email: coach@example.com; Matches: email"),
                Messages.getPossibleDuplicateWarning(added, List.of(other)));
    }

    @Test
    public void getPossibleDuplicateWarning_bothContactsMatch_listsRecordOnceWithBothFields() {
        Person added = new PersonBuilder().withName("Blake Tan")
                .withPhone("9123 4567 (HP)").withEmail("COACH@example.com").build();
        Person other = new PersonBuilder().withName("Alex Tan")
                .withPhone("91234567").withEmail("coach@example.com").build();

        assertEquals(String.format(Messages.MESSAGE_POSSIBLE_DUPLICATE,
                "- Alex Tan; Age category: Open; Phone: 91234567; Email: coach@example.com; Matches: phone and email"),
                Messages.getPossibleDuplicateWarning(added, List.of(other)));
    }

    @Test
    public void getPossibleDuplicateWarning_differentCountryCode_doesNotMatchPhone() {
        Person added = new PersonBuilder().withName("Blake Tan")
                .withPhone("(+65) 91234567").withEmail("blake@example.com").build();
        Person other = new PersonBuilder().withName("Alex Tan")
                .withPhone("91234567").withEmail("alex@example.com").build();

        assertEquals("", Messages.getPossibleDuplicateWarning(added, List.of(other)));
    }

    @Test
    public void getPossibleDuplicateWarning_duplicateIdentity_doesNotWarn() {
        Person added = new PersonBuilder().withName("Alex Tan")
                .withPhone("91234567").withEmail("alex@example.com").build();
        Person duplicate = new PersonBuilder(added).withName("ALEX   TAN").build();

        assertEquals("", Messages.getPossibleDuplicateWarning(added, List.of(added, duplicate)));
    }
}
