package trackflow.commons.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static trackflow.testutil.Assert.assertThrows;

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
    public void toStringMethod() {
        GuiSettings guiSettings = new GuiSettings();
        String expected = GuiSettings.class.getCanonicalName() + "{windowWidth=" + guiSettings.getWindowWidth()
                + ", windowHeight=" + guiSettings.getWindowHeight() + ", windowCoordinates="
                + guiSettings.getWindowCoordinates() + "}";
        assertEquals(expected, guiSettings.toString());
    }
}
