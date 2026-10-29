package trackflow.ui;

import java.nio.file.Path;
import java.nio.file.Paths;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
// @@author ken2k7
import trackflow.model.person.Person;
// @@author

/**
 * A UI for the status bar that is displayed at the footer of the application.
 */
public class StatusBarFooter extends UiPart<Region> {

    public static final String NO_CHANGES_STATUS = "No unsaved changes";
    public static final String SAVED_STATUS = "All changes saved";
    public static final String NOT_SAVED_STATUS = "Changes not saved";

    private static final String FXML = "StatusBarFooter.fxml";
    private static final String SAVED_STYLE_CLASS = "status-saved";
    private static final String NOT_SAVED_STYLE_CLASS = "status-not-saved";

    @FXML
    private Label saveStateStatus;

    @FXML
    private Label saveLocationStatus;

    @FXML
    private Label athleteCountStatus;

    /**
     * Creates a {@code StatusBarFooter} showing the given {@code Path} and the athlete count, kept up to date as
     * the lists change. The count is shown as "X of Y athletes" while a find or filter is hiding some athletes,
     * so an active selection stays visible even after later commands, and as "Y athletes" when the whole roster
     * is displayed.
     */
    public StatusBarFooter(Path saveLocation, ObservableList<Person> displayedPersons,
            ObservableList<Person> fullRoster) {
        super(FXML);
        saveLocationStatus.setText(Paths.get(".").resolve(saveLocation).toString());
        saveStateStatus.setText(NO_CHANGES_STATUS);
        Runnable updateCount = () -> showAthleteCount(displayedPersons.size(), fullRoster.size());
        updateCount.run();
        displayedPersons.addListener((ListChangeListener<Person>) change -> updateCount.run());
        fullRoster.addListener((ListChangeListener<Person>) change -> updateCount.run());
    }

    /**
     * Indicates that all changes have been saved to the data file.
     */
    public void showSaved() {
        setSaveState(SAVED_STATUS, SAVED_STYLE_CLASS);
    }

    /**
     * Indicates that the latest changes could not be saved to the data file.
     */
    public void showNotSaved() {
        setSaveState(NOT_SAVED_STATUS, NOT_SAVED_STYLE_CLASS);
    }

    /**
     * Resets the save state to the neutral status if the last save had failed. A failed save is rolled back,
     * so nothing is left unsaved once a later command succeeds.
     */
    public void clearNotSaved() {
        if (getRoot().getStyleClass().contains(NOT_SAVED_STYLE_CLASS)) {
            getRoot().getStyleClass().remove(NOT_SAVED_STYLE_CLASS);
            saveStateStatus.setText(NO_CHANGES_STATUS);
        }
    }

    private void setSaveState(String status, String styleClass) {
        getRoot().getStyleClass().removeAll(SAVED_STYLE_CLASS, NOT_SAVED_STYLE_CLASS);
        getRoot().getStyleClass().add(styleClass);
        saveStateStatus.setText(status);
    }

    private void showAthleteCount(int shown, int total) {
        String noun = total == 1 ? "athlete" : "athletes";
        athleteCountStatus.setText(shown == total
                ? total + " " + noun
                : shown + " of " + total + " " + noun);
    }

}
