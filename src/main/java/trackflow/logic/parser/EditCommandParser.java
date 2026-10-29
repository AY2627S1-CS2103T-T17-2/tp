// @@author qiyuannn
package trackflow.logic.parser;
// @@author

import static java.util.Objects.requireNonNull;
// @@author qiyuannn
import static trackflow.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static trackflow.logic.parser.CliSyntax.PREFIX_AGE_CATEGORY;
import static trackflow.logic.parser.CliSyntax.PREFIX_EMAIL;
import static trackflow.logic.parser.CliSyntax.PREFIX_NAME;
import static trackflow.logic.parser.CliSyntax.PREFIX_PHONE;
// @@author

// @@author qiyuannn
import trackflow.commons.core.index.Index;
import trackflow.logic.commands.EditCommand;
import trackflow.logic.commands.EditCommand.EditPersonDescriptor;
import trackflow.logic.parser.exceptions.ParseException;
// @@author

/**
 * Parses input arguments and creates a new EditCommand object.
 */
public class EditCommandParser implements Parser<EditCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the EditCommand
     * and returns an EditCommand object for execution.
     *
     * @throws ParseException if the user input does not conform to the expected format.
     */
    public EditCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ParserUtil.verifyNoUnknownPrefixes(args, EditCommand.MESSAGE_USAGE,
                PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_AGE_CATEGORY, PREFIX_ADDRESS);
        ArgumentMultimap argMultimap =
                ArgumentTokenizer.tokenize(args, PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_AGE_CATEGORY,
                        PREFIX_ADDRESS);

        Index index;

        try {
            index = ParserUtil.parseIndex(argMultimap.getPreamble());
        } catch (ParseException pe) {
            throw new ParseException(ParserUtil.getInvalidCommandFormatMessage(args, EditCommand.MESSAGE_USAGE), pe);
        }

        argMultimap.verifyNoDuplicatePrefixesFor(
                PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_AGE_CATEGORY, PREFIX_ADDRESS);

        EditPersonDescriptor editPersonDescriptor = new EditPersonDescriptor();

        if (argMultimap.getValue(PREFIX_NAME).isPresent()) {
            editPersonDescriptor.setName(ParserUtil.parseName(argMultimap.getValue(PREFIX_NAME).get()));
        }
        if (argMultimap.getValue(PREFIX_PHONE).isPresent()) {
            editPersonDescriptor.setPhone(ParserUtil.parsePhone(argMultimap.getValue(PREFIX_PHONE).get()));
        }
        if (argMultimap.getValue(PREFIX_EMAIL).isPresent()) {
            editPersonDescriptor.setEmail(ParserUtil.parseEmail(argMultimap.getValue(PREFIX_EMAIL).get()));
        }
        if (argMultimap.getValue(PREFIX_AGE_CATEGORY).isPresent()) {
            editPersonDescriptor.setAgeCategory(
                    ParserUtil.parseAgeCategory(argMultimap.getValue(PREFIX_AGE_CATEGORY).get()));
        }

        if (argMultimap.getValue(PREFIX_ADDRESS).isPresent()) {
            editPersonDescriptor.setAddress(ParserUtil.parseAddress(argMultimap.getValue(PREFIX_ADDRESS).get()));
        }

        if (!editPersonDescriptor.isAnyFieldEdited()) {
            throw new ParseException(EditCommand.MESSAGE_NOT_EDITED);
        }

        return new EditCommand(index, editPersonDescriptor);
    }

}
