package trackflow.logic.parser;

import static trackflow.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static trackflow.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static trackflow.logic.parser.CliSyntax.PREFIX_AGE_CATEGORY;
import static trackflow.logic.parser.CliSyntax.PREFIX_EMAIL;
import static trackflow.logic.parser.CliSyntax.PREFIX_NAME;
import static trackflow.logic.parser.CliSyntax.PREFIX_PHONE;
import static trackflow.logic.parser.CliSyntax.PREFIX_REMARK;
import static trackflow.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import trackflow.logic.commands.FindCommand;
import trackflow.logic.parser.exceptions.ParseException;
import trackflow.model.person.PersonContainsKeywordsPredicate;
import trackflow.model.person.PersonContainsKeywordsPredicate.SearchField;

/**
 * Parses input arguments and creates a new FindCommand object
 */
public class FindCommandParser implements Parser<FindCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the FindCommand
     * and returns a FindCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public FindCommand parse(String args) throws ParseException {
        ParserUtil.verifyNoUnknownPrefixes(args, FindCommand.MESSAGE_USAGE,
                PREFIX_NAME, PREFIX_AGE_CATEGORY, PREFIX_PHONE, PREFIX_EMAIL,
                PREFIX_ADDRESS, PREFIX_REMARK, PREFIX_TAG);
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(args,
                PREFIX_NAME, PREFIX_AGE_CATEGORY, PREFIX_PHONE, PREFIX_EMAIL,
                PREFIX_ADDRESS, PREFIX_REMARK, PREFIX_TAG);
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_NAME, PREFIX_AGE_CATEGORY, PREFIX_PHONE, PREFIX_EMAIL,
                PREFIX_ADDRESS, PREFIX_REMARK, PREFIX_TAG);

        String preamble = arguments.getPreamble();
        List<String> broadKeywords = preamble.isEmpty() ? List.of() : List.of(preamble.split("\\s+"));
        Map<SearchField, List<String>> fieldKeywords = new EnumMap<>(SearchField.class);
        addFieldKeyword(arguments, PREFIX_NAME, SearchField.NAME, fieldKeywords);
        addFieldKeyword(arguments, PREFIX_AGE_CATEGORY, SearchField.AGE_CATEGORY, fieldKeywords);
        addFieldKeyword(arguments, PREFIX_PHONE, SearchField.PHONE, fieldKeywords);
        addFieldKeyword(arguments, PREFIX_EMAIL, SearchField.EMAIL, fieldKeywords);
        addFieldKeyword(arguments, PREFIX_ADDRESS, SearchField.ADDRESS, fieldKeywords);
        addFieldKeyword(arguments, PREFIX_REMARK, SearchField.REMARK, fieldKeywords);
        addFieldKeyword(arguments, PREFIX_TAG, SearchField.EVENT, fieldKeywords);

        if (broadKeywords.isEmpty() && fieldKeywords.isEmpty()) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
        }

        return new FindCommand(new PersonContainsKeywordsPredicate(broadKeywords, fieldKeywords));
    }

    private static void addFieldKeyword(ArgumentMultimap arguments, Prefix prefix, SearchField field,
            Map<SearchField, List<String>> fieldKeywords) throws ParseException {
        if (arguments.getValue(prefix).isEmpty()) {
            return;
        }
        String value = arguments.getValue(prefix).get();
        if (value.isBlank()) {
            throw new ParseException(String.format(FindCommand.MESSAGE_EMPTY_FIELD, prefix,
                    FindCommand.MESSAGE_USAGE));
        }
        fieldKeywords.put(field, List.of(value));
    }

}
