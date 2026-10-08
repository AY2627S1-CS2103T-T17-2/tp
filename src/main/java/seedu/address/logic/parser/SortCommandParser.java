package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.Locale;

import seedu.address.logic.commands.SortCommand;
import seedu.address.logic.commands.SortCommand.SortField;
import seedu.address.logic.commands.SortCommand.SortOrder;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses arguments for a {@code SortCommand}.
 */
public class SortCommandParser implements Parser<SortCommand> {

    @Override
    public SortCommand parse(String arguments) throws ParseException {
        String trimmedArguments = arguments.trim();
        if (trimmedArguments.isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, SortCommand.MESSAGE_USAGE));
        }

        String[] tokens = trimmedArguments.split("\\s+");
        if (tokens.length > 2) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, SortCommand.MESSAGE_USAGE));
        }

        SortField field = parseField(tokens[0]);
        SortOrder order = tokens.length == 1 ? SortOrder.ASCENDING : parseOrder(tokens[1]);
        return new SortCommand(field, order);
    }

    private static SortField parseField(String field) throws ParseException {
        return switch (field.toLowerCase(Locale.ROOT)) {
            case "name" -> SortField.NAME;
            case "age" -> SortField.AGE;
            case "phone" -> SortField.PHONE;
            case "email" -> SortField.EMAIL;
            case "address" -> SortField.ADDRESS;
            default -> throw new ParseException(String.format(SortCommand.MESSAGE_INVALID_FIELD, field));
        };
    }

    private static SortOrder parseOrder(String order) throws ParseException {
        return switch (order.toLowerCase(Locale.ROOT)) {
            case "asc", "ascending" -> SortOrder.ASCENDING;
            case "desc", "descending" -> SortOrder.DESCENDING;
            default -> throw new ParseException(String.format(SortCommand.MESSAGE_INVALID_ORDER, order));
        };
    }
}
