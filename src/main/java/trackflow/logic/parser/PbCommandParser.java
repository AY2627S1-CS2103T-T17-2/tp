package trackflow.logic.parser;

import static java.util.Objects.requireNonNull;
import static trackflow.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static trackflow.logic.parser.CliSyntax.PREFIX_DATE;
import static trackflow.logic.parser.CliSyntax.PREFIX_TAG;
import static trackflow.logic.parser.CliSyntax.PREFIX_VALUE;

import java.time.LocalDate;

import trackflow.commons.core.index.Index;
import trackflow.logic.commands.PbCommand;
import trackflow.logic.parser.exceptions.ParseException;
import trackflow.model.person.Performance;

/**
 * Parses input arguments and creates a new {@code PbCommand} object.
 */
public class PbCommandParser implements Parser<PbCommand> {

    @Override
    public PbCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ParserUtil.verifyNoUnknownPrefixes(args, PbCommand.MESSAGE_USAGE, PREFIX_TAG, PREFIX_VALUE, PREFIX_DATE);
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_TAG, PREFIX_VALUE, PREFIX_DATE);

        if (argMultimap.getValue(PREFIX_TAG).isEmpty() || argMultimap.getValue(PREFIX_VALUE).isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, PbCommand.MESSAGE_USAGE));
        }
        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_TAG, PREFIX_VALUE, PREFIX_DATE);

        Index index;
        try {
            index = ParserUtil.parseIndex(argMultimap.getPreamble());
        } catch (ParseException pe) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, PbCommand.MESSAGE_USAGE), pe);
        }

        String event = argMultimap.getValue(PREFIX_TAG).get();
        String result = argMultimap.getValue(PREFIX_VALUE).get();
        if (!Performance.isValidEvent(event)) {
            throw new ParseException(Performance.EVENT_MESSAGE_CONSTRAINTS);
        }
        if (!Performance.isValidResult(result)) {
            throw new ParseException(Performance.RESULT_MESSAGE_CONSTRAINTS);
        }

        LocalDate date = null;
        if (argMultimap.getValue(PREFIX_DATE).isPresent()) {
            String rawDate = argMultimap.getValue(PREFIX_DATE).get();
            if (!Performance.isValidDate(rawDate)) {
                throw new ParseException(Performance.DATE_MESSAGE_CONSTRAINTS);
            }
            date = LocalDate.parse(rawDate.trim());
        }

        return new PbCommand(index, new Performance(event, result, date));
    }
}
