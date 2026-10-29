// @@author Austin-Chia
package trackflow.logic;
// @@author

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// @@author Austin-Chia
import trackflow.logic.parser.Prefix;
import trackflow.model.person.Person;
// @@author

/**
 * Container for user visible messages.
 */
public class Messages {

    public static final String MESSAGE_EMPTY_COMMAND = "No command entered. Type a command and try again.";
    public static final String MESSAGE_UNKNOWN_COMMAND = "Unknown command: %1$s.";
    public static final String MESSAGE_INVALID_COMMAND_FORMAT = "Invalid command format!\n%1$s";
    public static final String MESSAGE_INVALID_PERSON_DISPLAYED_INDEX =
            "The athlete index provided is invalid.";
    public static final String MESSAGE_DUPLICATE_FIELDS =
                "Multiple values specified for the following single-valued field(s): ";
    public static final String MESSAGE_UNKNOWN_PARAMETER =
            "%1$s is not valid for this command.\n%2$s";
    public static final String MESSAGE_LITERAL_PREFIX_HINT =
            "\nFor literal address or remark text, double a parameter-like slash (e.g. w//coach). "
            + "If a value starts with '/', put a space after its prefix (e.g. addr/ //server/share).";

    public static final String MESSAGE_POSSIBLE_DUPLICATE =
            "\nWarning: possible duplicate. Shared contact details:\n%1$s\nPlease review the records.";

    /**
     * Warns about shared contacts without blocking the change. Each matching record is identified by its details
     * and the shared contact fields, rather than a displayed index that may refer to another filtered view.
     */
    public static String getPossibleDuplicateWarning(Person person, Collection<Person> otherPersons) {
        String matches = otherPersons.stream()
                .filter(other -> !person.isSamePerson(other))
                .map(other -> formatSharedContact(person, other))
                .filter(match -> !match.isEmpty())
                .collect(Collectors.joining("\n"));
        return matches.isEmpty() ? "" : String.format(MESSAGE_POSSIBLE_DUPLICATE, matches);
    }

    /**
     * Describes an existing record and its matching contact fields, or returns an empty string if neither matches.
     * Phone comparison uses the shared significant-digit rule; email comparison ignores case and surrounding spaces.
     */
    private static String formatSharedContact(Person person, Person other) {
        List<String> matchingFields = new ArrayList<>();
        if (person.getPhone().getSignificantDigits().equals(other.getPhone().getSignificantDigits())) {
            matchingFields.add("phone");
        }
        if (person.getEmail().value.trim().equalsIgnoreCase(other.getEmail().value.trim())) {
            matchingFields.add("email");
        }
        if (matchingFields.isEmpty()) {
            return "";
        }
        return "- " + formatAthlete(other) + "; Matches: " + String.join(" and ", matchingFields);
    }

    /**
     * Returns an error message indicating the duplicate prefixes.
     */
    public static String getErrorMessageForDuplicatePrefixes(Prefix... duplicatePrefixes) {
        assert duplicatePrefixes.length > 0;

        Set<String> duplicateFields =
                Stream.of(duplicatePrefixes).map(Prefix::toString).collect(Collectors.toSet());

        return MESSAGE_DUPLICATE_FIELDS + String.join(" ", duplicateFields);
    }

    /**
     * Formats the {@code person} for display to the user.
     */
    public static String formatAthlete(Person person) {
        return person.getName() + "; Age category: " + person.getAgeCategory()
                + "; Phone: " + person.getPhone() + "; Email: " + person.getEmail();
    }

}
