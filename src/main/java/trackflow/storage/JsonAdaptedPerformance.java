package trackflow.storage;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import trackflow.commons.exceptions.IllegalValueException;
import trackflow.model.person.Performance;

/**
 * Jackson-friendly version of {@link Performance}.
 */
class JsonAdaptedPerformance {

    private final String event;
    private final String result;
    private final String date;

    /**
     * Constructs a {@code JsonAdaptedPerformance} with the given performance details.
     */
    @JsonCreator
    public JsonAdaptedPerformance(@JsonProperty("event") String event, @JsonProperty("result") String result,
            @JsonProperty("date") String date) {
        this.event = event;
        this.result = result;
        this.date = date;
    }

    /**
     * Converts a given {@code Performance} into this class for Jackson use.
     */
    public JsonAdaptedPerformance(Performance source) {
        event = source.getEvent();
        result = source.getResult();
        date = source.getDate() == null ? null : source.getDate().toString();
    }

    /**
     * Converts this Jackson-friendly adapted performance into the model's {@code Performance} object.
     *
     * @throws IllegalValueException if any stored value violates a performance constraint.
     */
    public Performance toModelType() throws IllegalValueException {
        if (event == null || !Performance.isValidEvent(event)) {
            throw new IllegalValueException(Performance.EVENT_MESSAGE_CONSTRAINTS);
        }
        if (result == null || !Performance.isValidResult(result)) {
            throw new IllegalValueException(Performance.RESULT_MESSAGE_CONSTRAINTS);
        }
        LocalDate modelDate = null;
        if (date != null) {
            if (!Performance.isValidDate(date)) {
                throw new IllegalValueException(Performance.DATE_MESSAGE_CONSTRAINTS);
            }
            modelDate = LocalDate.parse(date.trim());
        }
        return new Performance(event, result, modelDate);
    }
}
