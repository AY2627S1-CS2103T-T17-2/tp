package trackflow.logic;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import trackflow.commons.core.GuiSettings;
import trackflow.commons.core.LogsCenter;
import trackflow.logic.commands.Command;
import trackflow.logic.commands.CommandResult;
import trackflow.logic.commands.exceptions.CommandException;
import trackflow.logic.parser.TrackFlowParser;
import trackflow.logic.parser.exceptions.ParseException;
import trackflow.model.Model;
import trackflow.model.ModelManager;
import trackflow.model.ReadOnlyRoster;
import trackflow.model.person.Person;
import trackflow.storage.Storage;

/**
 * Coordinates command parsing, execution, and persistence.
 * Applies roster-changing commands to a detached model and publishes them only after saving succeeds.
 */
public class LogicManager implements Logic {
    public static final String FILE_OPS_ERROR_FORMAT = "Could not save data due to the following error: %s";

    public static final String FILE_OPS_PERMISSION_ERROR_FORMAT =
            "Could not save data to file %s due to insufficient permissions to write to the file or the folder.";

    private final Logger logger = LogsCenter.getLogger(LogicManager.class);

    private final Model model;
    private final Storage storage;
    private final TrackFlowParser trackFlowParser;

    /**
     * Constructs a {@code LogicManager} with the given {@code Model} and {@code Storage}.
     */
    public LogicManager(Model model, Storage storage) {
        this.model = model;
        this.storage = storage;
        trackFlowParser = new TrackFlowParser();
    }

    /**
     * {@inheritDoc}
     * Roster changes are staged with the current filter and sort order. A parsing, execution, or save failure
     * leaves the live roster and display state unchanged.
     */
    @Override
    public CommandResult execute(String commandText) throws CommandException, ParseException {
        logger.info("----------------[USER COMMAND][" + commandText + "]");

        Command command = trackFlowParser.parseCommand(commandText);
        if (!command.changesRoster()) {
            return command.execute(model);
        }

        Model stagedModel = createStagedModel();
        CommandResult commandResult = command.execute(stagedModel);

        try {
            storage.saveRoster(stagedModel.getRoster());
        } catch (AccessDeniedException e) {
            throw new CommandException(String.format(FILE_OPS_PERMISSION_ERROR_FORMAT,
                    storage.getRosterFilePath()), e);
        } catch (IOException ioe) {
            throw new CommandException(String.format(FILE_OPS_ERROR_FORMAT, ioe.getMessage()), ioe);
        }

        model.setRoster(stagedModel.getRoster());
        model.updateFilteredPersonList(stagedModel.getPersonListPredicate());
        return commandResult.withDataSaved();
    }

    /**
     * Returns a detached copy of the model with the same roster and display state.
     * Roster mutations are applied to this copy until persistence succeeds. Comparator ties retain the live
     * displayed order so that indexed commands address the same athlete in the copy.
     */
    private Model createStagedModel() {
        Model stagedModel = new ModelManager(model.getRoster(), model.getUserPrefs());
        stagedModel.updateFilteredPersonList(model.getPersonListPredicate());
        if (model.getPersonListComparator() != null) {
            Map<Person, Integer> displayedPositions = new IdentityHashMap<>();
            for (int index = 0; index < model.getFilteredPersonList().size(); index++) {
                displayedPositions.put(model.getFilteredPersonList().get(index), index);
            }
            // A fresh SortedList otherwise resolves ties from stored order, which can differ after another sort.
            // Only the detached copy receives this tie breaker; the live comparator and saved order are unchanged.
            stagedModel.updateSortedPersonList(model.getPersonListComparator().thenComparingInt(
                    person -> displayedPositions.getOrDefault(person, Integer.MAX_VALUE)));
        }
        return stagedModel;
    }

    @Override
    public ObservableList<Person> getFilteredPersonList() {
        return model.getFilteredPersonList();
    }

    @Override
    public ReadOnlyRoster getRoster() {
        return model.getRoster();
    }

    @Override
    public GuiSettings getGuiSettings() {
        return model.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        model.setGuiSettings(guiSettings);
    }
}
