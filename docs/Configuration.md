---
  layout: default.md
  title: "Configuration guide"
---

# Configuration guide

TrackFlow does not read a `config.json` file. For a packaged JAR, its home folder is the folder containing the actual
JAR, even when it is launched from another directory or through a symbolic link. The following paths are relative
to that home folder:

| File | Purpose |
|------|---------|
| `data/trackflow.json` | Saved athlete roster |
| `preferences.json` | Window size and position |
| `trackflow.log.*` | Application logs |

An IDE or test run from unpacked class files uses the process working directory instead. `ApplicationHome` resolves
this location; the roster and preference paths are defined in `trackflow.MainApp`. The log level is the `LOG_LEVEL` constant
in `trackflow.commons.core.LogsCenter`. Changing those defaults requires rebuilding the application. See the
[User Guide](UserGuide.md#editing-the-data-file) before moving or editing roster data.
