package trackflow.commons.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Writes and reads files.
 */
public class FileUtil {

    /**
     * Creates parent directories of file if it has a parent directory.
     */
    private static void createParentDirsOfFile(Path file) throws IOException {
        Path parentDir = file.getParent();

        if (parentDir != null) {
            Files.createDirectories(parentDir);
        }
    }

    /**
     * Reads the contents of {@code file} as UTF-8 text.
     *
     * @throws IOException If the file cannot be read, including when it does not exist.
     */
    public static String readFromFile(Path file) throws IOException {
        return Files.readString(file, StandardCharsets.UTF_8);
    }

    /**
     * Writes content to a temporary sibling file before replacing {@code file}.
     * The replacement is atomic when the filesystem supports atomic moves. Otherwise, the completed temporary file
     * is moved into place normally. A failure before replacement leaves an existing destination unchanged.
     */
    public static void writeToFile(Path file, String content) throws IOException {
        Path destination = file.toAbsolutePath();
        createParentDirsOfFile(destination);
        Path temporaryFile = Files.createTempFile(destination.getParent(), ".trackflow-", ".tmp");
        try {
            Files.writeString(temporaryFile, content, StandardCharsets.UTF_8);
            replaceFile(temporaryFile, destination);
        } finally {
            Files.deleteIfExists(temporaryFile);
        }
    }

    private static void replaceFile(Path source, Path destination) throws IOException {
        try {
            Files.move(source, destination, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);
        }
    }

}
