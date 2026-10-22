package trackflow.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static trackflow.testutil.Assert.assertThrows;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import trackflow.commons.exceptions.IllegalValueException;
import trackflow.commons.util.JsonUtil;
import trackflow.model.AthleteRoster;
import trackflow.model.person.Person;
import trackflow.testutil.PersonBuilder;
import trackflow.testutil.TypicalPersons;

public class JsonSerializableRosterTest {

    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonSerializableRosterTest");
    private static final Path TYPICAL_PERSONS_FILE = TEST_DATA_FOLDER.resolve("typicalRoster.json");
    private static final Path INVALID_PERSON_FILE = TEST_DATA_FOLDER.resolve("invalidPersonAthleteRoster.json");
    private static final Path DUPLICATE_PERSON_FILE = TEST_DATA_FOLDER.resolve("duplicatePersonAthleteRoster.json");

    @Test
    public void toModelType_sameNameSharedContact_throwsIllegalValueException() {
        Person original = new PersonBuilder().build();
        Person duplicate = new PersonBuilder(original).withEmail("changed@example.com")
                .withAgeCategory("Under 14").build();
        JsonSerializableRoster saved = new JsonSerializableRoster(List.of(
                new JsonAdaptedPerson(original), new JsonAdaptedPerson(duplicate)));
        assertThrows(IllegalValueException.class, JsonSerializableRoster.MESSAGE_DUPLICATE_PERSON,
                saved::toModelType);
    }

    @Test
    public void toModelType_typicalPersonsFile_success() throws Exception {
        JsonSerializableRoster dataFromFile = JsonUtil.readJsonFile(TYPICAL_PERSONS_FILE,
                JsonSerializableRoster.class).get();
        AthleteRoster rosterFromFile = dataFromFile.toModelType();
        AthleteRoster typicalRoster = TypicalPersons.getTypicalRoster();
        assertEquals(rosterFromFile, typicalRoster);
    }

    @Test
    public void toModelType_invalidPersonFile_throwsIllegalValueException() throws Exception {
        JsonSerializableRoster dataFromFile = JsonUtil.readJsonFile(INVALID_PERSON_FILE,
                JsonSerializableRoster.class).get();
        assertThrows(IllegalValueException.class, dataFromFile::toModelType);
    }

    @Test
    public void toModelType_duplicatePersons_throwsIllegalValueException() throws Exception {
        JsonSerializableRoster dataFromFile = JsonUtil.readJsonFile(DUPLICATE_PERSON_FILE,
                JsonSerializableRoster.class).get();
        assertThrows(IllegalValueException.class, JsonSerializableRoster.MESSAGE_DUPLICATE_PERSON,
                dataFromFile::toModelType);
    }

    @Test
    public void toModelType_nullPersonsList_throwsIllegalValueException() {
        JsonSerializableRoster roster = new JsonSerializableRoster((List<JsonAdaptedPerson>) null);
        assertThrows(IllegalValueException.class, "Athlete list is missing!", roster::toModelType);
    }

    @Test
    public void toModelType_nullPersonElement_throwsIllegalValueException() {
        JsonSerializableRoster roster = new JsonSerializableRoster(Collections.singletonList(null));
        assertThrows(IllegalValueException.class, "Athlete list contains a null athlete!", roster::toModelType);
    }

}
