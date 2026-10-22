package trackflow.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static trackflow.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import trackflow.testutil.SerializableTestClass;
import trackflow.testutil.TestUtil;

/**
 * Tests JSON Read and Write
 */
public class JsonUtilTest {

    private static final Path SERIALIZATION_FILE = TestUtil.getFilePathInSandboxFolder("serialize.json");

    @Test
    public void serializeObjectToJsonFile_noExceptionThrown() throws IOException {
        SerializableTestClass serializableTestClass = new SerializableTestClass();
        serializableTestClass.setTestValues();

        JsonUtil.serializeObjectToJsonFile(SERIALIZATION_FILE, serializableTestClass);

        assertEquals(FileUtil.readFromFile(SERIALIZATION_FILE), SerializableTestClass.JSON_STRING_REPRESENTATION);
    }

    @Test
    public void deserializeObjectFromJsonFile_noExceptionThrown() throws IOException {
        FileUtil.writeToFile(SERIALIZATION_FILE, SerializableTestClass.JSON_STRING_REPRESENTATION);

        SerializableTestClass serializableTestClass = JsonUtil
                .deserializeObjectFromJsonFile(SERIALIZATION_FILE, SerializableTestClass.class);

        assertEquals(serializableTestClass.getName(), SerializableTestClass.getNameTestValue());
        assertEquals(serializableTestClass.getListOfLocalDateTimes(), SerializableTestClass.getListTestValues());
        assertEquals(serializableTestClass.getMapOfIntegerToString(), SerializableTestClass.getHashMapTestValues());
    }

    @Test
    public void fromJsonString_singleRootWithWhitespace_preservesValueAndNullContract() throws IOException {
        assertEquals(Map.of("message", "literal } { text"),
                JsonUtil.fromJsonString(" {\"message\":\"literal } { text\"} \t\r\n", Map.class));
        assertEquals(42, JsonUtil.fromJsonString("42 \n", Integer.class));
        assertNull(JsonUtil.fromJsonString("null \t", Object.class));
    }

    @Test
    public void fromJsonString_extraRootsGarbageOrTruncation_throwsIoException() {
        for (String json : List.of("{} GARBAGE", "{} {}", "[] []", "42 43", "null {}", "{\"a\":1")) {
            assertThrows(IOException.class, () -> JsonUtil.fromJsonString(json, Object.class));
        }
    }
}
