package trackflow.logic.parser;

import static java.util.Objects.requireNonNull;
import static trackflow.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import trackflow.commons.core.index.Index;
import trackflow.logic.commands.PbDelCommand;
import trackflow.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new {@code PbDelCommand} object.
 */
public class PbDelCommandParser implements Parser<PbDelCommand> {

    @Override
    public PbDelCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String[] tokens = args.trim().split("\\s+");
        if (tokens.length != 2) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, PbDelCommand.MESSAGE_USAGE));
        }
        try {
            Index athleteIndex = ParserUtil.parseIndex(tokens[0]);
            Index performanceIndex = ParserUtil.parseIndex(tokens[1]);
            return new PbDelCommand(athleteIndex, performanceIndex);
        } catch (ParseException pe) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, PbDelCommand.MESSAGE_USAGE), pe);
        }
    }
}
