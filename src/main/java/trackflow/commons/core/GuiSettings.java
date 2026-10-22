package trackflow.commons.core;

import static trackflow.commons.util.AppUtil.checkArgument;

import java.awt.Point;
import java.io.Serializable;
import java.util.Objects;

import trackflow.commons.util.ToStringBuilder;

/**
 * A Serializable class that contains the GUI settings.
 * Guarantees: immutable.
 */
public class GuiSettings implements Serializable {

    private static final double DEFAULT_HEIGHT = 600;
    private static final double DEFAULT_WIDTH = 740;

    private final double windowWidth;
    private final double windowHeight;
    private final Point windowCoordinates;

    /**
     * Constructs a {@code GuiSettings} with the default height, width and position.
     */
    public GuiSettings() {
        windowWidth = DEFAULT_WIDTH;
        windowHeight = DEFAULT_HEIGHT;
        windowCoordinates = null; // null represent no coordinates
    }

    /**
     * Constructs a {@code GuiSettings} with the specified height, width and position.
     *
     * @throws IllegalArgumentException If a dimension is nonpositive or nonfinite.
     */
    public GuiSettings(double windowWidth, double windowHeight, int xPosition, int yPosition) {
        checkArgument(isValidSize(windowWidth, windowHeight), "Window dimensions must be finite and positive");
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
        windowCoordinates = new Point(xPosition, yPosition);
    }

    public double getWindowWidth() {
        return windowWidth;
    }

    public double getWindowHeight() {
        return windowHeight;
    }

    public Point getWindowCoordinates() {
        return windowCoordinates != null ? new Point(windowCoordinates) : null;
    }

    /**
     * Checks dimensions after deserialization, which can bypass constructor validation.
     * Null coordinates request automatic placement; integer coordinates are fitted to actual screens by the UI.
     *
     * @return Whether both dimensions are finite and positive.
     */
    public boolean isValid() {
        return isValidSize(windowWidth, windowHeight);
    }

    private static boolean isValidSize(double width, double height) {
        return Double.isFinite(width) && width > 0 && Double.isFinite(height) && height > 0;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof GuiSettings otherGuiSettings)) {
            return false;
        }

        return windowWidth == otherGuiSettings.windowWidth
                && windowHeight == otherGuiSettings.windowHeight
                && Objects.equals(windowCoordinates, otherGuiSettings.windowCoordinates);
    }

    @Override
    public int hashCode() {
        return Objects.hash(windowWidth, windowHeight, windowCoordinates);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("windowWidth", windowWidth)
                .add("windowHeight", windowHeight)
                .add("windowCoordinates", windowCoordinates)
                .toString();
    }
}
