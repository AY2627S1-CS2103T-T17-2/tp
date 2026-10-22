package trackflow.ui;

import javafx.stage.Stage;

/**
 * API of the UI component.
 */
public interface Ui {

    /** Starts the UI (and the App).  */
    void start(Stage primaryStage);

    /** Shows {@code error} in a dialog and shuts down, for when the app cannot start normally. */
    void startWithError(Stage primaryStage, Throwable error);

}
