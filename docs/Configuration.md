---
  layout: default.md
  title: "Configuration guide"
---

# Configuration guide

TrackFlow v1.3 does not read a `config.json` file. It uses these paths relative to the folder from which it is launched:

| File | Purpose |
|------|---------|
| `data/trackflow.json` | Saved athlete roster |
| `preferences.json` | Window size and position |
| `trackflow.log.*` | Application logs |

The roster and preference paths are constants in `trackflow.MainApp`. The log level is the `LOG_LEVEL` constant
in `trackflow.commons.core.LogsCenter`. Changing those defaults requires rebuilding the application. See the
[User Guide](UserGuide.md#editing-the-data-file) before moving or editing roster data.
