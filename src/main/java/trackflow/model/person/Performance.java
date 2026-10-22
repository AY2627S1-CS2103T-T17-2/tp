package trackflow.model.person;

import static java.util.Objects.requireNonNull;
import static trackflow.commons.util.AppUtil.checkArgument;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Objects;

/**
 * Represents one personal-best performance for an athlete in a single event.
 * A performance has an event name, a result, and an optional date.
 * Guarantees: immutable; the event, result, and date are valid as declared by {@link #isValidEvent(String)},
 * {@link #isValidResult(String)}, and {@link #isValidDate(String)}.
 */
public class Performance {

    public static final String EVENT_MESSAGE_CONSTRAINTS = "Event names must not be blank.";
    public static final String RESULT_MESSAGE_CONSTRAINTS =
            "Results must be either a time (for example 12.34, 58.7s, 2:05.4, or 1:02:03.4) where a lower value is "
            + "better, or a distance in metres ending in 'm' (for example 6.21m or 1.85m) where a higher value is "
            + "better. Times use minutes and seconds under 60.";
    public static final String DATE_MESSAGE_CONSTRAINTS =
            "Dates must be a real calendar date in the format YYYY-MM-DD, for example 2026-10-22.";

    /** Whether a result is a time (lower is better) or a distance (higher is better). */
    public enum Kind {
        TIME("time"),
        DISTANCE("distance");

        private final String label;

