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
 * Adds one or more track events to an existing athlete in the roster. Events the athlete already has are kept.
 */
public class TagCommand extends EventTagCommand {

    public static final String COMMAND_WORD = "tag";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Adds track events to the athlete identified by the index number used in the displayed athlete "
            + "list. Existing events are kept.\n"
            + "Parameters: INDEX (must be a positive integer) t/EVENT [t/EVENT]...\n"
            + "Example: " + COMMAND_WORD + " 1 t/100m t/Long Jump";

    public static final String MESSAGE_SUCCESS = "Added event(s) %1$s to %2$s.";
    public static final String MESSAGE_SKIPPED_EVENTS = " Already had: %1$s.";
    public static final String MESSAGE_ALREADY_TAGGED = "%1$s already has event(s): %2$s.";

    private static final Logger logger = LogsCenter.getLogger(TagCommand.class);

    /**
     * @param index of the athlete in the filtered athlete list
     * @param eventsToAdd events to add to the athlete; must not be empty
     */
    public TagCommand(Index index, Set<Tag> eventsToAdd) {
        super(index, eventsToAdd);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Person athlete = getTargetAthlete(model);

        Set<Tag> newEvents = new HashSet<>(events);
        newEvents.removeAll(athlete.getTags());
        Set<Tag> skippedEvents = new HashSet<>(events);
        skippedEvents.retainAll(athlete.getTags());

        if (newEvents.isEmpty()) {
            throw new CommandException(String.format(MESSAGE_ALREADY_TAGGED,
                    athlete.getName(), formatEvents(skippedEvents)));
        }

        Set<Tag> updatedEvents = new HashSet<>(athlete.getTags());
        updatedEvents.addAll(newEvents);
        Person updatedAthlete = replaceEvents(model, athlete, updatedEvents);
        assert updatedAthlete.getTags().containsAll(events);
        logger.fine("Tagged " + updatedAthlete.getName() + " with " + newEvents);

        String message = String.format(MESSAGE_SUCCESS, formatEvents(newEvents), updatedAthlete.getName());
        if (!skippedEvents.isEmpty()) {
            message += String.format(MESSAGE_SKIPPED_EVENTS, formatEvents(skippedEvents));
        }
        return new CommandResult(message);
    }
}
