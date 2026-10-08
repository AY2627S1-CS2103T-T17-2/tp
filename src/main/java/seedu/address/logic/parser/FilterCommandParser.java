package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_AGE_CATEGORY;

import seedu.address.logic.commands.FilterCommand;
import seedu.address.logic.parser.exceptions.ParseException;

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
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(args, PREFIX_AGE_CATEGORY);
        if (arguments.getValue(PREFIX_AGE_CATEGORY).isEmpty() || !arguments.getPreamble().isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, FilterCommand.MESSAGE_USAGE));
        }

        arguments.verifyNoDuplicatePrefixesFor(PREFIX_AGE_CATEGORY);
        return new FilterCommand(ParserUtil.parseAgeCategory(arguments.getValue(PREFIX_AGE_CATEGORY).get()));
    }
}
