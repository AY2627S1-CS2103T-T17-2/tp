package trackflow;

import java.util.logging.Logger;

import trackflow.commons.core.LogsCenter;

/**
 * Exercises application initialization and real persistence in a child JVM without opening a JavaFX window.
 */
public class StartupProcessProbe {

    public static void main(String[] args) throws Exception {
        MainApp app = new MainApp();
        app.init();
        if (args.length > 0 && args[0].equals("change")) {
            app.logic.execute("clear");
            app.logic.execute("y");
            app.logic.execute("add n/Avery Tan a/Under 14 p/91234567 e/avery@example.com addr/23 Bishan Street");
            app.logic.execute("remark 1 r/Weekend training");
            app.logic.execute("tag 1 t/100m");
        }
        Logger logger = LogsCenter.getLogger(StartupProcessProbe.class);
        logger.info("STARTUP_PROCESS_OK");
        System.out.println("ROSTER_FILE=" + app.storage.getRosterFilePath().toAbsolutePath());
        System.out.println("ATHLETE_COUNT=" + app.model.getRoster().getPersonList().size());
        app.stop();
    }
}
