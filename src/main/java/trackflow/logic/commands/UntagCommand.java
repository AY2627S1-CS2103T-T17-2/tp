package trackflow.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import trackflow.commons.core.LogsCenter;
import trackflow.commons.core.index.Index;
import trackflow.logic.commands.exceptions.CommandException;
import trackflow.model.Model;
import trackflow.model.person.Person;
import trackflow.model.tag.Tag;

/**
 * Removes one or more track events from an existing athlete in the roster.
 * Nothing is removed if any of the given events is not one of the athlete's events.
 */
public class UntagCommand extends EventTagCommand {

    public static final String COMMAND_WORD = "untag";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Removes track events from the athlete identified by the index number used in the displayed "
            + "athlete list.\n"
            + "Parameters: INDEX (must be a positive integer) t/EVENT [t/EVENT]...\n"
            + "Example: " + COMMAND_WORD + " 1 t/100m";

    public static final String MESSAGE_SUCCESS = "Removed event(s) %1$s from %2$s.";
    public static final String MESSAGE_EVENT_NOT_FOUND = "%1$s does not have event(s): %2$s. No events were removed.";

    private static final Logger logger = LogsCenter.getLogger(UntagCommand.class);

    /**
     * Creates a command that removes the given events from an athlete.
     *
     * @param index of the athlete in the filtered athlete list.
     * @param eventsToRemove events to remove from the athlete; must not be empty.
     */
    public UntagCommand(Index index, Set<Tag> eventsToRemove) {
        super(index, eventsToRemove);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Person athlete = getTargetAthlete(model);

        Set<Tag> missingEvents = new HashSet<>(events);
        missingEvents.removeAll(athlete.getTags());
        if (!missingEvents.isEmpty()) {
            throw new CommandException(String.format(MESSAGE_EVENT_NOT_FOUND,
                    athlete.getName(), formatEvents(missingEvents)));
        }

        // Report the events as stored on the athlete, which may differ in case from the user's input
        Set<Tag> removedEvents = new HashSet<>(athlete.getTags());
        removedEvents.retainAll(events);
        Set<Tag> updatedEvents = new HashSet<>(athlete.getTags());
        updatedEvents.removeAll(events);
        Person updatedAthlete = replaceEvents(model, athlete, updatedEvents);
        assert updatedAthlete.getTags().stream().noneMatch(events::contains);
        logger.fine("Removed " + removedEvents + " from " + updatedAthlete.getName());

        return new CommandResult(String.format(MESSAGE_SUCCESS,
                formatEvents(removedEvents), updatedAthlete.getName()));
    }
}
