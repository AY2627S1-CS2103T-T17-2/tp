package trackflow.logic.parser;

import static trackflow.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static trackflow.logic.parser.CliSyntax.PREFIX_AGE_CATEGORY;

import trackflow.logic.commands.FilterCommand;
import trackflow.logic.parser.exceptions.ParseException;

/**
 * Parses a single required age category for filtering.
 */
public class FilterCommandParser implements Parser<FilterCommand> {

    /**
     * Parses a required age category and returns a command that filters the roster.
     *
     * @throws ParseException If the arguments do not contain exactly one valid age category.
     */
    @Override
    public FilterCommand parse(String args) throws ParseException {
        ParserUtil.verifyNoUnknownPrefixes(args, FilterCommand.MESSAGE_USAGE, PREFIX_AGE_CATEGORY);
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(args, PREFIX_AGE_CATEGORY);
        if (arguments.getValue(PREFIX_AGE_CATEGORY).isEmpty() || !arguments.getPreamble().isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, FilterCommand.MESSAGE_USAGE));
        }

        arguments.verifyNoDuplicatePrefixesFor(PREFIX_AGE_CATEGORY);
        return new FilterCommand(ParserUtil.parseAgeCategory(arguments.getValue(PREFIX_AGE_CATEGORY).get()));
    }
}