        Kind(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private final String event;
    private final String result;
    private final LocalDate date;
    private final Kind kind;
    private final long magnitude; // milliseconds for a time, millimetres for a distance

    /**
     * Constructs a {@code Performance}.
     *
     * @param event  A valid, non-blank event name.
     * @param result A valid result (a time or a distance).
     * @param date   The date achieved, or {@code null} if not recorded.
     */
    public Performance(String event, String result, LocalDate date) {
        requireNonNull(event);
        requireNonNull(result);
        String trimmedEvent = event.trim();
        String trimmedResult = result.trim();
        checkArgument(isValidEvent(trimmedEvent), EVENT_MESSAGE_CONSTRAINTS);
        checkArgument(isValidResult(trimmedResult), RESULT_MESSAGE_CONSTRAINTS);
        this.event = trimmedEvent;
        this.result = trimmedResult;
        this.date = date;
        this.kind = isDistance(trimmedResult) ? Kind.DISTANCE : Kind.TIME;
        this.magnitude = isDistance(trimmedResult)
                ? parseDistanceMillimetres(trimmedResult)
                : parseTimeMillis(trimmedResult);
    }

    public String getEvent() {
        return event;
    }

    public String getResult() {
        return result;
    }

    /**
     * Returns the result formatted for display with its unit: a distance as "{@code <number> m}" and a
     * plain-seconds time as "{@code <number> s}". A time already written with minutes (containing ':') is
     * shown unchanged, since its format already identifies it as a time.
     */
    public String getDisplayResult() {
        if (kind == Kind.DISTANCE) {
            String metres = result.substring(0, result.length() - 1).trim();
            return metres + " m";
        }
        String time = result;
        if (time.toLowerCase(Locale.ROOT).endsWith("s")) {
            time = time.substring(0, time.length() - 1).trim();
        }
        return time.contains(":") ? time : time + " s";
    }

    /** Returns the recorded date, or {@code null} if none was recorded. */
    public LocalDate getDate() {
        return date;
    }

    public Kind getKind() {
        return kind;
    }

    /** Returns true if {@code other} refers to the same event, ignoring case and surrounding whitespace. */
    public boolean isSameEvent(Performance other) {
        return other != null && event.equalsIgnoreCase(other.event);
    }

    /** Returns true if this result can be compared with {@code other}'s, i.e. both are times or both are distances. */
    public boolean isComparableResult(Performance other) {
        return other != null && kind == other.kind;
    }

    /**
     * Returns true if this result is strictly better than {@code other}'s: a faster time, or a longer distance.
     * The two results must be comparable (see {@link #isComparableResult(Performance)}).
     */
    public boolean isBetterThan(Performance other) {
        requireNonNull(other);
        checkArgument(isComparableResult(other));
        return switch (kind) {
            case TIME -> magnitude < other.magnitude;
            case DISTANCE -> magnitude > other.magnitude;
        };
    }

    /** Returns true if the event name is not blank. */
    public static boolean isValidEvent(String test) {
        requireNonNull(test);
        return !test.trim().isEmpty();
    }

    /** Returns true if the result is a valid time or a valid distance. */
    public static boolean isValidResult(String test) {
        requireNonNull(test);
        String trimmed = test.trim();
        if (trimmed.isEmpty()) {
            return false;
        }
        try {
            if (isDistance(trimmed)) {
                parseDistanceMillimetres(trimmed);
            } else {
                parseTimeMillis(trimmed);
            }
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /** Returns true if the text is a real calendar date in ISO {@code YYYY-MM-DD} form. */
    public static boolean isValidDate(String test) {
        requireNonNull(test);
        try {
            LocalDate.parse(test.trim());
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private static boolean isDistance(String trimmedResult) {
        String lower = trimmedResult.toLowerCase(Locale.ROOT);
        return lower.endsWith("m") && !lower.endsWith("km");
    }

    private static long parseDistanceMillimetres(String trimmedResult) {
        String numberPart = trimmedResult.substring(0, trimmedResult.length() - 1).trim();
        if (!numberPart.matches("\\d+(\\.\\d+)?")) {
            throw new IllegalArgumentException(RESULT_MESSAGE_CONSTRAINTS);
        }
        return Math.round(Double.parseDouble(numberPart) * 1000);
    }

    private static long parseTimeMillis(String trimmedResult) {
        String time = trimmedResult;
        if (time.toLowerCase(Locale.ROOT).endsWith("s")) {
            time = time.substring(0, time.length() - 1).trim();
        }
        String[] parts = time.split(":", -1);
        double hours = 0;
        double minutes = 0;
        double seconds;
        if (parts.length == 3) {
            hours = parseWholeNumber(parts[0]);
            minutes = parseBoundedWholeNumber(parts[1]);
            seconds = parseSeconds(parts[2], true);
        } else if (parts.length == 2) {
            minutes = parseWholeNumber(parts[0]);
            seconds = parseSeconds(parts[1], true);
        } else if (parts.length == 1) {
            seconds = parseSeconds(parts[0], false);
        } else {
            throw new IllegalArgumentException(RESULT_MESSAGE_CONSTRAINTS);
        }
        return Math.round((hours * 3600 + minutes * 60 + seconds) * 1000);
    }

    private static long parseWholeNumber(String part) {
        if (!part.matches("\\d+")) {
            throw new IllegalArgumentException(RESULT_MESSAGE_CONSTRAINTS);
        }
        return Long.parseLong(part);
    }

    private static long parseBoundedWholeNumber(String part) {
        long value = parseWholeNumber(part);
        if (value >= 60) {
            throw new IllegalArgumentException(RESULT_MESSAGE_CONSTRAINTS);
        }
        return value;
    }

    private static double parseSeconds(String part, boolean mustBeUnderSixty) {
        if (!part.matches("\\d+(\\.\\d+)?")) {
            throw new IllegalArgumentException(RESULT_MESSAGE_CONSTRAINTS);
        }
        double value = Double.parseDouble(part);
        if (mustBeUnderSixty && value >= 60) {
            throw new IllegalArgumentException(RESULT_MESSAGE_CONSTRAINTS);
        }
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Performance otherPerformance)) {
            return false;
        }
        return event.equals(otherPerformance.event)
                && result.equals(otherPerformance.result)
                && Objects.equals(date, otherPerformance.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(event, result, date);
    }

    @Override
    public String toString() {
        return date == null ? event + " " + result : event + " " + result + " (" + date + ")";
    }
}
