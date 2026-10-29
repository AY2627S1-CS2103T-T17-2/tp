package trackflow.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static trackflow.model.Model.PREDICATE_SHOW_ALL_PERSONS;
import static trackflow.testutil.Assert.assertThrows;
import static trackflow.testutil.TypicalPersons.ALICE;
import static trackflow.testutil.TypicalPersons.BENSON;

import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.Test;

import trackflow.commons.core.GuiSettings;
import trackflow.model.person.PersonContainsKeywordsPredicate;
import trackflow.testutil.AthleteRosterBuilder;

public class ModelManagerTest {

    private ModelManager modelManager = new ModelManager();

    @Test
    public void constructor() {
        assertEquals(new UserPrefs(), modelManager.getUserPrefs());
        assertEquals(new GuiSettings(), modelManager.getGuiSettings());
        assertEquals(new AthleteRoster(), new AthleteRoster(modelManager.getRoster()));
    }

    @Test
    public void constructor_validUserPrefs_copiesUserPrefs() {
        UserPrefs userPrefs = new UserPrefs();
        userPrefs.setGuiSettings(new GuiSettings(1, 2, 3, 4));
        modelManager = new ModelManager(new AthleteRoster(), userPrefs);
        assertEquals(userPrefs, modelManager.getUserPrefs());

        // Modifying userPrefs should not modify modelManager's userPrefs
        UserPrefs oldUserPrefs = new UserPrefs(userPrefs);
        userPrefs.setGuiSettings(new GuiSettings(5, 6, 7, 8));
        assertEquals(oldUserPrefs, modelManager.getUserPrefs());
    }

    @Test
    public void setGuiSettings_nullGuiSettings_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.setGuiSettings(null));
    }

    @Test
    public void setGuiSettings_validGuiSettings_setsGuiSettings() {
        GuiSettings guiSettings = new GuiSettings(1, 2, 3, 4);
        modelManager.setGuiSettings(guiSettings);
        assertEquals(guiSettings, modelManager.getGuiSettings());
    }

    @Test
    public void hasPerson_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.hasPerson(null));
    }

    @Test
    public void hasPerson_personNotInAthleteRoster_returnsFalse() {
        assertFalse(modelManager.hasPerson(ALICE));
    }

    @Test
    public void hasPerson_personInAthleteRoster_returnsTrue() {
        modelManager.addPerson(ALICE);
        assertTrue(modelManager.hasPerson(ALICE));
    }

    @Test
    public void getFilteredPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> modelManager.getFilteredPersonList().remove(0));
    }

    @Test
    public void updateSortedPersonList_nullComparator_restoresRosterOrder() {
        modelManager = new ModelManager(
                new AthleteRosterBuilder().withPerson(BENSON).withPerson(ALICE).build(), new UserPrefs());
        modelManager.updateSortedPersonList(
                Comparator.comparing(person -> person.getName().fullName, String.CASE_INSENSITIVE_ORDER));

        modelManager.updateSortedPersonList(null);

        assertNull(modelManager.getPersonListComparator());
        assertEquals(List.of(BENSON, ALICE), modelManager.getFilteredPersonList());
    }

    @Test
    public void updateSortedPersonList_validComparator_sortsDisplayedList() {
        modelManager = new ModelManager(
                new AthleteRosterBuilder().withPerson(BENSON).withPerson(ALICE).build(), new UserPrefs());
        modelManager.updateSortedPersonList(
                Comparator.comparing(person -> person.getName().fullName, String.CASE_INSENSITIVE_ORDER));
        assertEquals(List.of(ALICE, BENSON), modelManager.getFilteredPersonList());
    }

    @Test
    public void equals() {
        AthleteRoster roster = new AthleteRosterBuilder().withPerson(ALICE).withPerson(BENSON).build();
        AthleteRoster differentRoster = new AthleteRoster();
        UserPrefs userPrefs = new UserPrefs();

        // same values -> returns true
        modelManager = new ModelManager(roster, userPrefs);
        ModelManager modelManagerCopy = new ModelManager(roster, userPrefs);
        assertTrue(modelManager.equals(modelManagerCopy));

        // same object -> returns true
        assertTrue(modelManager.equals(modelManager));

        // null -> returns false
        assertFalse(modelManager.equals(null));

        // different types -> returns false
        assertFalse(modelManager.equals(5));

        // different roster -> returns false
        assertFalse(modelManager.equals(new ModelManager(differentRoster, userPrefs)));

        // different filteredList -> returns false
        String[] keywords = ALICE.getName().fullName.split("\\s+");
        modelManager.updateFilteredPersonList(new PersonContainsKeywordsPredicate(List.of(keywords)));
        assertFalse(modelManager.equals(new ModelManager(roster, userPrefs)));

        // resets modelManager to initial state for upcoming tests
        modelManager.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);

        // different userPrefs -> returns false
        UserPrefs differentUserPrefs = new UserPrefs();
        differentUserPrefs.setGuiSettings(new GuiSettings(1, 2, 3, 4));
        assertFalse(modelManager.equals(new ModelManager(roster, differentUserPrefs)));
    }
}
