package trackflow.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import trackflow.commons.exceptions.IllegalValueException;
import trackflow.model.AthleteRoster;
import trackflow.model.ReadOnlyRoster;
import trackflow.model.person.Person;

/**
 * An Immutable AthleteRoster that is serializable to JSON format.
 */
@JsonRootName(value = "trackflow")
class JsonSerializableRoster {

    public static final String MESSAGE_DUPLICATE_PERSON = "Athlete list contains duplicate athletes.";

    private final List<JsonAdaptedPerson> persons;

    /**
     * Constructs a {@code JsonSerializableRoster} with the given persons.
     */
    @JsonCreator
    public JsonSerializableRoster(@JsonProperty("persons") List<JsonAdaptedPerson> persons) {
        this.persons = persons == null ? null : new ArrayList<>(persons);
    }

    /**
     * Converts a given {@code ReadOnlyRoster} into this class for Jackson use.
     *
     * @param source future changes to this will not affect the created {@code JsonSerializableRoster}.
     */
    public JsonSerializableRoster(ReadOnlyRoster source) {
        persons = source.getPersonList().stream().map(JsonAdaptedPerson::new).collect(Collectors.toList());
    }

    /**
     * Converts this athlete roster into the model's {@code AthleteRoster} object.
     *
     * @throws IllegalValueException if there were any data constraints violated.
     */
    public AthleteRoster toModelType() throws IllegalValueException {
        if (persons == null) {
            throw new IllegalValueException("Athlete list is missing!");
        }
        AthleteRoster roster = new AthleteRoster();
        for (JsonAdaptedPerson jsonAdaptedPerson : persons) {
            if (jsonAdaptedPerson == null) {
                throw new IllegalValueException("Athlete list contains a null athlete!");
            }
            Person person = jsonAdaptedPerson.toModelType();
            if (roster.hasPerson(person)) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_PERSON);
            }
            roster.addPerson(person);
        }
        return roster;
    }

}
