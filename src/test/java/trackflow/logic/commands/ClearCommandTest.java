package trackflow.logic.commands;

import static trackflow.logic.commands.CommandTestUtil.assertCommandSuccess;
import static trackflow.testutil.TypicalPersons.getTypicalRoster;

import org.junit.jupiter.api.Test;

import trackflow.model.AthleteRoster;
import trackflow.model.Model;
import trackflow.model.ModelManager;
import trackflow.model.UserPrefs;

public class ClearCommandTest {

    @Test
    public void execute_emptyAthleteRoster_success() {
        Model model = new ModelManager();
        Model expectedModel = new ModelManager();

        assertCommandSuccess(new ClearCommand(), model, ClearCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_nonEmptyAthleteRoster_success() {
        Model model = new ModelManager(getTypicalRoster(), new UserPrefs());
        Model expectedModel = new ModelManager(getTypicalRoster(), new UserPrefs());
        expectedModel.setRoster(new AthleteRoster());

        assertCommandSuccess(new ClearCommand(), model, ClearCommand.MESSAGE_SUCCESS, expectedModel);
    }

}
