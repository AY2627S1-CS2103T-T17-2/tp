---
  layout: default.md
  title: "Kenil - Project Portfolio"
---

# Kenil - Project Portfolio

### Project: TrackFlow
TrackFlow is a desktop application for track and field coaches and sports administrators to manage their athlete rosters efficiently. It provides a fast, CLI-optimized interface alongside a clean graphical view to track athletes, contact details, ages, and performance tags.

### Code contributions
- **UI Enhancements**: Resolved critical UI runtime bugs including window fitting, layout resizing, keyboard autocomplete behavior, and fixed the locale for athlete indexes. Added logic to show "X of Y" count when filter or find commands hide athletes.
- **Validation Hardening**: Improved phone number validation and parsing by supporting bracketed country codes and rejecting malformed inputs.

### Testing
- Wrote and expanded UI integration tests to assert correct graphical component state updates upon command executions. Asserted correct bounds constraints for application window scaling.

### Documentation
- **User Guide**: Authored UI walkthroughs and refreshed UI screenshots for the current release.
- **Developer Guide**: Created the UI class diagram (`UiClassDiagram.puml`) with window placement helpers and wrote the DG window-bounds implementation explanation.

### Project management and team contributions
- **Role**: UI
- Actively managed UI-related PRs, ensuring a cohesive and responsive visual layout. Guided the user interface experience and handled bug fixes for v1.4 and v1.5 UI rendering.
