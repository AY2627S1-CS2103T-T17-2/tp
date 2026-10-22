package trackflow.logic.commands;

import static java.util.Objects.requireNonNull;
import static trackflow.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.ArrayList;
import java.util.List;

import trackflow.commons.core.index.Index;
import trackflow.logic.Messages;
import trackflow.logic.commands.exceptions.CommandException;
import trackflow.model.Model;
import trackflow.model.person.Performance;
import trackflow.model.person.Person;

/**
 * Removes one recorded performance from an athlete, by its position in that athlete's list of performances.
 */
public class PbDelCommand extends Command {

    public static final String COMMAND_WORD = "pbdel";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Removes a recorded performance from the athlete identified by the index number in the displayed "
            + "list.\n"
            + "Parameters: INDEX (a positive integer) ENTRY_INDEX (a positive integer: the position of the "
            + "performance in that athlete's list)\n"
            + "Example: " + COMMAND_WORD + " 1 2";

    public static final String MESSAGE_SUCCESS = "Removed performance from %1$s: %2$s";
    public static final String MESSAGE_INVALID_PERFORMANCE_INDEX = "The performance index provided is invalid.";
    public static final String MESSAGE_NO_PERFORMANCES = "%1$s has no recorded performances to remove.";

    private final Index athleteIndex;
    private final Index performanceIndex;

    /**
     * @param athleteIndex of the athlete in the displayed list
     * @param performanceIndex of the performance in that athlete's list of performances
     */
    public PbDelCommand(Index athleteIndex, Index performanceIndex) {
        requireNonNull(athleteIndex);
        requireNonNull(performanceIndex);
        this.athleteIndex = athleteIndex;
        this.performanceIndex = performanceIndex;
    }

    @Override
    public boolean changesRoster() {
        return true;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();
        if (athleteIndex.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person athlete = lastShownList.get(athleteIndex.getZeroBased());
        List<Performance> performances = athlete.getPerformances();
        if (performances.isEmpty()) {
            throw new CommandException(String.format(MESSAGE_NO_PERFORMANCES, athlete.getName()));
        }
        if (performanceIndex.getZeroBased() >= performances.size()) {
            throw new CommandException(MESSAGE_INVALID_PERFORMANCE_INDEX);
        }

        List<Performance> updated = new ArrayList<>(performances);
        Performance removed = updated.remove(performanceIndex.getZeroBased());

        Person updatedAthlete = new Person(athlete.getName(), athlete.getAgeCategory(), athlete.getPhone(),
                athlete.getEmail(), athlete.getAddress(), athlete.getRemark(), athlete.getTags(), updated);
        model.setPerson(athlete, updatedAthlete);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        return new CommandResult(String.format(MESSAGE_SUCCESS, athlete.getName(), removed));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof PbDelCommand otherCommand)) {
            return false;
        }
        return athleteIndex.equals(otherCommand.athleteIndex)
                && performanceIndex.equals(otherCommand.performanceIndex);
    }
}
