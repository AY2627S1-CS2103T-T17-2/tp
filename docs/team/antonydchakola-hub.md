---
  layout: default.md
  title: "Antony Dennis Chakola - Project Portfolio"
---

# Antony Dennis Chakola - Project Portfolio

### Project: TrackFlow
TrackFlow is a desktop application for track and field coaches and sports administrators to manage their athlete rosters efficiently. It provides a fast, CLI-optimized interface alongside a clean graphical view to track athletes, contact details, ages, and performance tags.

### Code contributions
- **Autocomplete Engine**: Designed and integrated the interactive tab-autocomplete engine for the command box. Refactored `CommandBox` handlers to display supported sort fields and cyclic command suggestions directly to users.
- **Event Tags**: Authored the `tag` and `untag` commands for track events, and enabled `find` to search athletes by track event. Added support for non-English unicode letters and separated accent marks for international event tags.
- **Name Rule Standardization**: Implemented strict validation to reject names with dangling or repeated separators, standardizing the roster input format.

### Testing
- Developed the automated UI test suite for the Autocomplete engine, ensuring cyclic tab navigation and empty-state fallbacks worked flawlessly. Ensured event tag regex matching was comprehensively tested.

### Documentation
- **User Guide**: Added index support guidelines for autocomplete and created syntax examples for the event tagging feature.
- **Developer Guide**: Designed the event tagging UML class diagrams and drafted the documentation detailing the internal state machine for the autocomplete sequence.

### Project management and team contributions
- **Role**: Deliverables and deadlines expert
- Tracked milestone progress and assured deliverables matched the CS2103T standards format. Kept codebase formatting clean via code tidying tasks (wrapping long lines) to satisfy Checkstyle.
