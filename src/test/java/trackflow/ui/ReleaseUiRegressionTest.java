package trackflow.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextArea;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import javafx.stage.Window;
import trackflow.model.person.Person;
import trackflow.testutil.PersonBuilder;

/**
 * Verifies the release feedback and edit-visibility fixes in real JavaFX windows.
 */
public class ReleaseUiRegressionTest {
    private static boolean available;
    private final List<Stage> stages = new ArrayList<>();

    @BeforeAll
    public static void initJavaFx() throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }
        CountDownLatch ready = new CountDownLatch(1);
        try {
            Platform.startup(ready::countDown);
            available = ready.await(5, TimeUnit.SECONDS);
        } catch (IllegalStateException e) {
            available = true;
        }
        if (available) {
            Platform.setImplicitExit(false);
        }
    }

    @AfterEach
    public void closeWindows() throws Exception {
        if (available) {
            onFx(() -> {
                stages.forEach(Stage::close);
                return null;
            });
        }
    }

    @Test
    public void expandFeedback_showsCompleteCopyableRecoveryPath_andResetsScroll() throws Exception {
        Assumptions.assumeTrue(available, "JavaFX display unavailable");
        onFx(() -> {
            ResultDisplay display = new ResultDisplay();
            Stage stage = show(new Scene(display.getRoot(), 700, 100));
            String message = "TrackFlow could not read your saved data.\n" + "Recovery instructions\n".repeat(8)
                    + "Backup: /a/long/path/trackflow.json.unreadable-123.bak";
            display.showError(message);
            Button expand = (Button) display.getRoot().lookup(".button");
            expand.fire();
            Stage details = (Stage) Window.getWindows().stream()
                    .filter(window -> window != stage && window instanceof Stage && window.isShowing())
                    .findFirst().orElseThrow();
            stages.add(details);
            TextArea fullText = (TextArea) details.getScene().getRoot().lookup(".text-area");
            assertEquals(message, fullText.getText());
            assertFalse(fullText.isEditable());
            assertTrue(details.isResizable());
            screenshot(details, "expanded-recovery");
            TextArea feedback = (TextArea) display.getRoot().lookup(".text-area");
            feedback.positionCaret(feedback.getLength());
            display.showError("First line\nSecond line\nThird line");
            assertEquals(0, feedback.getCaretPosition());
            screenshot(stage, "feedback");
            return null;
        });
    }

    @Test
    public void editedAthlete_isVisibleAfterMovingToEndOfList() throws Exception {
        Assumptions.assumeTrue(available, "JavaFX display unavailable");
        ObservableList<Person> persons = FXCollections.observableArrayList();
        for (int index = 0; index < 40; index++) {
            persons.add(new PersonBuilder().withName("Athlete " + index).build());
        }
        Person updated = new PersonBuilder(persons.get(0)).withName("Updated Athlete").build();
        PersonListPanel panel = onFx(() -> {
            PersonListPanel result = new PersonListPanel(persons, persons);
            show(new Scene(result.getRoot(), 650, 400));
            return result;
        });
        awaitPulses();
        onFx(() -> {
            // Models publish a replacement and may reposition it under the active comparator.
            persons.remove(0);
            persons.add(updated);
            panel.highlightNewlyAddedPerson();
            return null;
        });
        awaitPulses();
        // Drains scrollTo and lays out the resulting visible cells before inspecting them.
        onFx(() -> {
            panel.getRoot().applyCss();
            panel.getRoot().layout();
            return null;
        });
        onFx(() -> {
            screenshot(stages.get(0), "edited-athlete");
            assertTrue(panel.getRoot().lookupAll(".list-cell").stream()
                    .filter(node -> node instanceof ListCell<?> cell && cell.getItem() == updated)
                    .anyMatch(node -> node.localToScene(node.getBoundsInLocal()).intersects(
                            panel.getRoot().localToScene(panel.getRoot().getBoundsInLocal()))),
                    "Updated athlete should be visible after scrolling");
            return null;
        });
    }

    private Stage show(Scene scene) {
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.show();
        stages.add(stage);
        scene.getRoot().applyCss();
        scene.getRoot().layout();
        return stage;
    }

    private static <T> T onFx(Callable<T> action) throws Exception {
        FutureTask<T> task = new FutureTask<>(action);
        Platform.runLater(task);
        return task.get(10, TimeUnit.SECONDS);
    }

    private static void awaitPulses() throws Exception {
        CountDownLatch rendered = new CountDownLatch(1);
        onFx(() -> {
            new AnimationTimer() {
                private int pulses;

                @Override
                public void handle(long now) {
                    if (++pulses == 3) {
                        stop();
                        rendered.countDown();
                    }
                }
            }.start();
            return null;
        });
        assertTrue(rendered.await(5, TimeUnit.SECONDS));
    }

    private static void screenshot(Stage stage, String name) throws Exception {
        WritableImage snapshot = stage.getScene().snapshot(null);
        BufferedImage image = new BufferedImage((int) snapshot.getWidth(), (int) snapshot.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                image.setRGB(x, y, snapshot.getPixelReader().getArgb(x, y));
            }
        }
        Path directory = Path.of("build", "reports", "release-ui");
        Files.createDirectories(directory);
        ImageIO.write(image, "png", directory.resolve(name + ".png").toFile());
    }
}
