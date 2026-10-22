package trackflow.commons.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static trackflow.testutil.Assert.assertThrows;

import java.awt.Point;

import org.junit.jupiter.api.Test;

public class GuiSettingsTest {
    @Test
    public void constructor_invalidDimensions_throwsIllegalArgumentException() {
        for (double dimension : new double[] {0, -1, Double.NaN, Double.POSITIVE_INFINITY,
            Double.NEGATIVE_INFINITY}) {
            assertThrows(IllegalArgumentException.class, () -> new GuiSettings(dimension, 600, 0, 0));
            assertThrows(IllegalArgumentException.class, () -> new GuiSettings(740, dimension, 0, 0));
        }
    }

    @Test
    public void getWindowCoordinates_defensiveCopy_doesNotMutateInternalState() {
        GuiSettings guiSettings = new GuiSettings(800, 600, 10, 20);
        Point coordinates = guiSettings.getWindowCoordinates();
        coordinates.setLocation(999, 999);
        assertEquals(new Point(10, 20), guiSettings.getWindowCoordinates());
    }

    @Test
    public void equals() {
        GuiSettings defaultSettings = new GuiSettings();
        GuiSettings explicitSettings = new GuiSettings(800, 600, 10, 20);
        GuiSettings explicitSettingsCopy = new GuiSettings(800, 600, 10, 20);

        assertTrue(defaultSettings.equals(defaultSettings));
        assertTrue(explicitSettings.equals(explicitSettingsCopy));
        assertEquals(explicitSettings.hashCode(), explicitSettingsCopy.hashCode());

        assertFalse(defaultSettings.equals(null));
        assertFalse(defaultSettings.equals("not a GuiSettings"));
        assertFalse(defaultSettings.equals(explicitSettings));
        assertFalse(explicitSettings.equals(new GuiSettings(900, 600, 10, 20)));
        assertFalse(explicitSettings.equals(new GuiSettings(800, 700, 10, 20)));
        assertFalse(explicitSettings.equals(new GuiSettings(800, 600, 30, 20)));
    }

    @Test
    public void toStringMethod() {
        GuiSettings guiSettings = new GuiSettings();
        String expected = GuiSettings.class.getCanonicalName() + "{windowWidth=" + guiSettings.getWindowWidth()
                + ", windowHeight=" + guiSettings.getWindowHeight() + ", windowCoordinates="
                + guiSettings.getWindowCoordinates() + "}";
        assertEquals(expected, guiSettings.toString());
    }
}
