package trackflow.verification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

import trackflow.logic.LogicManager;
import trackflow.logic.commands.CommandResult;
import trackflow.logic.commands.exceptions.CommandException;
import trackflow.logic.parser.exceptions.ParseException;
import trackflow.model.AthleteRoster;
import trackflow.model.ModelManager;
import trackflow.model.ReadOnlyRoster;
import trackflow.model.UserPrefs;
import trackflow.storage.JsonRosterStorage;
import trackflow.storage.JsonUserPrefsStorage;
import trackflow.storage.StorageManager;
import trackflow.testutil.PersonBuilder;

/**
 * Exhaustively explores all 9^4 action sequences from a one-athlete roster against a small reference machine.
 * The bound and action alphabet are explicit: this is bounded checking, not an unbounded correctness proof.
 */
public class ConfirmationModelTest {
    private static final List<String> ACTIONS = List.of("clear", "Y", "yes", "no", "", "list",
            "restart", "fail-save", "invalid");
    private static final int DEPTH = 4;

    @Test
    public void allBoundedSequences_matchConfirmationAndPersistenceModel() throws Exception {
        int sequenceCount = (int) Math.pow(ACTIONS.size(), DEPTH);
        for (int sequence = 0; sequence < sequenceCount; sequence++) {
            MemoryStorage storage = new MemoryStorage();
            ModelManager model = new ModelManager(storage.saved, new UserPrefs());
            LogicManager logic = logic(model, storage);
            ReferenceState expected = new ReferenceState();
            StringBuilder trace = new StringBuilder("sequence=" + sequence);
            int remaining = sequence;
            for (int step = 0; step < DEPTH; step++) {
                String action = ACTIONS.get(remaining % ACTIONS.size());
                remaining /= ACTIONS.size();
                trace.append(" -> [").append(action).append(']');
                boolean errorExpected = expected.accept(action);
                if (action.equals("restart")) {
                    model = new ModelManager(storage.saved, new UserPrefs());
                    logic = logic(model, storage);
                } else if (action.equals("fail-save")) {
                    storage.failNextSave = true;
                } else {
                    try {
                        CommandResult result = logic.execute(action);
                        assertEquals(false, errorExpected, trace.toString());
                        assertEquals(expected.justSaved, result.isDataSaved(), trace.toString());
                    } catch (CommandException | ParseException e) {
                        if (!errorExpected) {
                            fail(trace + ": unexpected rejection", e);
                        }
                    }
                }
                assertEquals(expected.present ? 1 : 0, model.getRoster().getPersonList().size(), trace.toString());
                assertEquals(expected.present ? 1 : 0, storage.saved.getPersonList().size(), trace.toString());
                assertEquals(expected.writes, storage.successfulWrites, trace.toString());
            }
        }
    }

    private static LogicManager logic(ModelManager model, MemoryStorage storage) {
        return new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(Path.of("unused-model-preferences.json"))));
    }

    /**
     * Specification over booleans and a write count; does not call the production parser or commands.
     */
    private static class ReferenceState {
        private boolean pending;
        private boolean present = true;
        private boolean failNextSave;
        private boolean justSaved;
        private int writes;

        boolean accept(String action) {
            justSaved = false;
            switch (action) {
                case "restart":
                    pending = false;
                    return false;
                case "fail-save":
                    failNextSave = true;
                    return false;
                case "clear":
                    pending = true;
                    return false;
                case "Y":
                case "yes":
                    boolean confirmed = pending;
                    pending = false;
                    if (!confirmed) {
                        return true;
                    }
                    if (failNextSave) {
                        failNextSave = false;
                        return true;
                    }
                    present = false;
                    writes++;
                    justSaved = true;
                    return false;
                case "no":
                    boolean wasPending = pending;
                    pending = false;
                    return !wasPending;
                case "list":
                    pending = false;
                    return false;
                case "":
                    // An empty submission re-shows a pending warning; otherwise it is an empty-command error
                    return !pending;
                default:
                    pending = false;
                    return true;
            }
        }
    }

    /**
     * Only the bounded machine uses memory storage; generated roster scenarios below use real JSON files.
     */
    private static class MemoryStorage extends JsonRosterStorage {
        private AthleteRoster saved = new AthleteRoster();
        private boolean failNextSave;
        private int successfulWrites;

        MemoryStorage() {
            super(Path.of("unused-model-roster.json"));
            saved.addPerson(new PersonBuilder().build());
        }

        @Override
        public void saveRoster(ReadOnlyRoster roster) throws IOException {
            if (failNextSave) {
                failNextSave = false;
                throw new IOException("Injected bounded-model save failure");
            }
            saved = new AthleteRoster(roster);
            successfulWrites++;
        }
    }
}
