package trackflow.commons.util;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import trackflow.commons.core.LogsCenter;
import trackflow.commons.exceptions.DataLoadingException;

/**
 * Converts Java objects to and from JSON and reads or saves their file representations.
 */
public class JsonUtil {

    private static final Logger logger = LogsCenter.getLogger(JsonUtil.class);

    private static ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules()
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            // Reject files with repeated keys so that a corrupted or hand-edited file is treated as unreadable
            // (and preserved) instead of silently keeping only one of the duplicated values.
            .configure(JsonParser.Feature.STRICT_DUPLICATE_DETECTION, true)
            .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

    static <T> void serializeObjectToJsonFile(Path jsonFile, T objectToSerialize) throws IOException {
        FileUtil.writeToFile(jsonFile, toJsonString(objectToSerialize));
    }

    static <T> T deserializeObjectFromJsonFile(Path jsonFile, Class<T> classOfObjectToDeserialize)
            throws IOException {
        return fromJsonString(FileUtil.readFromFile(jsonFile), classOfObjectToDeserialize);
    }

    /**
     * Returns the object decoded from a JSON file, or an empty optional if the file is absent.
     * Rejects a JSON null root. Missing properties are handled by the target class; its domain constraints
     * may require separate validation after deserialization.
     *
     * @param <T> Type of object to decode.
     * @param filePath Source file; must not be null.
     * @param classOfObjectToDeserialize Class whose fields describe the JSON structure.
     * @return The decoded object, or an empty optional if the file is absent.
     * @throws DataLoadingException If the file cannot be read, decoded, or contains a null root.
     */
    public static <T> Optional<T> readJsonFile(
            Path filePath, Class<T> classOfObjectToDeserialize) throws DataLoadingException {
        requireNonNull(filePath);

        if (!Files.exists(filePath)) {
            return Optional.empty();
        }
        logger.info("JSON file " + filePath + " found.");

        T jsonFile;

        try {
            jsonFile = deserializeObjectFromJsonFile(filePath, classOfObjectToDeserialize);
            if (jsonFile == null) {
                throw new IOException("JSON file contains a null root: " + filePath);
            }
        } catch (IOException e) {
            logger.warning("Error reading from jsonFile file " + filePath + ": " + e);
            throw new DataLoadingException(e);
        }

        return Optional.of(jsonFile);
    }

    /**
     * Saves an object as JSON to the specified file.
     * Overwrites existing file if it exists, creates a new file if it doesn't.
     *
     * @param <T> Type of object to serialize.
     * @param jsonFile Object to save; must not be null.
     * @param filePath Destination file; must not be null.
     * @throws IOException If serialization, writing, or replacement of the file fails.
     */
    public static <T> void saveJsonFile(T jsonFile, Path filePath) throws IOException {
        requireNonNull(filePath);
        requireNonNull(jsonFile);

        serializeObjectToJsonFile(filePath, jsonFile);
    }


    /**
     * Converts JSON text to an instance of the requested class.
     * Unlike {@link #readJsonFile(Path, Class)}, this conversion can return null for a JSON null root.
     *
     * @param <T> Type of object to decode.
     * @param json JSON text to decode.
     * @param instanceClass Class whose fields describe the JSON structure.
     * @return The decoded object, or null for a JSON null root.
     * @throws IOException If the text is malformed or cannot be converted to the requested class.
     */
    public static <T> T fromJsonString(String json, Class<T> instanceClass) throws IOException {
        try (JsonParser parser = objectMapper.getFactory().createParser(json)) {
            T value = objectMapper.readValue(parser, instanceClass);
            if (parser.nextToken() != null) {
                // Content after the first JSON value means the file is malformed; rejecting it lets the caller
                // preserve the file instead of silently dropping the trailing data on the next save.
                throw new IOException("Unexpected content after the JSON value.");
            }
            return value;
        }
    }

    /**
     * Converts an object into a formatted JSON string.
     *
     * @param <T> Type of object to encode.
     * @param instance Object to convert; null is encoded as the JSON null value.
     * @return The object's JSON representation.
     * @throws JsonProcessingException If the object cannot be encoded as JSON.
     */
    public static <T> String toJsonString(T instance) throws JsonProcessingException {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(instance);
    }

}
