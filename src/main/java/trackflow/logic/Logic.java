package trackflow.logic;

import javafx.collections.ObservableList;
import trackflow.commons.core.GuiSettings;
import trackflow.logic.commands.CommandResult;
import trackflow.logic.commands.exceptions.CommandException;
import trackflow.logic.parser.exceptions.ParseException;
import trackflow.model.person.Person;

/**
 * API for executing commands and reading the current athlete view and GUI preferences.
 */
public interface Logic {
    /**
     * Executes a command and returns its feedback and UI actions.
     * Roster-changing commands must be saved before their changes are published to the live model.
     * Commands that only change the view do not save roster data.
     *
     * @param commandText The command as entered by the user.
     * @return The command result, with its saved flag set only after a successful roster save.
     * @throws CommandException If command execution or saving fails.
     * @throws ParseException If the input does not conform to the command's format or field constraints.
     */
    CommandResult execute(String commandText) throws CommandException, ParseException;

    /**
     * Returns the live, unmodifiable athlete list in its current filtered and sorted display order.
     */
    ObservableList<Person> getFilteredPersonList();

    /**
     * Returns the user prefs' GUI settings.
     */
    GuiSettings getGuiSettings();

    /**
     * Sets the user prefs' GUI settings.
     */
    void setGuiSettings(GuiSettings guiSettings);
}
