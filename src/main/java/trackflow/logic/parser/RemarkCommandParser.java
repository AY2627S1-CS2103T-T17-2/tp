package trackflow.logic.parser;

import static java.util.Objects.requireNonNull;
import static trackflow.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static trackflow.logic.parser.CliSyntax.PREFIX_REMARK;

import trackflow.commons.core.index.Index;
import trackflow.logic.commands.RemarkCommand;
import trackflow.logic.parser.exceptions.ParseException;
import trackflow.model.person.Remark;

/**
 * Parses input arguments and creates a new {@code RemarkCommand} object.
 */
public class RemarkCommandParser implements Parser<RemarkCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the {@code RemarkCommand}
     * and returns a {@code RemarkCommand} object for execution.
     *
     * @throws ParseException if the user input does not conform to the expected format.
     */
    public RemarkCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ParserUtil.verifyNoUnknownPrefixes(args, RemarkCommand.MESSAGE_USAGE, PREFIX_REMARK);
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_REMARK);

        if (argMultimap.getValue(PREFIX_REMARK).isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, RemarkCommand.MESSAGE_USAGE));
        }
        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_REMARK);

        Index index;
        try {
            index = ParserUtil.parseIndex(argMultimap.getPreamble());
        } catch (ParseException pe) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, RemarkCommand.MESSAGE_USAGE), pe);
        }

        String remark = argMultimap.getValue(PREFIX_REMARK).get();

        return new RemarkCommand(index, new Remark(remark));
    }
}
