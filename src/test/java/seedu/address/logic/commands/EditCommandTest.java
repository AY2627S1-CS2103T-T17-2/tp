package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.AgeCategory;
import seedu.address.model.person.AgeCategoryPredicate;
import seedu.address.model.person.Person;
import seedu.address.testutil.EditPersonDescriptorBuilder;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for EditCommand.
 */
public class EditCommandTest {

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_keepsPhoneButMatchesAnotherEmail_failure() {
        Person original = new PersonBuilder().withName("Avery Tan").build();
        Person other = new PersonBuilder(original).withPhone("81112222").withEmail("other@example.com").build();
        model = new ModelManager();
        model.addPerson(original);
        model.addPerson(other);
        model.updateFilteredPersonList(person -> person == original);
        EditCommand command = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withEmail("other@example.com").build());
        assertCommandFailure(command, model, EditCommand.MESSAGE_DUPLICATE_PERSON);
    }

    @Test
    public void execute_sharedContactWithDifferentName_successWithWarning() {
        Person original = new PersonBuilder().withName("Avery Tan").build();
        Person other = new PersonBuilder().withName("Jordan Lee").withPhone("81112222")
                .withEmail("other@example.com").build();
        model = new ModelManager();
        model.addPerson(original);
        model.addPerson(other);
        model.updateFilteredPersonList(person -> person == original);
        Person edited = new PersonBuilder(original).withEmail("OTHER@example.com").build();
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(original, edited);
        EditCommand command = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withEmail("OTHER@example.com").build());
        String expected = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.formatAthlete(edited))
                + String.format(Messages.MESSAGE_POSSIBLE_DUPLICATE, other.getName());
        assertCommandSuccess(command, model, expected, expectedModel);
    }

    @Test
    public void execute_allFieldsSpecifiedUnfilteredList_success() {
        Person editedPerson = new PersonBuilder(model.getFilteredPersonList().get(0))
                .withName(VALID_NAME_BOB).withAgeCategory("Under 16")
                .withPhone(VALID_PHONE_BOB).withEmail("bob@example.com").build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(editedPerson).build();
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON, descriptor);

        String expectedMessage = String.format(
                EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.formatAthlete(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(model.getFilteredPersonList().get(0), editedPerson);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_someFieldsSpecifiedUnfilteredList_success() {
        Index indexLastPerson = Index.fromOneBased(model.getFilteredPersonList().size());
        Person lastPerson = model.getFilteredPersonList().get(indexLastPerson.getZeroBased());

        PersonBuilder personInList = new PersonBuilder(lastPerson);
        Person editedPerson = personInList.withName(VALID_NAME_BOB).withPhone(VALID_PHONE_BOB).build();

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB)
                .withPhone(VALID_PHONE_BOB).build();
        EditCommand editCommand = new EditCommand(indexLastPerson, descriptor);

        String expectedMessage = String.format(
                EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.formatAthlete(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(lastPerson, editedPerson);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_noFieldSpecifiedUnfilteredList_success() {
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON, new EditPersonDescriptor());
        Person editedPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());

        String expectedMessage = String.format(
                EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.formatAthlete(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_filteredList_success() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        Person personInFilteredList = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person editedPerson = new PersonBuilder(personInFilteredList).withName(VALID_NAME_BOB).build();
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build());

        String expectedMessage = String.format(
                EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.formatAthlete(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(model.getFilteredPersonList().get(0), editedPerson);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_duplicatePersonUnfilteredList_failure() {
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(firstPerson).build();
        EditCommand editCommand = new EditCommand(INDEX_SECOND_PERSON, descriptor);

        assertCommandFailure(editCommand, model, EditCommand.MESSAGE_DUPLICATE_PERSON);
    }

    @Test
    public void execute_duplicatePersonFilteredList_failure() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        // edit person in filtered list into a duplicate in address book
        Person personInList = model.getAddressBook().getPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder(personInList).build());

        assertCommandFailure(editCommand, model, EditCommand.MESSAGE_DUPLICATE_PERSON);
    }

    @Test
    public void execute_invalidPersonIndexUnfilteredList_failure() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build();
        EditCommand editCommand = new EditCommand(outOfBoundIndex, descriptor);

        assertCommandFailure(editCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    /**
     * Edit filtered list where index is larger than size of filtered list,
     * but smaller than size of address book
     */
    @Test
    public void execute_invalidPersonIndexFilteredList_failure() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        Index outOfBoundIndex = INDEX_SECOND_PERSON;
        // ensures that outOfBoundIndex is still in bounds of address book list
        assertTrue(outOfBoundIndex.getZeroBased() < model.getAddressBook().getPersonList().size());

        EditCommand editCommand = new EditCommand(outOfBoundIndex,
                new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build());

        assertCommandFailure(editCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_tagsOnly_replacesAndClearsWhilePreservingOtherDetails() throws Exception {
        Person original = model.getFilteredPersonList().get(0);
        new EditCommand(INDEX_FIRST_PERSON, new EditPersonDescriptorBuilder()
                .withTags("sprinter", "relay").build()).execute(model);
        Person edited = model.getFilteredPersonList().get(0);
        assertEquals(new PersonBuilder(original).withTags("sprinter", "relay").build(), edited);
        assertEquals(original.getRemark(), edited.getRemark());
        new EditCommand(INDEX_FIRST_PERSON, new EditPersonDescriptorBuilder().withTags().build()).execute(model);
        Person cleared = model.getFilteredPersonList().get(0);
        assertEquals(new PersonBuilder(original).withTags().build(), cleared);
        assertEquals(original.getRemark(), cleared.getRemark());
    }

    @Test
    public void execute_addressOnly_preservesOtherDetails() throws Exception {
        Person original = model.getFilteredPersonList().get(0);
        new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withAddress("456 Main Street").build()).execute(model);
        Person edited = model.getFilteredPersonList().get(0);
        assertEquals(new PersonBuilder(original).withAddress("456 Main Street").build(), edited);
        assertEquals(original.getRemark(), edited.getRemark());
    }

    @Test
    public void execute_ageCategoryOnly_preservesOtherDetails() throws Exception {
        Person original = new PersonBuilder().withAddress("123 Main Street")
                .withTags("sprinter").withRemark("Personal best: 12.34s").build();
        model.addPerson(original);
        Index index = Index.fromOneBased(model.getFilteredPersonList().size());
        EditCommand command = new EditCommand(index,
                new EditPersonDescriptorBuilder().withAgeCategory("Under 14").build());
        command.execute(model);
        Person edited = model.getFilteredPersonList().get(index.getZeroBased());
        assertEquals(new PersonBuilder(original).withAgeCategory("Under 14").build(), edited);
        // Person.equals does not compare remarks, so check it explicitly.
        assertEquals(original.getRemark(), edited.getRemark());
    }

    @Test
    public void execute_ageFilteredList_usesDisplayedIndexAndShowsAll() throws Exception {
        Person original = new PersonBuilder().withAgeCategory("Under 14").build();
        model.addPerson(original);
        model.updateFilteredPersonList(new AgeCategoryPredicate(new AgeCategory("Under 14")));
        assertEquals(List.of(original), model.getFilteredPersonList());
        new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withAgeCategory("Under 16").build()).execute(model);
        assertEquals(model.getAddressBook().getPersonList(), model.getFilteredPersonList());
        assertTrue(model.getFilteredPersonList().contains(
                new PersonBuilder(original).withAgeCategory("Under 16").build()));
    }

    @Test
    public void execute_contactChangeCreatesNormalizedDuplicate_failure() {
        Person first = new PersonBuilder().withAgeCategory("Under 14").withTags("sprinter").build();
        Person second = new PersonBuilder(first).withName("AMY   BEE")
                .withPhone("81112222").withEmail("other@example.com")
                .withAgeCategory("Under 16").withRemark("Keep me").build();
        model.addPerson(first);
        model.addPerson(second);
        model.updateFilteredPersonList(new AgeCategoryPredicate(new AgeCategory("Under 16")));
        EditCommand command = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withEmail(first.getEmail().value.toUpperCase()).build());
        assertCommandFailure(command, model, EditCommand.MESSAGE_DUPLICATE_PERSON);
        assertEquals(second.getRemark(), model.getFilteredPersonList().get(0).getRemark());
    }

    @Test
    public void execute_sameValues_success() throws Exception {
        Person original = model.getFilteredPersonList().get(0);
        new EditCommand(INDEX_FIRST_PERSON, new EditPersonDescriptorBuilder(original).build()).execute(model);
        assertEquals(original, model.getFilteredPersonList().get(0));
        assertEquals(original.getRemark(), model.getFilteredPersonList().get(0).getRemark());
    }

    @Test
    public void equals() {
        final EditCommand standardCommand = new EditCommand(INDEX_FIRST_PERSON, DESC_AMY);

        // same values -> returns true
        EditPersonDescriptor copyDescriptor = new EditPersonDescriptor(DESC_AMY);
        EditCommand commandWithSameValues = new EditCommand(INDEX_FIRST_PERSON, copyDescriptor);
        assertTrue(standardCommand.equals(commandWithSameValues));

        // same object -> returns true
        assertTrue(standardCommand.equals(standardCommand));

        // null -> returns false
        assertFalse(standardCommand.equals(null));

        // different types -> returns false
        assertFalse(standardCommand.equals(new ClearCommand()));

        // different index -> returns false
        assertFalse(standardCommand.equals(new EditCommand(INDEX_SECOND_PERSON, DESC_AMY)));

        // different descriptor -> returns false
        assertFalse(standardCommand.equals(new EditCommand(INDEX_FIRST_PERSON, DESC_BOB)));
    }

    @Test
    public void toStringMethod() {
        Index index = Index.fromOneBased(1);
        EditPersonDescriptor editPersonDescriptor = new EditPersonDescriptor();
        EditCommand editCommand = new EditCommand(index, editPersonDescriptor);
        String expected = EditCommand.class.getCanonicalName() + "{index=" + index + ", editPersonDescriptor="
                + editPersonDescriptor + "}";
        assertEquals(expected, editCommand.toString());
    }

}
