package trackflow.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static trackflow.logic.Messages.MESSAGE_EMPTY_COMMAND;
import static trackflow.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static trackflow.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static trackflow.logic.parser.CliSyntax.PREFIX_REMARK;
import static trackflow.logic.parser.CliSyntax.PREFIX_TAG;
import static trackflow.testutil.Assert.assertThrows;
import static trackflow.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import trackflow.logic.commands.AddCommand;
import trackflow.logic.commands.ClearCommand;
import trackflow.logic.commands.DeleteCommand;
import trackflow.logic.commands.EditCommand;
import trackflow.logic.commands.EditCommand.EditPersonDescriptor;
import trackflow.logic.commands.ExitCommand;
import trackflow.logic.commands.FindCommand;
import trackflow.logic.commands.HelpCommand;
import trackflow.logic.commands.ListAthleteCommand;
import trackflow.logic.commands.RemarkCommand;
import trackflow.logic.commands.SortCommand;
import trackflow.logic.commands.SortCommand.SortField;
import trackflow.logic.commands.SortCommand.SortOrder;
import trackflow.logic.commands.TagCommand;
import trackflow.logic.commands.UntagCommand;
import trackflow.logic.parser.exceptions.ParseException;
import trackflow.model.person.Person;
import trackflow.model.person.PersonContainsKeywordsPredicate;
import trackflow.model.person.Remark;
import trackflow.model.util.SampleDataUtil;
import trackflow.testutil.EditPersonDescriptorBuilder;
import trackflow.testutil.PersonBuilder;
import trackflow.testutil.PersonUtil;

public class TrackFlowParserTest {

    private final TrackFlowParser parser = new TrackFlowParser();

    @Test
    public void parseCommand_add() throws Exception {
        Person person = new PersonBuilder().withAddress("123 Main Street").withRemark("").withTags().build();
        String addCommandText = PersonUtil.getAddCommand(person);
        AddCommand command = (AddCommand) parser.parseCommand(addCommandText);
        assertEquals(new AddCommand(person), command);
        AddCommand uppercaseCommand = (AddCommand) parser.parseCommand("ADD" + addCommandText.substring(3));
        assertEquals(new AddCommand(person), uppercaseCommand);
    }

    @Test
    public void parseCommand_clear() throws Exception {
        assertTrue(parser.parseCommand(ClearCommand.COMMAND_WORD) instanceof ClearCommand);
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ClearCommand.MESSAGE_USAGE);
        assertThrows(ParseException.class, expectedMessage, () ->
                parser.parseCommand(ClearCommand.COMMAND_WORD + " 3"));
    }

    @Test
    public void parseCommand_delete() throws Exception {
        DeleteCommand command = (DeleteCommand) parser.parseCommand(
                DeleteCommand.COMMAND_WORD + " " + INDEX_FIRST_PERSON.getOneBased());
        assertEquals(new DeleteCommand(INDEX_FIRST_PERSON), command);
    }

    @Test
    public void parseCommand_edit() throws Exception {
        Person person = new PersonBuilder().build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(person).build();
        descriptor.setTags(null);
        EditCommand command = (EditCommand) parser.parseCommand(EditCommand.COMMAND_WORD + " "
                + INDEX_FIRST_PERSON.getOneBased() + " " + PersonUtil.getEditPersonDescriptorDetails(descriptor));
        assertEquals(new EditCommand(INDEX_FIRST_PERSON, descriptor), command);
    }

    @Test
    public void parseCommand_exit() throws Exception {
        assertTrue(parser.parseCommand(ExitCommand.COMMAND_WORD) instanceof ExitCommand);
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ExitCommand.MESSAGE_USAGE);
        assertThrows(ParseException.class, expectedMessage, () ->
                parser.parseCommand(ExitCommand.COMMAND_WORD + " 3"));
    }

    @Test
    public void parseCommand_find() throws Exception {
        List<String> keywords = List.of("foo", "bar", "baz");
        FindCommand command = (FindCommand) parser.parseCommand(
                FindCommand.COMMAND_WORD + " " + keywords.stream().collect(Collectors.joining(" ")));
        assertEquals(new FindCommand(new PersonContainsKeywordsPredicate(keywords)), command);
    }

    @Test
    public void parseCommand_help() throws Exception {
        assertTrue(parser.parseCommand(HelpCommand.COMMAND_WORD) instanceof HelpCommand);
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE);
        assertThrows(ParseException.class, expectedMessage, () ->
                parser.parseCommand(HelpCommand.COMMAND_WORD + " 3"));
    }

    @Test
    public void parseCommand_list() throws Exception {
        assertTrue(parser.parseCommand(ListAthleteCommand.COMMAND_WORD) instanceof ListAthleteCommand);
        assertTrue(parser.parseCommand("LIST") instanceof ListAthleteCommand);
        assertTrue(parser.parseCommand("  LiSt  ") instanceof ListAthleteCommand);
        assertThrows(ParseException.class, ListAthleteCommand.MESSAGE_USAGE, () -> parser.parseCommand("LiSt 3"));
    }

    @Test
    public void parseCommand_remark() throws Exception {
        final Remark remark = new Remark("Some remark.");
        RemarkCommand command = (RemarkCommand) parser.parseCommand(RemarkCommand.COMMAND_WORD + " "
                + INDEX_FIRST_PERSON.getOneBased() + " " + PREFIX_REMARK + remark.value);
        assertEquals(new RemarkCommand(INDEX_FIRST_PERSON, remark), command);
    }

    @Test
    public void parseCommand_tag() throws Exception {
        TagCommand command = (TagCommand) parser.parseCommand(TagCommand.COMMAND_WORD + " "
                + INDEX_FIRST_PERSON.getOneBased() + " " + PREFIX_TAG + "100m");
        assertEquals(new TagCommand(INDEX_FIRST_PERSON, SampleDataUtil.getTagSet("100m")), command);
    }

    @Test
    public void parseCommand_untag() throws Exception {
        UntagCommand command = (UntagCommand) parser.parseCommand(UntagCommand.COMMAND_WORD + " "
                + INDEX_FIRST_PERSON.getOneBased() + " " + PREFIX_TAG + "100m");
        assertEquals(new UntagCommand(INDEX_FIRST_PERSON, SampleDataUtil.getTagSet("100m")), command);
    }

    @Test
    public void parseCommand_sort() throws Exception {
        assertEquals(new SortCommand(SortField.AGE, SortOrder.DESCENDING), parser.parseCommand("SoRt age desc"));
    }

    @Test
    public void parseCommand_unrecognizedInput_throwsParseException() {
        assertThrows(ParseException.class, MESSAGE_EMPTY_COMMAND, () -> parser.parseCommand(""));
        assertThrows(ParseException.class, MESSAGE_EMPTY_COMMAND, () -> parser.parseCommand("   \t\n"));
    }

    @Test
    public void parseCommand_unknownCommand_throwsParseException() {
        String unknownCommand = "unknownCommand";
        String expectedMessage = String.format(MESSAGE_UNKNOWN_COMMAND, unknownCommand);
        assertThrows(ParseException.class, expectedMessage, () -> parser.parseCommand(unknownCommand + " arguments"));
    }
}
