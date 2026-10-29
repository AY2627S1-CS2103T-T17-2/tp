---
  layout: default.md
  title: "Kong Qi Yuan - Project Portfolio"
---

# Kong Qi Yuan - Project Portfolio

### Project: TrackFlow
TrackFlow is a desktop application for track and field coaches and sports administrators to manage their athlete rosters efficiently. It provides a fast, CLI-optimized interface alongside a clean graphical view to track athletes, contact details, ages, and performance tags.

### Code contributions
- **Core Commands**: Refined the `add` and `edit` command pipelines. Implemented rejection of unchanged athlete edits and updated the application to explicitly report duplicate names during edit operations.
- **Regex & Validation**: Fixed critical regex stack overflow vulnerabilities on malformed email inputs (AF04).
- **Roster File Processing**: Implemented logic to handle malformed roster loading, preventing application crashes and preserving unreadable data instead of discarding it.

### Testing
- Resolved testing conflicts, edited test files to accommodate strict formatting, and established test pipelines for athlete formatting and exception messages. Ensured high test coverage across new edit command constraints.

### Documentation
- **Developer Guide**: Designed the Roster JSON mapping class diagram to visualize the object conversion between `JsonRosterStorage`, `JsonSerializableRoster`, and `JsonAdaptedPerson`. Refined the documentation scope and updated workflow descriptions.
- **User Guide**: Updated documentation for the `add`, `edit`, and `help` commands, standardizing formatting.

### Project management and team contributions
- **Role**: Testing
- Ensured continuous testing integration throughout the project. Kept the team aware of testing metrics and handled large PR review merges focusing on application stability.
