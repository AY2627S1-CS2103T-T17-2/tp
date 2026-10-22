package trackflow.ui;

import java.util.List;

import javafx.geometry.Rectangle2D;
import trackflow.commons.core.GuiSettings;

/**
 * Fits saved window bounds to usable screens and checks whether a title bar remains reachable.
 */
final class WindowPlacement {

    /** Preferred minimum outer width where the screen has enough space. */
    static final double MIN_WINDOW_WIDTH = 450;

    /** Preferred minimum outer height where the screen has enough space. */
    static final double MIN_WINDOW_HEIGHT = 400;

    /** Width of the window, in pixels, that must be visible so that the title bar can still be grabbed. */
    static final double MIN_VISIBLE_WIDTH = 100;

    /** Height of the window, in pixels, that must be visible below the top edge. */
    static final double MIN_VISIBLE_HEIGHT = 50;

    /** Slack allowed above a screen's top edge (some platforms place maximized windows slightly above it). */
    private static final double TOP_TOLERANCE = 10;

    private WindowPlacement() {
    }

    /**
     * Returns outer window bounds fitted to a connected screen's usable area, including space for decorations.
     * Restores reachable positions on the screen with the largest overlap, otherwise centers on the primary screen.
     * Keeps the preferred minimum size only where the screen has enough usable space.
     */
    static Rectangle2D fit(GuiSettings settings, List<Rectangle2D> screens, Rectangle2D primary) {
        return fit(settings, screens, primary, MIN_WINDOW_WIDTH, MIN_WINDOW_HEIGHT);
    }

    /**
     * Returns fitted outer bounds using the given window's preferred minimum dimensions.
     */
    static Rectangle2D fit(GuiSettings settings, List<Rectangle2D> screens, Rectangle2D primary,
            double minimumWidth, double minimumHeight) {
        GuiSettings defaults = new GuiSettings();
        double width = validSizeOrDefault(settings.getWindowWidth(), defaults.getWindowWidth());
        double height = validSizeOrDefault(settings.getWindowHeight(), defaults.getWindowHeight());
        var coordinates = settings.getWindowCoordinates();
        Rectangle2D target = primary;
        boolean restorePosition = false;
        double largestOverlap = 0;
        if (coordinates != null) {
            for (Rectangle2D screen : screens) {
                if (!isReachable(coordinates.getX(), coordinates.getY(), width, height, List.of(screen))) {
                    continue;
                }
                double overlapWidth = Math.min(coordinates.getX() + width, screen.getMaxX())
                        - Math.max(coordinates.getX(), screen.getMinX());
                double overlapHeight = Math.min(coordinates.getY() + height, screen.getMaxY())
                        - Math.max(coordinates.getY(), screen.getMinY());
                double overlap = overlapWidth * overlapHeight;
                if (overlap > largestOverlap) {
                    target = screen;
                    largestOverlap = overlap;
                    restorePosition = true;
                }
            }
        }
        width = Math.min(Math.max(width, minimumWidth), target.getWidth());
        height = Math.min(Math.max(height, minimumHeight), target.getHeight());
        double x = restorePosition ? coordinates.getX() : target.getMinX() + (target.getWidth() - width) / 2;
        double y = restorePosition ? coordinates.getY() : target.getMinY() + (target.getHeight() - height) / 2;
        return new Rectangle2D(clamp(x, target.getMinX(), target.getMaxX() - width),
                clamp(y, target.getMinY(), target.getMaxY() - height), width, height);
    }

    /**
     * Returns a finite positive size or the given default.
     */
    private static double validSizeOrDefault(double size, double defaultSize) {
        return Double.isFinite(size) && size > 0 ? size : defaultSize;
    }

    /**
     * Returns the value constrained to the inclusive range.
     */
    private static double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(value, maximum));
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
