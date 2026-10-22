package trackflow.testutil;

import static trackflow.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static trackflow.logic.parser.CliSyntax.PREFIX_AGE_CATEGORY;
import static trackflow.logic.parser.CliSyntax.PREFIX_EMAIL;
import static trackflow.logic.parser.CliSyntax.PREFIX_NAME;
import static trackflow.logic.parser.CliSyntax.PREFIX_PHONE;
import static trackflow.logic.parser.CliSyntax.PREFIX_REMARK;
import static trackflow.logic.parser.CliSyntax.PREFIX_TAG;

import trackflow.logic.commands.AddCommand;
import trackflow.logic.commands.EditCommand.EditPersonDescriptor;
import trackflow.model.person.Person;

/**
 * A utility class for Person.
 */
public class PersonUtil {

    /**
     * Returns an add command string for adding the {@code person}.
     */
    public static String getAddCommand(Person person) {
        return AddCommand.COMMAND_WORD + " " + getPersonDetails(person);
    }

    /**
     * Returns the part of command string for the given {@code person}'s details.
     */
    public static String getPersonDetails(Person person) {
        StringBuilder sb = new StringBuilder();
        sb.append(PREFIX_NAME + person.getName().fullName + " ");
        sb.append(PREFIX_PHONE + person.getPhone().value + " ");
        sb.append(PREFIX_EMAIL + person.getEmail().value + " ");
        sb.append(PREFIX_AGE_CATEGORY + person.getAgeCategory().value + " ");
        if (!person.getAddress().value.isEmpty()) {
            sb.append(PREFIX_ADDRESS).append(person.getAddress().value).append(" ");
        }
        if (!person.getRemark().value.isEmpty()) {
            sb.append(PREFIX_REMARK).append(person.getRemark().value).append(" ");
        }
        person.getTags().stream().forEach(
            s -> sb.append(PREFIX_TAG + s.tagName + " ")
        );
        return sb.toString();
    }

    /**
     * Returns the part of command string for the given {@code EditPersonDescriptor}'s details.
     */
    public static String getEditPersonDescriptorDetails(EditPersonDescriptor descriptor) {
        StringBuilder sb = new StringBuilder();
        descriptor.getName().ifPresent(name -> sb.append(PREFIX_NAME).append(name.fullName).append(" "));
        descriptor.getPhone().ifPresent(phone -> sb.append(PREFIX_PHONE).append(phone.value).append(" "));
        descriptor.getEmail().ifPresent(email -> sb.append(PREFIX_EMAIL).append(email.value).append(" "));
        descriptor.getAddress().ifPresent(address -> sb.append(PREFIX_ADDRESS).append(address.value).append(" "));
        descriptor.getAgeCategory().ifPresent(category ->
                sb.append(PREFIX_AGE_CATEGORY).append(category.value).append(" "));
        return sb.toString();
    }
}
