package trackflow.ui;

import java.util.List;

import javafx.geometry.Rectangle2D;

/**
 * Decides whether a saved window position can still be used on the screens that are currently connected.
 */
final class WindowPlacement {

    /** Width of the window, in pixels, that must be visible so that the title bar can still be grabbed. */
    static final double MIN_VISIBLE_WIDTH = 100;

    /** Height of the window, in pixels, that must be visible below the top edge. */
    static final double MIN_VISIBLE_HEIGHT = 50;

    /** Slack allowed above a screen's top edge (some platforms place maximized windows slightly above it). */
    private static final double TOP_TOLERANCE = 10;

    private WindowPlacement() {
    }

    /**
     * Returns true if a window of the given size placed at ({@code x}, {@code y}) has its title bar
     * reachable on at least one of {@code screens}. That means the top edge is on the screen, and enough
     * of the window's width and height is on the same screen to grab it.
     */
    static boolean isReachable(double x, double y, double width, double height, List<Rectangle2D> screens) {
        double requiredWidth = Math.min(width, MIN_VISIBLE_WIDTH);
        double requiredHeight = Math.min(height, MIN_VISIBLE_HEIGHT);
        for (Rectangle2D screen : screens) {
            double visibleWidth = Math.min(x + width, screen.getMaxX()) - Math.max(x, screen.getMinX());
            boolean topOnScreen = y >= screen.getMinY() - TOP_TOLERANCE;
            boolean enoughHeight = y + requiredHeight <= screen.getMaxY();
            if (visibleWidth >= requiredWidth && topOnScreen && enoughHeight) {
                return true;
            }
        }
        return false;
    }
}
