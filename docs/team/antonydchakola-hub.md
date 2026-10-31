---
  layout: default.md
  title: "Antony Dennis Chakola - Project Portfolio"
---

# Antony Dennis Chakola - Project Portfolio

### Project: TrackFlow
TrackFlow is a desktop application for track and field coaches to manage their athlete rosters. It provides a fast,
command-driven interface alongside a graphical view of athletes, their contact details, age categories, and events.

### Code contributions
- **Command autocomplete**: Built `AutocompleteEngine` and its integration in `CommandBox`.
  - While the command word is typed, a dropdown suggests matching command templates (e.g. `tag INDEX t/`). Tab or a
    click applies the selected suggestion, and Enter always runs the typed text instead of a suggestion.
  - Pressing Tab again inserts the next missing prefix. Prefixes already typed are recognized in any order and case,
    escaped prefixes such as `r//` are not counted, and indexed commands get prefixes only after a numeric index.
  - The `sort` suggestion lists the supported fields and orders.
  - Refactored the `CommandBox` key handling into small single-purpose methods, and fixed Escape not closing the
    dropdown (JavaFX redirects key presses to the open popup, whose list consumed Escape).
- **Event tagging**: Built the `tag` and `untag` commands, which share an `EventTagCommand` base class and a generic
  `EventTagCommandParser<T>`.
  - `tag` adds only the events an athlete does not already have and reports the skipped ones as stored; `untag`
    removes nothing if any given event is missing, guarding against typos.
  - Designed the event name rules: words separated by a single space or hyphen, up to 30 characters, allowing
    apostrophes and decimals (e.g. `Women's 100m`, `Hammer 7.26kg`) and non-English letters.
  - Events match ignoring case, including accents typed as separate marks and letters such as Greek sigma with
    several lower-case forms. Older events longer than 30 characters remain removable.
- **Other features**: Let `find` search athletes by event (`find t/EVENT`), implemented the `list` command, and
  tightened name validation to reject names with dangling or repeated hyphens and apostrophes.

### Testing
- Wrote the unit tests for `Tag`, the tag and untag commands and parsers, and `AutocompleteEngine`, covering every line
  of these classes, including boundary values such as 30- and 31-character events.
- Added JavaFX tests for the autocomplete dropdown (Tab, Up/Down, Enter, Escape), and fixed them hanging the Linux CI
  build by skipping them when no display is available.

### Documentation
- **User Guide**: Wrote the command autocomplete section and the `tag`/`untag` section, with examples whose outputs
  match the sample data.
- **Developer Guide**: Wrote the command autocomplete implementation and design considerations, the event tagging
  section with its class diagram, and the manual testing steps for both features.

### Project management and team contributions
- **Role**: Deliverables and deadlines expert.
- Reviewed 26 pull requests from teammates and reported 5 bugs during alpha testing.
- Before the v1.5 release, tested the whole product against the User Guide and Developer Guide and reported the bugs
  found to their owners, including indexed commands acting on a different athlete after tied sorts.
- Updated the version references in the guides for the v1.6 release.
