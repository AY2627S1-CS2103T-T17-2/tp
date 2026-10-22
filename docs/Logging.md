---
  layout: default.md
  title: "Logging guide"
---

# Logging guide

* The project uses the `java.util.logging` package for logging.
* `LogsCenter` manages logging levels and destinations.
* A class obtains a `Logger` with `LogsCenter.getLogger(Class)`, which logs messages at the configured level.
* Log messages are written to the console and rotated `trackflow.log` files in application home: beside the real
  JAR for packaged launches, or in the working directory for IDE/unpacked-class launches.
* If the log file cannot be opened, `LogsCenter` warns through the initialized console logger and continues with
  console logging. It does not crash initialization or change roster data because file logging is unavailable.
* The output logging level can be controlled by changing the `LOG_LEVEL` constant in the `LogsCenter` class.
* **When choosing a level for a log message**, follow the conventions given in [_[se-edu/guides] Java: Logging conventions_](https://se-education.org/guides/conventions/java/logging.html).
