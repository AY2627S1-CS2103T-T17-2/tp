package trackflow.logic.commands;

import static java.util.Objects.requireNonNull;
import static trackflow.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import trackflow.commons.core.index.Index;
import trackflow.logic.Messages;
import trackflow.logic.commands.exceptions.CommandException;
import trackflow.model.Model;
import trackflow.model.person.Performance;
import trackflow.model.person.Person;

/**
 * Records a personal-best performance for an athlete. A lower time or a longer distance replaces the previous
 * best for the same event; an equal or worse result is rejected so the existing record is kept.
 */
public class PbCommand extends Command {

    public static final String COMMAND_WORD = "pb";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Records a personal best for the athlete identified by the index number in the displayed list.\n"
            + "A faster time or a longer distance replaces the previous best for that event.\n"
            + "Parameters: INDEX (a positive integer) t/EVENT v/RESULT [d/DATE]\n"
            + "A result is a time such as 12.34, 58.7s, or 2:05.4 (lower is better), or a distance ending in 'm' "
            + "such as 6.21m (higher is better).\n"
            + "Example: " + COMMAND_WORD + " 1 t/100m v/12.34 d/2026-10-22";

    public static final String MESSAGE_RECORDED = "Recorded personal best for %1$s: %2$s";
    public static final String MESSAGE_IMPROVED = "New personal best for %1$s in %2$s: %3$s (previous: %4$s)";
    public static final String MESSAGE_NOT_IMPROVED =
            "%1$s already has a %2$s best of %3$s, which is at least as good as %4$s. The existing record was kept.";
    public static final String MESSAGE_INCOMPARABLE =
            "The existing %1$s best (%2$s) is a %3$s, but %4$s is a %5$s. Record the same kind of result.";

    private final Index index;
    private final Performance performance;

    /**
     * @param index of the athlete in the displayed list
     * @param performance to record as a personal best
     */
    public PbCommand(Index index, Performance performance) {
        requireNonNull(index);
        requireNonNull(performance);
        this.index = index;
        this.performance = performance;
    }

    @Override
    public boolean changesRoster() {
        return true;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();
        if (index.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person athlete = lastShownList.get(index.getZeroBased());
        List<Performance> updated = new ArrayList<>(athlete.getPerformances());
        Optional<Performance> existing = updated.stream().filter(performance::isSameEvent).findFirst();

        String message;
        if (existing.isEmpty()) {
            updated.add(performance);
            message = String.format(MESSAGE_RECORDED, athlete.getName(), performance);
        } else {
            Performance current = existing.get();
            if (!performance.isComparableResult(current)) {
                throw new CommandException(String.format(MESSAGE_INCOMPARABLE, current.getEvent(),
                        current.getResult(), current.getKind(), performance.getResult(), performance.getKind()));
            }
            if (!performance.isBetterThan(current)) {
                throw new CommandException(String.format(MESSAGE_NOT_IMPROVED, athlete.getName(),
                        current.getEvent(), current.getResult(), performance.getResult()));
            }
            updated.set(updated.indexOf(current), performance);
            message = String.format(MESSAGE_IMPROVED, athlete.getName(), performance.getEvent(),
                    performance.getResult(), current.getResult());
        }

        Person updatedAthlete = new Person(athlete.getName(), athlete.getAgeCategory(), athlete.getPhone(),
                athlete.getEmail(), athlete.getAddress(), athlete.getRemark(), athlete.getTags(), updated);
        model.setPerson(athlete, updatedAthlete);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        return new CommandResult(message);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof PbCommand otherCommand)) {
            return false;
        }
        return index.equals(otherCommand.index) && performance.equals(otherCommand.performance);
    }
}
