package seedu.address.ui;

import java.nio.file.Path;
import java.nio.file.Paths;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import seedu.address.model.person.Person;

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
     * Creates a {@code StatusBarFooter} showing the given {@code Path} and the number of athletes in
     * {@code displayedPersons}, which is kept up to date as the list changes.
     */
    public StatusBarFooter(Path saveLocation, ObservableList<Person> displayedPersons) {
        super(FXML);
        saveLocationStatus.setText(Paths.get(".").resolve(saveLocation).toString());
        saveStateStatus.setText(NO_CHANGES_STATUS);
        showAthleteCount(displayedPersons.size());
        displayedPersons.addListener((ListChangeListener<Person>) change ->
                showAthleteCount(displayedPersons.size()));
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

    private void setSaveState(String status, String styleClass) {
        getRoot().getStyleClass().removeAll(SAVED_STYLE_CLASS, NOT_SAVED_STYLE_CLASS);
        getRoot().getStyleClass().add(styleClass);
        saveStateStatus.setText(status);
    }

    private void showAthleteCount(int count) {
        athleteCountStatus.setText(count == 1 ? "1 athlete shown" : count + " athletes shown");
    }

}
