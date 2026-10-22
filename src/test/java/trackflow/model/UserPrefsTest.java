package trackflow.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static trackflow.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import trackflow.commons.core.GuiSettings;

public class UserPrefsTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new UserPrefs(null));
    }

    @Test
    public void setGuiSettings_nullGuiSettings_throwsNullPointerException() {
        UserPrefs userPref = new UserPrefs();
        assertThrows(NullPointerException.class, () -> userPref.setGuiSettings(null));
    }

    @Test
    public void equals() {
        UserPrefs userPrefs = new UserPrefs();
        UserPrefs copy = new UserPrefs(userPrefs);

        assertTrue(userPrefs.equals(userPrefs));
        assertTrue(userPrefs.equals(copy));
        assertEquals(userPrefs.hashCode(), copy.hashCode());
        assertEquals("Gui Settings : " + userPrefs.getGuiSettings(), userPrefs.toString());

        assertFalse(userPrefs.equals(null));
        assertFalse(userPrefs.equals("not a UserPrefs"));

        UserPrefs different = new UserPrefs();
        different.setGuiSettings(new GuiSettings(800, 600, 0, 0));
        assertFalse(userPrefs.equals(different));
    }

}
