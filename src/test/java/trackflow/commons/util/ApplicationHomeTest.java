package trackflow.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static trackflow.testutil.Assert.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

public class ApplicationHomeTest {

    @Test
    public void getPath_unpackedClasses_usesWorkingDirectory() throws Exception {
        Path classes = Path.of(ApplicationHome.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        assertTrue(Files.isDirectory(classes));
        assertEquals(Path.of("").toAbsolutePath().normalize(), ApplicationHome.getPath());
        assertEquals(ApplicationHome.getPath().resolve("preferences.json"),
                ApplicationHome.resolve("data/../preferences.json"));
    }

    @Test
    public void resolve_pathOutsideHome_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> ApplicationHome.resolve("../outside.json"));
        String absoluteOutside = ApplicationHome.getPath().getParent().resolve("outside.json").toString();
        assertThrows(IllegalArgumentException.class, () -> ApplicationHome.resolve(absoluteOutside));
    }
}
