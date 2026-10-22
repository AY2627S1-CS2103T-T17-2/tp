package trackflow.testutil;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Starts bounded, isolated JVM checks using the test runtime's classpath.
 */
public class ProcessTestUtil {

    /**
     * Runs a main class from a separate working directory, preferring classes in {@code applicationJar}.
     * Returns its combined console output; a timeout or nonzero exit fails with that output.
     */
    public static String runJava(Path applicationJar, Path workingDirectory, Path output, Class<?> mainClass,
            String... args) throws Exception {
        List<String> command = new ArrayList<>();
        command.add(Path.of(System.getProperty("java.home"), "bin", "java").toString());
        command.add("-ea");
        command.add("-cp");
        command.add(applicationJar + File.pathSeparator + getRuntimeClasspath());
        command.add(mainClass.getName());
        command.addAll(List.of(args));
        Process process = new ProcessBuilder(command).directory(workingDirectory.toFile())
                .redirectErrorStream(true).redirectOutput(output.toFile()).start();
        if (!process.waitFor(30, TimeUnit.SECONDS)) {
            process.destroyForcibly().waitFor();
            throw new AssertionError("Startup process timed out: " + Files.readString(output));
        }
        String text = Files.readString(output);
        if (process.exitValue() != 0) {
            throw new AssertionError("Startup process exited " + process.exitValue() + ": " + text);
        }
        return text;
    }

    private static String getRuntimeClasspath() throws Exception {
        Set<String> entries = new LinkedHashSet<>();
        entries.addAll(List.of(System.getProperty("java.class.path").split(File.pathSeparator)));
        for (ClassLoader loader = ProcessTestUtil.class.getClassLoader(); loader != null; loader = loader.getParent()) {
            if (loader instanceof URLClassLoader urlLoader) {
                for (URL url : urlLoader.getURLs()) {
                    entries.add(Path.of(url.toURI()).toString());
                }
            }
        }
        return String.join(File.pathSeparator, entries);
    }
}
