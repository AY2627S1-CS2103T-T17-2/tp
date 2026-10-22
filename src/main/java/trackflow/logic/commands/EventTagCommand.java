package trackflow.logic.commands;

import static trackflow.commons.util.CollectionUtil.requireAllNonNull;
import static trackflow.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import trackflow.commons.core.index.Index;
import trackflow.commons.util.ToStringBuilder;
import trackflow.logic.Messages;
import trackflow.logic.commands.exceptions.CommandException;
import trackflow.model.Model;
import trackflow.model.person.Person;
import trackflow.model.tag.Tag;

/**
 * Represents a command that changes the track event tags of an athlete identified by index.
 */
public abstract class EventTagCommand extends Command {

    protected final Index index;
    protected final Set<Tag> events;

    /**
     * Creates an event command for the athlete at the displayed index.
     *
     * @param index of the athlete in the filtered athlete list.
     * @param events the events this command acts on; must not be empty.
     */
    protected EventTagCommand(Index index, Set<Tag> events) {
        requireAllNonNull(index, events);
        assert !events.isEmpty() : "An event tag command needs at least one event";

        this.index = index;
        this.events = Set.copyOf(events);
    }

    @Override
    public boolean changesRoster() {
        return true;
    }

    /**
     * Returns the athlete at {@code index} in the currently displayed list.
     *
     * @throws CommandException if the index is out of bounds.
     */
    protected Person getTargetAthlete(Model model) throws CommandException {
        List<Person> lastShownList = model.getFilteredPersonList();
        if (index.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
        return lastShownList.get(index.getZeroBased());
    }

    /**
     * Replaces {@code athlete} in the model with a copy that has {@code updatedEvents} as its events,
     * then shows the full roster.
     *
     * @return the updated athlete.
     */
    protected static Person replaceEvents(Model model, Person athlete, Set<Tag> updatedEvents) {
        Person updatedAthlete = new Person(athlete.getName(), athlete.getAgeCategory(), athlete.getPhone(),
                athlete.getEmail(), athlete.getAddress(), athlete.getRemark(), updatedEvents);
        model.setPerson(athlete, updatedAthlete);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        return updatedAthlete;
    }

    /**
     * Formats events as a comma-separated list, sorted alphabetically ignoring case, e.g. {@code 100m, Long Jump}.
     */
    protected static String formatEvents(Collection<Tag> events) {
        return events.stream()
                .map(event -> event.tagName)
                .sorted(Comparator.comparing(name -> name.toLowerCase(Locale.ROOT)))
                .collect(Collectors.joining(", "));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // also rejects null and commands of a different subclass
        if (other == null || other.getClass() != getClass()) {
            return false;
        }

        EventTagCommand otherCommand = (EventTagCommand) other;
        return index.equals(otherCommand.index)
                && events.equals(otherCommand.events);
    }

    @Override
    public int hashCode() {
        return index.hashCode() * 31 + events.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("index", index)
                .add("events", events)
                .toString();
    }
}
