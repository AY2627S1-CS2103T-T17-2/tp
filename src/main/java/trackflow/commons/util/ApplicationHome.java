// @@author qiyuannn
package trackflow.commons.util;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Locates the directory containing the running JAR, or the working directory for an IDE/classes launch.
 * Does not log: logging itself needs this location during early startup.
 */
public final class ApplicationHome {
    private static final Path HOME = findHome();

    private ApplicationHome() {}

    /**
     * Returns the absolute application home. A packaged JAR's symbolic links are resolved to its real location.
     *
     * @return The directory used by storage and early logging.
     */
    public static Path getPath() {
        return HOME;
    }

    /**
     * Resolves a fixed application-relative file path inside the home directory.
     *
     * @param relativePath Application file path, such as {@code data/trackflow.json}; must not be null.
     * @return The normalized absolute path inside application home.
     * @throws IllegalArgumentException If the supplied path escapes the application home.
     */
    public static Path resolve(String relativePath) {
        Path resolved = HOME.resolve(relativePath).normalize();
        if (!resolved.startsWith(HOME)) {
            throw new IllegalArgumentException("Application files must stay inside " + HOME);
        }
        return resolved;
    }

    private static Path findHome() {
        URL location = ApplicationHome.class.getProtectionDomain().getCodeSource().getLocation();
        try {
            Path codeLocation = Path.of(location.toURI()).toRealPath();
            if (Files.isRegularFile(codeLocation)) {
                return codeLocation.getParent();
            }
            if (Files.isDirectory(codeLocation)) {
                return Path.of("").toAbsolutePath().normalize();
            }
            throw new IOException("Application code location is not a file or directory: " + codeLocation);
        } catch (IOException | URISyntaxException e) {
            // Refuse an unknown packaged location rather than accidentally writing into another directory.
            throw new IllegalStateException("Cannot determine the TrackFlow application home", e);
        }
    }
}
