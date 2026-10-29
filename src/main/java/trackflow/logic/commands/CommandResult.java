package trackflow.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Objects;

import trackflow.commons.util.ToStringBuilder;

/**
 * Represents the result of a command execution.
 */
public class CommandResult {

    private final String feedbackToUser;

    /** Help information should be shown to the user. */
    private final boolean showHelp;

    /** The application should exit. */
    private final boolean exit;

    /** Roster changes were successfully persisted while executing the command. */
    private final boolean dataSaved;
    private final boolean warning;

    /**
     * Constructs feedback with the requested help and exit actions, without claiming that data was saved.
     *
     * @param feedbackToUser Message to display to the user.
     * @param showHelp Whether the UI should show the help window.
     * @param exit Whether the UI should close the application.
     * @throws NullPointerException If {@code feedbackToUser} is null.
     */
    public CommandResult(String feedbackToUser, boolean showHelp, boolean exit) {
        this(feedbackToUser, showHelp, exit, false, false);
    }

    private CommandResult(String feedbackToUser, boolean showHelp, boolean exit, boolean dataSaved, boolean warning) {
        this.feedbackToUser = requireNonNull(feedbackToUser);
        this.showHelp = showHelp;
        this.exit = exit;
        this.dataSaved = dataSaved;
        this.warning = warning;
    }

    /**
     * Constructs feedback with no help, exit, or successful-save indication.
     *
     * @throws NullPointerException If {@code feedbackToUser} is null.
     */
    public CommandResult(String feedbackToUser) {
        this(feedbackToUser, false, false);
    }

    public String getFeedbackToUser() {
        return feedbackToUser;
    }

    public boolean isShowHelp() {
        return showHelp;
    }

    public boolean isExit() {
        return exit;
    }

    public boolean isWarning() {
        return warning;
    }

    /**
     * Returns a warning that requires the user's attention rather than reporting success.
     */
    public static CommandResult warning(String feedbackToUser) {
        return new CommandResult(feedbackToUser, false, false, false, true);
    }

    public boolean isDataSaved() {
        return dataSaved;
    }

    /**
     * Returns a new result recording a successful roster save, preserving the feedback and requested UI actions.
     * Calling this method does not perform a save; the logic layer calls it after persistence succeeds.
     */
    public CommandResult withDataSaved() {
        return new CommandResult(feedbackToUser, showHelp, exit, true, warning);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof CommandResult otherCommandResult)) {
            return false;
        }

        return feedbackToUser.equals(otherCommandResult.feedbackToUser)
                && showHelp == otherCommandResult.showHelp
                && exit == otherCommandResult.exit
                && dataSaved == otherCommandResult.dataSaved
                && warning == otherCommandResult.warning;
    }

    @Override
    public int hashCode() {
        return Objects.hash(feedbackToUser, showHelp, exit, dataSaved, warning);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("feedbackToUser", feedbackToUser)
                .add("showHelp", showHelp)
                .add("exit", exit)
                .add("dataSaved", dataSaved)
                .add("warning", warning)
                .toString();
    }

}
