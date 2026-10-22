package trackflow.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class FileUtilTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void writeToFile_existingFile_replacesContentAndCleansTemporaryFile() throws Exception {
        Path target = temporaryFolder.resolve("roster.json");
        Files.writeString(target, "previous roster");

        FileUtil.writeToFile(target, "updated roster");

        assertEquals("updated roster", Files.readString(target));
        assertEquals(1, countFilesIn(temporaryFolder));
    }

    @Test
    public void writeToFile_missingParents_createsFile() throws Exception {
        Path target = temporaryFolder.resolve("nested/data/roster.json");

        FileUtil.writeToFile(target, "roster");

        assertEquals("roster", Files.readString(target));
        assertEquals(1, countFilesIn(target.getParent()));
    }

    @Test
    public void writeToFile_replacementFails_preservesDestinationAndCleansTemporaryFile() throws Exception {
        Path target = Files.createDirectory(temporaryFolder.resolve("roster.json"));
        Path original = target.resolve("original");
        Files.writeString(original, "preserved");

        assertThrows(IOException.class, () -> FileUtil.writeToFile(target, "replacement"));

        assertEquals("preserved", Files.readString(original));
        assertEquals(1, countFilesIn(temporaryFolder));
    }

    @Test
    public void readFromFile_existingAndMissingFile() throws Exception {
        Path file = temporaryFolder.resolve("sample.txt");
        Files.writeString(file, "hello trackflow");
        assertEquals("hello trackflow", FileUtil.readFromFile(file));

        Path missing = temporaryFolder.resolve("missing.txt");
        assertThrows(IOException.class, () -> FileUtil.readFromFile(missing));
    }

    private static long countFilesIn(Path folder) throws IOException {
        try (var files = Files.list(folder)) {
            return files.count();
        }
    }
}
