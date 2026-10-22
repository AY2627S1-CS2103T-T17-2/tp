package trackflow.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.geometry.Rectangle2D;
import trackflow.commons.core.GuiSettings;

public class WindowPlacementTest {

    private static final Rectangle2D PRIMARY = new Rectangle2D(0, 0, 1440, 900);
    private static final Rectangle2D SECOND = new Rectangle2D(-1920, 0, 1920, 1080);
    private static final List<Rectangle2D> PRIMARY_ONLY = List.of(PRIMARY);
    private static final List<Rectangle2D> BOTH = List.of(PRIMARY, SECOND);

    private static final double WIDTH = 740;
    private static final double HEIGHT = 600;

    @Test
    public void isReachable_onPrimaryScreen_true() {
        assertTrue(WindowPlacement.isReachable(100, 100, WIDTH, HEIGHT, PRIMARY_ONLY));
    }

    @Test
    public void isReachable_onSecondScreenButOnlyPrimaryConnected_false() {
        assertFalse(WindowPlacement.isReachable(-1920, 100, WIDTH, HEIGHT, PRIMARY_ONLY));
    }

    @Test
    public void isReachable_onSecondScreenWithBothConnected_true() {
        assertTrue(WindowPlacement.isReachable(-1920, 100, WIDTH, HEIGHT, BOTH));
    }

    @Test
    public void isReachable_topToleranceBoundary() {
        assertTrue(WindowPlacement.isReachable(100, -10, WIDTH, HEIGHT, PRIMARY_ONLY));
        assertFalse(WindowPlacement.isReachable(100, -11, WIDTH, HEIGHT, PRIMARY_ONLY));
    }

    @Test
    public void isReachable_minimumVisibleHeightBoundary() {
        assertTrue(WindowPlacement.isReachable(100, 850, WIDTH, HEIGHT, PRIMARY_ONLY));
        assertFalse(WindowPlacement.isReachable(100, 851, WIDTH, HEIGHT, PRIMARY_ONLY));
    }

    @Test
    public void isReachable_aboveTopEdge_false() {
        assertFalse(WindowPlacement.isReachable(100, -500, WIDTH, HEIGHT, PRIMARY_ONLY));
    }

    @Test
    public void isReachable_belowBottomEdge_false() {
        assertFalse(WindowPlacement.isReachable(100, 900, WIDTH, HEIGHT, PRIMARY_ONLY));
    }

    @Test
    public void isReachable_exactlyMinimumVisibleWidth_true() {
        assertTrue(WindowPlacement.isReachable(1340, 100, WIDTH, HEIGHT, PRIMARY_ONLY));
    }

    @Test
    public void isReachable_lessThanMinimumVisibleWidth_false() {
        assertFalse(WindowPlacement.isReachable(1400, 100, WIDTH, HEIGHT, PRIMARY_ONLY));
    }

    @Test
    public void isReachable_noScreens_false() {
        assertFalse(WindowPlacement.isReachable(100, 100, WIDTH, HEIGHT, List.of()));
    }

    @Test
    public void isReachable_windowNarrowerThanMinimumVisibleWidth_true() {
        assertTrue(WindowPlacement.isReachable(0, 0, 80, HEIGHT, PRIMARY_ONLY));
    }

    @Test
    public void isReachable_nanCoordinate_false() {
        assertFalse(WindowPlacement.isReachable(Double.NaN, 100, WIDTH, HEIGHT, PRIMARY_ONLY));
    }

    @Test
    public void fit_validSavedBounds_preservesSizeAndPosition() {
        assertEquals(new Rectangle2D(100, 100, WIDTH, HEIGHT),
                WindowPlacement.fit(new GuiSettings(WIDTH, HEIGHT, 100, 100), BOTH, PRIMARY));
    }

    @Test
    public void fit_oversizedReachableWindow_fitsEntirePrimaryScreen() {
        assertEquals(PRIMARY, WindowPlacement.fit(new GuiSettings(5000, 4000, 100, 100), BOTH, PRIMARY));
    }

    @Test
    public void fit_smallUsableScreen_doesNotForceSixHundredPixelHeight() {
        Rectangle2D usable = new Rectangle2D(0, 25, 853, 440);
        assertEquals(usable, WindowPlacement.fit(new GuiSettings(900, 600, 0, 25), List.of(usable), usable));
    }

    @Test
    public void fit_shortSavedWindow_keepsFourHundredPixelHeight() {
        assertEquals(new Rectangle2D(100, 100, WIDTH, 400),
                WindowPlacement.fit(new GuiSettings(WIDTH, 400, 100, 100), BOTH, PRIMARY));
    }

    @Test
    public void fit_windowBelowNormalMinimum_usesMinimumWhereSpaceAllows() {
        assertEquals(new Rectangle2D(100, 100, 450, 400),
                WindowPlacement.fit(new GuiSettings(100, 100, 100, 100), BOTH, PRIMARY));
    }

    @Test
    public void fit_screenSmallerThanMinimum_prioritizesScreenBounds() {
        Rectangle2D tiny = new Rectangle2D(0, 25, 400, 350);
        assertEquals(tiny, WindowPlacement.fit(new GuiSettings(), List.of(tiny), tiny));
    }

    @Test
    public void fit_removedMonitor_centersOnPrimary() {
        assertEquals(new Rectangle2D(350, 150, WIDTH, HEIGHT),
                WindowPlacement.fit(new GuiSettings(WIDTH, HEIGHT, -1920, 100), PRIMARY_ONLY, PRIMARY));
    }

    @Test
    public void fit_oversizedWindowOnSecondary_fitsSecondaryWithNegativeCoordinates() {
        assertEquals(SECOND, WindowPlacement.fit(new GuiSettings(3000, 2000, -1920, 100), BOTH, PRIMARY));
    }

    @Test
    public void fit_windowPartlyPastScreenEdges_movesWholeWindowInside() {
        assertEquals(new Rectangle2D(700, 300, WIDTH, HEIGHT),
                WindowPlacement.fit(new GuiSettings(WIDTH, HEIGHT, 1340, 850), PRIMARY_ONLY, PRIMARY));
    }

    @Test
    public void fit_windowSpanningScreens_selectsScreenWithMostOverlap() {
        assertEquals(new Rectangle2D(-740, 100, WIDTH, HEIGHT),
                WindowPlacement.fit(new GuiSettings(WIDTH, HEIGHT, -500, 100), BOTH, PRIMARY));
    }

    @Test
    public void fit_helpWindow_usesItsOwnMinimumDimensions() {
        assertEquals(new Rectangle2D(100, 100, 480, 320), WindowPlacement.fit(
                new GuiSettings(480, 320, 100, 100), BOTH, PRIMARY, 480, 320));
        Rectangle2D usable = new Rectangle2D(0, 25, 853, 440);
        assertEquals(new Rectangle2D(0, 25, 760, 440), WindowPlacement.fit(
                new GuiSettings(760, 680, 0, 25), List.of(usable), usable, 480, 320));
    }
}
