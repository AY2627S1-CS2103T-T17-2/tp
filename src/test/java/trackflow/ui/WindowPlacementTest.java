package trackflow.ui;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.geometry.Rectangle2D;

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
}
