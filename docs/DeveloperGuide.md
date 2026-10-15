---
  layout: default.md
  title: "Developer Guide"
  pageNav: 3
---

# TrackFlow Developer Guide

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## **Acknowledgements**

* This project is based on the AddressBook-Level3 project created by the [SE-EDU initiative](https://se-education.org).
* Libraries used: [JavaFX](https://openjfx.io/), [Jackson](https://github.com/FasterXML/jackson), [JUnit5](https://github.com/junit-team/junit5).
* Font used: [Plus Jakarta Sans](https://github.com/tokotype/PlusJakartaSans) by Tokotype, licensed under [OFL 1.1](https://scripts.sil.org/OFL).

--------------------------------------------------------------------------------------------------------------------

## **Setting up, getting started**

Refer to the guide [_Setting up and getting started_](SettingUp.md).

--------------------------------------------------------------------------------------------------------------------

## **Design**

### Architecture

<puml src="diagrams/ArchitectureDiagram.puml" width="280" />

The ***Architecture Diagram*** given above explains the high-level design of the App.

The following provides a quick overview of the main components and their interactions.

**Main components of the architecture**

**`Main`** (consisting of classes [`Main`](https://github.com/AY2627S1-CS2103T-T17-2/tp/tree/master/src/main/java/trackflow/Main.java) and [`MainApp`](https://github.com/AY2627S1-CS2103T-T17-2/tp/tree/master/src/main/java/trackflow/MainApp.java)) is in charge of the app launch and shut down.
* At app launch, it initializes the other components in the correct sequence, and connects them up with each other.
* At shut down, it shuts down the other components and invokes cleanup methods where necessary.

The bulk of the app's work is done by the following four components:

* [**`UI`**](#ui-component): The UI of the App.
* [**`Logic`**](#logic-component): The command executor.
* [**`Model`**](#model-component): Holds the data of the App in memory.
* [**`Storage`**](#storage-component): Reads data from, and writes data to, the hard disk.

[**`Commons`**](#common-classes) represents a collection of classes used by multiple other components.

**How the architecture components interact with each other**

The *Sequence Diagram* below shows how the components interact with each other for the scenario where the user issues the command `delete 1`.

<puml src="diagrams/ArchitectureSequenceDiagram.puml" width="1100" />

Each of the four main components (also shown in the diagram above),

* defines its *API* in an `interface` with the same name as the Component.
* provides its functionality through a concrete `{Component Name}Manager` class that implements the corresponding API interface.

For example, the `Logic` component defines its API in `Logic.java` and implements it in `LogicManager.java`. Other components interact with a component through its interface rather than its concrete class, preventing them from coupling to that component's implementation, as illustrated in the following partial class diagram.

<puml src="diagrams/ComponentManagers.puml" width="300" />

The sections below give more details of each component.

### UI component

The **API** of this component is specified in [`Ui.java`](https://github.com/AY2627S1-CS2103T-T17-2/tp/tree/master/src/main/java/trackflow/ui/Ui.java)

<puml src="diagrams/UiClassDiagram.puml" alt="Structure of the UI Component"/>

The UI consists of a `MainWindow` and its parts, such as `CommandBox`, `ResultDisplay`, `PersonListPanel`, and `StatusBarFooter`. All of these, including `MainWindow`, inherit from the abstract `UiPart` class, which captures common behavior among classes that represent visible GUI parts.

The `UI` component uses the JavaFX UI framework. The layouts of these UI parts are defined in matching `.fxml` files in `src/main/resources/view`. For example, [`MainWindow.fxml`](https://github.com/AY2627S1-CS2103T-T17-2/tp/tree/master/src/main/resources/view/MainWindow.fxml) specifies the layout of [`MainWindow`](https://github.com/AY2627S1-CS2103T-T17-2/tp/tree/master/src/main/java/trackflow/ui/MainWindow.java).

The `UI` component,

* executes user commands using the `Logic` component.
* listens for changes to `Model` data so that the UI can be updated with the modified data.
* keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
* depends on some classes in the `Model` component because it displays `Person` objects from the model.

### Logic component

**API** : [`Logic.java`](https://github.com/AY2627S1-CS2103T-T17-2/tp/tree/master/src/main/java/trackflow/logic/Logic.java)

Here's a (partial) class diagram of the `Logic` component:

<puml src="diagrams/LogicClassDiagram.puml" width="550"/>

The sequence diagram below illustrates the interactions within the `Logic` component, taking `execute("delete 1")` API call as an example.

<puml src="diagrams/DeleteSequenceDiagram.puml" alt="Interactions Inside the Logic Component for the `delete 1` Command" />



How the `Logic` component works:

1. When `Logic` is called upon to execute a command, the command is passed to a `TrackFlowParser` object, which in turn creates a parser that matches the command (e.g., `DeleteCommandParser`) and uses it to parse the command.
1. This results in a `Command` object (more precisely, an object of one of its subclasses e.g., `DeleteCommand`) which is executed by the `LogicManager`.
1. The command can communicate with the `Model` when it is executed (e.g. to delete a person).<br>
   Note that although this is shown as a single step in the diagram above for simplicity, the code can require several interactions between the command object and the `Model` to complete the operation.
1. A roster-changing command identifies itself through `Command.changesRoster()`. `LogicManager` executes it against a
   detached model containing the current roster, filter, and sort order. It saves the staged roster before publishing
   the change to the live model. If saving fails, the live roster and displayed list are never changed. View-only
   commands such as `list`, `find`, `filter`, `sort`, `help`, and `exit` execute directly and do not write the roster.
1. The result of the command execution is encapsulated as a `CommandResult` object which is returned from `Logic`.

Here are the other classes in `Logic` (omitted from the class diagram above) that are used for parsing a user command:

<puml src="diagrams/ParserClasses.puml" width="600"/>

How the parsing works:
* When called upon to parse a user command, the `TrackFlowParser` class creates an `XYZCommandParser` (`XYZ` is a placeholder for the specific command name, e.g., `AddCommandParser`). The parser uses the other classes shown above to parse the user command and create an `XYZCommand` object (e.g., `AddCommand`). The `TrackFlowParser` returns that object as a `Command` object.
* All `XYZCommandParser` classes, such as `AddCommandParser` and `DeleteCommandParser`, implement the `Parser` interface so they can be treated similarly where appropriate, for example during testing.

### Model component
**API** : [`Model.java`](https://github.com/AY2627S1-CS2103T-T17-2/tp/tree/master/src/main/java/trackflow/model/Model.java)

<puml src="diagrams/ModelClassDiagram.puml" width="800" />


The `Model` component,

* stores the athlete roster data i.e., all `Person` objects (which are contained in a `UniquePersonList` object).
* stores the `Person` objects selected by the current filter, such as search results, in a `FilteredList`, and wraps
  that view in a `SortedList` for display ordering. It exposes the sorted view as an
  `ObservableList<Person>` that the UI can observe and bind to, so the UI updates when filtering, sorting, or roster
  data changes.
* distinguishes duplicate identity from complete value equality. `Person.isSamePerson` compares the normalized name
  and matching contact details, while `Person.equals` and `hashCode` include every stored field, including the remark.
* stores a `UserPrefs` object that represents the user’s preferences (currently, just the GUI settings). This is exposed to the outside as a `ReadOnlyUserPrefs` object.
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)


The following diagram focuses on stored athlete fields. `Tag` remains an internal compatibility field for legacy
rosters; `UniqueTagList` is not part of the implementation.

<puml src="diagrams/BetterModelClassDiagram.puml" width="850" />

### Storage component

**API** : [`Storage.java`](https://github.com/AY2627S1-CS2103T-T17-2/tp/tree/master/src/main/java/trackflow/storage/Storage.java)

<puml src="diagrams/StorageClassDiagram.puml" width="550" />

The `Storage` component,
* can save both athlete roster data and user preference data in JSON format, and read them back into corresponding objects.
* is implemented by `StorageManager`, which delegates the actual JSON file access to `JsonRosterStorage` and `JsonUserPrefsStorage` (one class per data file).
* writes JSON to a temporary sibling file before replacing the destination. `FileUtil` uses an atomic move when the
  filesystem supports it and cleans up the temporary file after success or failure.
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)

### Common classes

Classes used by multiple components are in the `trackflow.commons` package.

--------------------------------------------------------------------------------------------------------------------

## **Implementation**

This section describes some noteworthy details on how certain features are implemented.

### Command parsing and validation

`TrackFlowParser` treats command words without considering letter case and routes commands with parameters to their
dedicated parsers. `ArgumentTokenizer` recognizes supported parameter prefixes without considering letter case while
storing them under their canonical prefix. `ParserUtil.verifyNoUnknownPrefixes` rejects unsupported parameter-like
tokens of the reserved single-letter or `addr/` form before values are parsed. For example, unsupported `t/`
produces a command-specific error. Longer slash-containing words such as `wrong/value` are treated as text, while
`x/value` is treated as an unsupported parameter. Single-letter slash text inside addresses or remarks can also be
misidentified as a parameter; prefix handling remains a parsing-owner release item.

Commands that take no arguments (`clear`, `help`, and `exit`) reject extra text. `remark` requires exactly one `r/`
prefix. Index parsing accepts arbitrarily long positive digit sequences and safely maps values beyond the in-memory
index range to an out-of-range displayed index, avoiding numeric-overflow errors.

### Broad and field-scoped finding

`FindCommandParser` supports unprefixed broad keywords and the field prefixes `n/`, `a/`, `p/`, `e/`, `addr/`, and
`r/`. It rejects empty or repeated supported field parameters and unsupported reserved prefixes. `PersonContainsKeywordsPredicate` stores broad keywords
separately from field-specific criteria. Broad keywords search the combined athlete text, while a prefixed value is
tested only against its selected field. The criteria use case-insensitive partial matching and OR semantics.
This allows `find n/und` to match a name such as `Underwood` without matching athletes merely because their age
category starts with `Under`.

### Command autocomplete

The command autocomplete feature is facilitated by `CommandBox` and `AutocompleteEngine`.
It provides real-time dropdown suggestions for commands based on the user's input, and an intelligent prefix insertion mechanism to guide the user in completing complex commands.

#### Implementation Details

* `AutocompleteEngine` stores a predefined list of command templates (e.g., `"add n/ a/ p/ e/ addr/"`, `"edit INDEX n/ a/ p/ e/ addr/"`, `"delete INDEX"`, `"remark INDEX r/"`).
* `CommandBox` listens to changes in the `TextField`'s `textProperty`. While typing an initial command word, it queries `AutocompleteEngine#getSuggestions` for matching templates (matching case-insensitively) and displays them using a JavaFX `Popup` containing a `ListView`. Once whitespace or parameters are entered, or if there are no matches, the popup is hidden.
* **Selection:** When the user selects a suggestion (via Mouse Click or `Tab`), `CommandBox` inserts the command word followed by a space (e.g., `add `), positions the caret, and hides the popup. This leaves space for the user to insert an index or parameters if required.
* **Separation of Selection and Execution:** Pressing `Enter` does **not** select or apply autocomplete suggestions. If the user presses `Enter` while the suggestion dropdown is visible, `CommandBox` hides the popup and executes the command as typed in the text field. This prevents accidental template insertions when a fast typist intends to submit their input. Pressing `Escape` closes the dropdown without executing.
* **Incremental Prefix Insertion:** If the user presses `Tab` while the suggestion dropdown is closed (or after selecting a command), `CommandBox` uses `AutocompleteEngine#getNextPrefix` to scan the current input and append the next missing prefix based on the command's template. Existing prefixes are matched case-insensitively and regardless of order, allowing the user to construct the command sequentially by pressing `Tab` after entering each parameter's data.

#### Design Considerations

* **Aspect: Suggestion selection key (`Tab` only vs `Tab` and `Enter`)**
  * **Alternative 1:** Allow both `Tab` and `Enter` to select suggestions.
    * Pros: Familiar to users accustomed to web search bars that accept suggestions on `Enter`.
    * Cons: Interferes with command execution. A fast typist who types a command and presses `Enter` may accidentally apply an unwanted suggestion template instead of executing what they typed.
  * **Alternative 2 (Current Choice):** Use `Tab` exclusively to select suggestions and insert prefixes; reserve `Enter` strictly for command execution.
    * Pros: Follows established CLI and code-editor conventions (e.g., bash, zsh, IDE snippets) where `Tab` completes and `Enter` executes. Eliminates accidental command template overwrites upon submission.
    * Cons: Users who expect `Enter` to confirm a suggestion must use `Tab` or mouse click.
* **Aspect: Prefix insertion strategy**
  * **Alternative 1:** Insert the entire command template at once (e.g., `add n/ a/ p/ e/ addr/`).
    * Pros: Shows the user all required fields immediately.
    * Cons: If the user accidentally executes the command before filling all fields, the parser fails with an invalid format error. It also requires manual navigation between the placeholders.
  * **Alternative 2 (Current Choice):** Incrementally insert prefixes using `Tab`.
    * Pros: Creates a guided, IDE-like snippet experience. Prevents trailing empty prefixes from causing parsing errors.
    * Cons: Requires slightly more complex logic to parse the current text and determine the next missing prefix.

### Age-category filtering

`TrackFlowParser` dispatches `filter` to `FilterCommandParser`. The parser requires exactly one case-insensitive `a/`
prefix and no preamble, rejects duplicates and unknown prefixes, and reuses `ParserUtil.parseAgeCategory` for the same
canonical categories and normalization as `add`. Unsupported trailing text fails category validation.

`FilterCommand` installs an `AgeCategoryPredicate` through `Model.updateFilteredPersonList`. The predicate compares `Person.getAgeCategory()` with the normalized category. Replacing the predicate searches the full underlying roster, retains the current displayed ordering, including any active sort, and does not mutate records. Feedback distinguishes zero, one, and multiple matches. The filter is a view state and is not persisted across restarts.

The existing `DeleteCommand` resolves indexes against `Model.getFilteredPersonList()`, so it already targets the correct displayed athlete and keeps the predicate active. No delete implementation changes are needed for filtering. `ListAthleteCommand` restores `PREDICATE_SHOW_ALL_PERSONS`; `find` and `filter` replace one another.

`FilterCommandParserTest` covers categories and invalid syntax, `AgeCategoryPredicateTest` covers exact category matching, and `FilterCommandTest` covers routing, feedback, unchanged records, replacing searches, invalid input, compatibility with existing deletion, and `list` restoration.

### Multi-field sorting

`TrackFlowParser` dispatches `sort` to `SortCommandParser`. The parser requires one supported field and accepts one
optional order, defaulting to ascending. It accepts short (`asc`, `desc`) and long (`ascending`, `descending`) order
names without considering letter case, while rejecting missing fields, unsupported values, and extra arguments.

`ModelManager` wraps its existing `FilteredList<Person>` in a `SortedList<Person>`. `SortCommand` installs the selected
comparator through `Model.updateSortedPersonList`, changing only the displayed view rather than the underlying
`roster` order. Consequently, sorting composes with `find`, `filter`, and `list`, and indexed commands continue to
resolve athletes against exactly what the user sees. The comparator is session-only and is not persisted.

Text fields are compared without considering letter case. Phone numbers are converted to arbitrary-precision numeric
values for comparison, avoiding overflow and lexicographic ordering errors. Age categories use the competition order
`Under 14`, `Under 16`, `Under 18`, `Under 20`, `Open`.

### Future features [Future]

Undo/redo, archiving, guardian links, relay squads, event grouping, and structured performance logs are not
implemented in v1.3. Their user stories record possible future scope; they do not describe existing classes or
commit to a release version. Legacy tag data remains in the model and JSON schema for compatibility, but tag
commands are not part of this release.

--------------------------------------------------------------------------------------------------------------------

## **Documentation, logging, testing, dev-ops**

* [Documentation guide](Documentation.md)
* [Testing guide](Testing.md)
* [Logging guide](Logging.md)
* [DevOps guide](DevOps.md)

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Requirements**

### Product scope

**Target user profile**:

* High-school or club track-and-field coaches who manage large student-athlete rosters.
* Coaches who need to maintain athletes' contact details and age categories on their own computer.
* Keyboard-proficient users who type quickly, prefer typing to mouse interactions, and are comfortable learning short text commands.
* Individual users who need a desktop application that works without an account or internet connection.

**Value proposition**: TrackFlow helps a coach maintain and retrieve an organized athlete roster through short keyboard commands, reducing the effort of navigating forms and keeping contact information available between training sessions.

**v1.3 scope**: The release supports adding, listing, editing, remarking, searching, filtering, sorting, deleting,
clearing, built-in help, and exit. New athletes require a name, age category, phone number, email address, and
nonblank address. Remarks can be added or removed separately. Roster changes are saved locally before being published
to the live model; view-only commands do not save roster data.

Future features are marked **[Future]** below. Cloud synchronization and collaborative editing are excluded from
this single-user, local product. Athletes and guardians are records, not application users.

### Persistence and recovery status

`MainApp` loads `data/trackflow.json` relative to the process working directory. If no file exists, it supplies the
sample roster; the first successful roster change creates the file. An intentionally saved empty roster remains
empty on restart. `preferences.json` stores window settings separately.

`JsonRosterStorage` converts malformed JSON, null roots, missing or null persons lists, null athletes, null tag
entries, invalid saved field values, and duplicate athletes into `DataLoadingException`. `MainApp` preserves the
original bytes in a sibling file named `trackflow.json.unreadable-<UUID>.bak`, logs its path, and starts an empty
roster. Each failed load creates a separate backup without overwriting earlier backups. Subsequent successful
roster changes can replace the active file while the backup remains available for manual recovery. If backup
creation fails, startup stops before commands become available, protecting the original from being overwritten.

### User stories

Priorities: `* * *` = high, `* *` = medium, `*` = low. **Implemented** describes v1.3 behavior;
**Release target** identifies a requirement still awaiting integration; **[Future]** records unimplemented scope
without committing it to a release; **Excluded** records an idea outside this product. Priority does not imply status.

| ID | Priority | Status | As a ... | I want to ... | So that I can ... |
|----|----------|-------|----------|---------------|------------------|
| US01 | `* * *` | Implemented | coach | add an athlete's name, age category, phone number, email, and address | maintain the essential information needed to contact and organize athletes |
| US02 | `* * *` | Implemented | coach | view all athletes and sort them by useful fields and directions | locate athletes and select the correct displayed record |
| US03 | `* * *` | Implemented | coach | permanently delete one selected athlete | remove a record I no longer need |
| US04 | `* * *` | Implemented | returning coach | recover saved roster changes when I reopen TrackFlow | continue work without re-entering athletes |
| US05 | `* * *` | Implemented | coach | receive clear success messages and actionable errors | know whether a command worked and correct mistakes |
| US06 | `* * *` | Implemented | coach | have matching-name records with a shared phone or email rejected, and receive warnings for differently named records sharing contacts | avoid redundant entries without excluding different athletes |
| US07 | `* * *` | Implemented | coach | keep my previous roster unchanged when a change cannot be saved | avoid believing an unsaved update is permanent |
| US08 | `* * *` | Implemented | coach | have unreadable saved data preserved separately when loading fails | retain the possibility of recovering it while starting a new roster |
| US09 | `* *` | Implemented | coach | edit an athlete's details directly | correct information without deleting and re-entering the record |
| US10 | `* *` | Implemented | coach | find athletes using partial values from any searchable field | locate records without scanning the full roster or remembering an exact value |
| US11 | `* *` | Implemented | coach | filter athletes by age category | review athletes in a competition category |
| US12 | `* *` | [Future] | coach | assign and remove event-specialization tags for athletes | identify athletes who train for particular events and keep their specializations current |
| US13 | `* *` | Implemented | new coach using TrackFlow | view built-in command help | learn or recall how to operate the application |
| US14 | `* *` | [Future] | frequent user | recall previously entered commands | reduce repeated typing |
| US15 | `* *` | [Future] | coach | archive and restore athlete records | keep departed athletes' information without including them in the active roster |
| US16 | `* *` | [Future] | coach | record guardians and add or remove their links to athletes | find the appropriate family contact and keep those links current |
| US17 | `* *` | [Future] | coach | organize athletes into relay squads and event groups | review who belongs to each team or event |
| US18 | `* *` | [Future] | coach | record and review personal bests by event | track an athlete's performance progress |
| US19 | `* *` | [Future] | coach | record and review relevant medical notes | consult recorded considerations when planning training |
| US20 | `* *` | [Future] | coach | record and review competition eligibility | identify athletes recorded as eligible for an event |
| US21 | `* *` | [Future] | coach | record and review participation logs | track athletes' involvement in training or competitions |
| US22 | `* *` | [Future] | coach | undo and redo roster changes | recover from accidental changes or reapply them |
| US23 | `* *` | [Future] | coach | view an athlete's contact details, event tags, guardian links, and status notes together | prepare for a meet without searching through separate records |
| US25 | `* *` | [Future] | coach | view all event-specialization tags currently used in the roster | review which disciplines the team covers |
| US26 | `* *` | [Future] | coach | view the contact information of athletes in an event group | communicate practice or meet updates to that group |
| US27 | `* *` | [Future] | coach | link multiple guardians to one athlete | retain alternative contacts when a guardian is unavailable |
| US28 | `* *` | [Future] | coach managing siblings | link one guardian record to multiple athletes | update shared contact details only once |
| US29 | `* *` | [Future] | coach organizing travel | identify athletes without a guardian phone number | collect missing emergency contact information before departure |
| US31 | `* *` | [Future] | relay coach | assign or replace athletes on specific relay legs | maintain a clear running order and adjust the lineup when availability changes |
| US32 | `* *` | [Future] | relay coach | designate alternate runners for a relay squad | record replacements before a meet |
| US34 | `* *` | [Future] | coach | view an athlete's relay-squad and event-group memberships | review the athlete's assignments before making changes |
| US35 | `* *` | [Future] | coach | correct an inaccurate personal-best or status log entry | keep the athlete's recorded history reliable |
| US36 | `*` | [Future] | coach | import and export roster records in bulk | transfer my own records without entering each one manually |
| US37 | `*` | [Future] | coach | store names with characters beyond v1.3's supported set | preserve athletes' preferred name spellings |
| US38 | `*` | [Future] | coach | store multiple phone numbers and phone extensions | retain alternative ways of contacting an athlete |
| US39 | `*` | [Future] | coach | check whether an email address can receive mail | distinguish a structurally valid address from a reachable one |
| US40 | `*` | Implemented | keyboard-oriented coach | close TrackFlow with a text command | finish a session from the command box |
| US41 | `*` | Excluded | coach using several computers | synchronize my roster through cloud storage | access the same changes on different devices |
| US42 | `*` | Excluded | coach working with other coaches | share and collaboratively edit a roster | coordinate updates with colleagues |

Email reachability checking (US39) is a future enhancement beyond structural email validation. Its design must preserve offline access to the roster and must not require a TrackFlow-operated remote server.

### Use cases

For every use case below, the **system** is TrackFlow and the **primary actor** is the coach. **MSS** means main
success scenario. UC01–UC06 describe implemented workflows. Future requirements remain in the user stories.

#### UC01: Register an athlete

**Related stories**: US01, US02, US05, US06, US07.<br>
**Precondition**: TrackFlow is open.<br>
**Success postcondition**: One complete athlete record is saved and displayed in the roster.

**MSS**

1. The coach requests to view the roster.
2. TrackFlow displays the roster with current indexes, or indicates that it is empty.
3. The coach submits the new athlete's name, age category, phone number, email address, and address.
4. TrackFlow validates the input, checks for a duplicate, and saves the new record.
5. TrackFlow displays the full roster in the current sort order, or saved roster order if no sort is active, and displays the added athlete's details.

Use case ends.

**Extensions**

* 3a. A required field is missing, repeated, or invalid, or an unsupported parameter is supplied.
  * 3a1. TrackFlow shows the applicable error and retains the entered command for correction. The roster is unchanged.
  * Use case resumes at step 3.
* 4a. The normalized name and either phone or email match an existing athlete.
  * 4a1. TrackFlow reports the duplicate and leaves the roster unchanged. Age category does not affect this check. A shared name alone is allowed.
  * Use case resumes at step 3.
* 4b. The change cannot be saved.
  * 4b1. TrackFlow reports the save failure. No athlete is added, and the previous roster remains visible.
  * Use case ends.
* 4c. A differently named athlete shares the phone or email.
  * 4c1. TrackFlow saves the new record and includes a possible-duplicate warning naming the matching athletes.
  * Use case resumes at step 5.

#### UC02: Remove an athlete

**Related stories**: US02, US03, US05, US07.<br>
**Precondition**: TrackFlow is open.<br>
**Success postcondition**: Exactly the selected athlete is permanently removed from the saved roster.

**MSS**

1. The coach requests to list athletes.
2. TrackFlow displays the roster in the current displayed ordering with current indexes.
3. The coach checks the athlete's details and requests deletion using the athlete's displayed index.
4. TrackFlow saves the deletion, refreshes and renumbers the roster, and displays the deleted athlete's details.

Use case ends. Deletion has no confirmation prompt, archive, or undo operation.

**Extensions**

* 2a. The roster is empty.
  * 2a1. TrackFlow reports that the roster is empty.
  * Use case ends.
* 3a. The index is missing, malformed, or does not identify an athlete in the current list, or extra arguments are supplied.
  * 3a1. TrackFlow explains the error and leaves the roster unchanged.
  * Use case resumes at step 3.
* 4a. The deletion cannot be saved.
  * 4a1. TrackFlow reports the failure and retains the athlete in the roster.
  * Use case ends.

#### UC03: Edit an athlete's details

**Related stories**: US02, US05, US06, US07, US09.<br>
**Precondition**: TrackFlow is open and the athlete is in the roster.<br>
**Success postcondition**: The selected athlete's updated details are saved; omitted fields, remarks, and legacy tags
are retained.

**MSS**

1. The coach views the roster, optionally using find, filter, or sort.
2. TrackFlow displays the matching athletes with current indexes and ordering.
3. The coach submits `edit INDEX` with at least one supported field to change.
4. TrackFlow resolves the displayed index, validates the proposed record, and checks the full roster for duplicates.
5. TrackFlow saves the change, displays the full roster with the active sort retained, and confirms the edit.

Use case ends.

**Extensions**

* 3a. The index or field values are missing, invalid, repeated, or unsupported.
  * 3a1. TrackFlow reports the error and leaves the roster and display unchanged.
  * Use case resumes at step 3.
* 4a. The edit would duplicate another athlete, including one hidden by the current filter.
  * 4a1. TrackFlow rejects the edit and leaves the roster unchanged.
  * Use case resumes at step 3.
* 5a. Saving fails.
  * 5a1. TrackFlow reports the failure and retains the previous roster, filter, and sort order.
  * Use case ends.

#### UC04: Continue working with a saved roster

**Related stories**: US04, US07, US08.<br>
**Precondition**: A saved roster exists, or this is the first launch.<br>
**Success postcondition**: On reopening, the coach sees the saved roster, including successful additions, edits, remark updates, deletions, and clearing.

**MSS**

1. The coach closes TrackFlow.
2. The coach launches TrackFlow again.
3. TrackFlow loads the saved roster into the displayed list.
4. The coach requests to list athletes.
5. TrackFlow displays the saved athletes with their current indexes.

Use case ends.

**Extensions**

* 3a. No saved roster exists, as on first use.
  * 3a1. TrackFlow starts with sample athletes and logs that no saved roster was found. The data file is created after the first successful roster change.
  * Use case resumes at step 4.
* 3b. The saved roster cannot be recovered.
  * 3b1. TrackFlow logs the loading failure and backup path, and starts with an empty roster while preserving the unrecovered data in a separate file.
  * 3b2. Subsequent successful changes create a new active roster without replacing the preserved data. TrackFlow does not claim the old records were restored; the preserved copy remains until the coach removes it outside TrackFlow.
  * 3b3. If the backup cannot be created, TrackFlow stops startup before accepting commands to protect the original file.
  * Use case resumes at step 4.

#### UC05: Filter athletes by age category

**Status**: Implemented in v1.3.<br>
**Related stories**: US02, US03, US05, US11.<br>
**Precondition**: TrackFlow is open.<br>
**Success postcondition**: Only matching athletes are displayed; stored records are unchanged.

**MSS**

1. The coach enters `filter a/AGE_CATEGORY` with one supported category.
1. TrackFlow searches the full roster and displays matching athletes in the current displayed ordering, including any active sort, with current indexes and the match count.
1. The coach enters `list` to restore the complete roster, or uses the displayed indexes with existing commands.

**Extensions**

* **1a.** The category is missing, invalid, repeated, or accompanied by extra arguments. TrackFlow reports the error and retains the previous display and roster.
* **2a.** No athletes match, including when the roster is empty. TrackFlow displays an empty list and an explicit zero-match message. `list` remains available.

#### UC06: Replace or remove an athlete remark

**Related stories**: US05, US07.<br>
**Precondition**: TrackFlow displays the athlete to update.<br>
**Success postcondition**: The replacement remark, or its removal, is saved without changing other stored fields.

**MSS**

1. The coach submits `remark INDEX r/REMARK` using the current displayed index.
2. TrackFlow replaces the existing remark; an empty value removes it.
3. TrackFlow saves the change, restores the full roster with the active sort retained, and confirms the update.

**Extensions**

* **1a.** The index is invalid or the required `r/` prefix is missing or repeated. TrackFlow reports the error and
  leaves the roster unchanged.
* **3a.** Saving fails. TrackFlow reports the failure and leaves the live roster, filter, and sort order unchanged.

### Non-Functional Requirements

These are product requirements and acceptance targets, not claims about the current build. Platform, packaging, storage, and display requirements reflect the relevant [course product constraints](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-constraints.html).

| ID | Requirement |
|----|-------------|
| NFR01 | TrackFlow shall run on Windows, Linux, and macOS with Azul Zulu JDK 25. On macOS the runtime requirement is the JDK FX distribution; packaged-JAR verification is required for each supported processor architecture. |
| NFR02 | TrackFlow shall be distributed as a single JAR that runs without an installer or separately installed application dependencies other than the specified Azul Zulu JDK 25 runtime (JDK FX on macOS). |
| NFR03 | Core roster operations and local persistence shall work without internet access, an account, or a TrackFlow-operated remote server. |
| NFR04 | TrackFlow shall support one coach using a local roster. Shared accounts, simultaneous editing, and another user's access to the data file during normal operation are outside its scope. |
| NFR05 | Roster data shall use locally stored, human-editable text files rather than a database management system. Valid manual edits made while TrackFlow is closed shall be loaded on the next launch. |
| NFR06 | Every documented command shall be usable entirely by keyboard. Successful commands shall clear and refocus the command box; failed commands shall retain their text for correction. |
| NFR07 | Success and failure shall be communicated in text, without relying on color alone. Validation failures shall identify the input problem and shall not change the roster. |
| NFR08 | A change shall be reported as successful only after it has been saved. A save failure shall leave the observable roster unchanged. Successfully saved changes shall survive normal closure and reopening. |
| NFR09 | A loading failure shall not silently overwrite unrecovered data. That data shall remain separately preserved even if the coach makes changes to the new empty roster. |
| NFR10 | As a performance target, with 1,000 athlete records on a computer with a dual-core processor, 8 GB RAM, and local SSD storage, at least 95 of 100 consecutive add, list, or delete commands shall update the display within two seconds of submission, with persistence included for changes. |
| NFR11 | At 1920 × 1080 or higher with 100% and 125% display scaling, the interface shall avoid clipping essential controls and feedback. At 1280 × 720 or higher with 150% scaling, all functions shall remain accessible, including through scrolling or resizing where necessary. |
| NFR12 | For identical input and roster state, validation shall report the same first error. Without an active sort, listing shall use saved roster order. An active sort shall remain applied across find, filter, list, and roster changes during the session; sorting shall not change saved roster order. |

### Glossary

| Term | Definition |
|------|------------|
| Active roster | The athlete records currently maintained and displayed by the coach, excluding any separately preserved unrecovered data and, if archiving is introduced, archived records. |
| Age category | A coach-assigned competition grouping: Under 14, Under 16, Under 18, Under 20, or Open. TrackFlow stores the category, not an exact age or date of birth, and does not verify competition eligibility. |
| Athlete | A student-athlete whose contact and age-category information is recorded in TrackFlow. An athlete does not log in to the application. |
| Archive / restore | A future capability to remove a record from the active roster while retaining it, and later return it to the active roster. This differs from permanent deletion. |
| CLI / GUI | Command-line interface / graphical user interface. TrackFlow accepts typed commands in a graphical window and uses that window to display records and feedback. |
| Displayed index | An athlete's positive, one-based position in the current displayed list. It is not a permanent identifier and may change after roster updates. |
| Duplicate athlete record | A record with the same normalized name and either the same phone or email as another record, regardless of age category. Differently named records sharing contacts are allowed with a possible-duplicate warning on add or edit. |
| Eligibility | Whether an athlete meets the recorded participation conditions for a competition or event. Eligibility logs are a future feature, distinct from assigning an age category. |
| Event specialization | An athletics discipline, such as sprinting or long jump, associated with an athlete through a future tagging feature. |
| Guardian | A parent or other responsible adult whose future contact record may be linked to one or more athletes. |
| Local persistence | Automatically saving roster changes on the coach's computer and loading them in a later application session. |
| Implemented | A feature available in the current v1.3 code. Release targets and future features are identified separately. |
| Normalized field | A value prepared for comparison: surrounding whitespace is removed; name and age-category internal spaces are collapsed; name, category, and email comparisons ignore letter case; phone comparisons ignore permitted formatting characters and use the digits. Display formatting may be retained. |
| Participation log | A future record of an athlete's involvement in training or competition. |
| Personal best | An athlete's best recorded result for a particular event, intended for a future performance log. |
| Relay squad / event group | A future grouping of athletes for a relay team or a particular athletics event. |
| Unrecovered data | Previously saved roster data that TrackFlow could not load, preserved in a separate `.bak` file before starting an empty roster. If preservation fails, startup stops. |

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Instructions for manual testing**

Given below are instructions to test the app manually.

<box type="info" seamless>

**Note:** These instructions only provide a starting point for testers to work on;
testers are expected to do more *exploratory* testing.
</box>

### Launch and shutdown

1. Initial launch

   1. Download the JAR file and copy it into an empty folder.

   1. In that folder, run `java -jar TrackFlow.jar` with the runtime from the setup guide.<br>
      Expected: The GUI opens with a set of sample athletes. The window size may not be optimal.

1. Saving window preferences

   1. Resize the window to an optimal size. Move the window to a different location. Close the window.

   1. Relaunch from the same folder using `java -jar TrackFlow.jar`.<br>
       Expected: The most recent window size and location are retained.

1. Run `exit`, then relaunch using `java -jar TrackFlow.jar`. Expect the saved roster to reload; session filters and sorting are reset.

### Deleting an athlete

1. Deleting an athlete while all athletes are being shown

   1. Prerequisites: List all athletes using the `list` command, with multiple athletes in the list.

   1. Test case: `delete 1`<br>
      Expected: The first athlete is deleted from the list. The status message shows the deleted athlete's details.

   1. Test case: `delete 0`<br>
      Expected: No athlete is deleted. The status message shows error details.

   1. Other incorrect delete commands to try: `delete`, `delete x`, `delete 999999999999999999999999999999`<br>
      Expected: Similar to previous.

1. Run `sort name desc`, then `delete 1`. Expect the first visibly displayed athlete to be removed. Restart and confirm the deletion persists.

### Filtering by age category

Use a separate test roster for this procedure. The deletion steps test compatibility with the existing delete command and permanently remove test records.

1. Prepare an empty test roster with one `Open` athlete followed by two distinct `Under 14` athletes.
1. Run `filter a/under   14`. Expect the two `Under 14` athletes in roster order, numbered 1 and 2, and `Displaying 2 athletes in age category Under 14.`
1. Run `delete 3`. Expect an invalid-index error and no changes, even though the full roster has three athletes.
1. Run `delete 1`. Expect the first `Under 14` athlete to be deleted and the other to appear at index 1.
1. Run `list`. Expect both remaining athletes, including the `Open` athlete.
1. Run `filter a/Under 20`. Expect an empty display and `No athletes found in age category Under 20 (0 matches).`
1. Run `list`. Expect both remaining athletes.
1. Run `filter a/Open`. Expect one match and `Displaying 1 athlete in age category Open.`
1. Try each invalid command separately: `filter`, `filter a/Under 15`, `filter a/`, and `filter a/Open a/Under 14`. Expect an error after each command and no change to the display.
1. Run `find` with the remaining `Under 14` athlete's name. Expect that athlete in the results.
1. Run `filter a/Open`. Expect the `Open` athlete, confirming that filtering searches the full roster.
1. Restart TrackFlow. Expect both remaining athletes. The filter is not saved between sessions.

### Sorting the displayed roster

Use a test roster containing athletes from several age categories with names, phones, emails, and addresses that are
not already in sorted order.

1. Run `sort name`. Expect case-insensitive alphabetical name order and the ascending success message.
1. Run `sort name desc`. Expect the reverse name order.
1. Run `sort age`. Expect `Under 14`, `Under 16`, `Under 18`, `Under 20`, then `Open`.
1. Run `sort age descending`. Expect the reverse category order.
1. Repeat with `phone`, `email`, and `address`. Verify numeric phone ordering and that empty addresses come first only
   in ascending order.
1. Run `find` or `filter`, then sort the results. Expect only the matching athletes, in the selected order.
1. Run `list`. Expect the full roster while retaining the selected order.
1. Run `delete 1` on a sorted list. Expect the athlete visibly shown at index 1 to be deleted.
1. Try `sort`, `sort height`, `sort name upwards`, and `sort name asc extra`. Expect actionable errors and no display
   change.
1. Restart TrackFlow. Expect the original saved roster order because sorting is not persisted.

### Command autocomplete

1. Real-time command suggestions
   1. Type `a` in the command box.<br>
      Expected: A dropdown popup appears showing `add n/ a/ p/ e/ addr/`.
   2. Type `f`.<br>
      Expected: The dropdown shows `filter a/` and `find `.
   3. Type `ed`.<br>
      Expected: The dropdown shows `edit INDEX n/ a/ p/ e/ addr/`.
   4. Type `d`.<br>
      Expected: The dropdown shows `delete INDEX`.
   5. Press the Down and Up arrow keys.<br>
      Expected: Selection moves between the suggestions in the list.
   6. Press Enter while suggestions are displayed.<br>
      Expected: The dropdown closes without auto-completing, and TrackFlow executes the typed input (showing an unknown command error for `f`).
   7. Press Esc while suggestions are displayed.<br>
      Expected: The dropdown closes without modifying the text in the command box.

2. Selecting a suggestion with Tab
   1. Type `ad` and press Tab (or click the suggestion with the mouse).<br>
      Expected: The suggestion is applied and the command box updates to `add `. The dropdown closes.

3. Incremental prefix insertion with Tab
   1. With `add ` in the command box, press Tab.<br>
      Expected: The first required prefix is inserted (`add n/ `).
   2. Type `John Doe` and press Tab.<br>
      Expected: The next missing prefix is inserted (`add n/John Doe a/ `).
   3. Continue pressing Tab after typing parameter values for each field.<br>
      Expected: Prefixes `p/`, `e/`, `addr/` are sequentially inserted with appropriate spacing.
   4. Enter prefixes out of order, e.g., type `add a/Open ` and press Tab.<br>
      Expected: The first missing prefix `n/` is inserted (`add a/Open n/ `).

### Saving data

1. Run `add n/Avery Tan a/Under 14 p/91234567 e/avery@example.com addr/Home` and restart TrackFlow.<br>
   Expected: Avery remains in the roster after the restart.
1. Temporarily remove write permission from the folder containing the roster data file, then attempt each
   roster-changing command separately:
   `add`, `edit`, `remark`, `delete`, and `clear`.<br>
   Expected: TrackFlow reports that it could not save. The roster, displayed athletes, filter, sort order, and data file
   remain unchanged after every attempt.
1. While storage is unwritable, run `list`, `find`, `filter`, `sort`, `help`, and `exit` separately.<br>
   Expected: Each command succeeds because it does not need to rewrite roster data.
1. Restore write permission and run a roster-changing command.<br>
   Expected: The command succeeds, the status indicates that all changes are saved, and the change survives restart.

### Adding, editing, and remarks

Use a disposable home folder; these procedures change saved data.

1. Run `clear`, then `add n/Avery Tan a/Under 14 p/91234567 e/avery@example.com addr/Home`.
   Expect one saved athlete. Restart from the same folder and confirm every supplied field remains.
1. Run `remark 1 r/Sprints`, then `edit 1 a/Under 16 addr/Training centre`. Expect the new category and address,
   with the original name, phone, email, and remark retained. Restart and verify those values again.
1. Run `remark 1 r/Recovery`, then restart. Expect `Recovery` to replace `Sprints`.
1. Run `remark 1 r/`, then restart. Expect the remark to be empty and the category still `Under 16`.
1. Try `add n/Incomplete`, `edit 1`, `edit 0 p/999`, `edit 1 addr/`, and `remark 1`.
   Expect errors with no roster changes. Add/edit/find with `t/sprinter` must report an unsupported parameter.
1. Run `clear`, restart, and confirm the roster stays empty rather than loading samples again.

### Duplicate identity and complete equality

Duplicate checks use normalized name plus **either** matching phone or matching email, regardless of age category.
Name normalization ignores case and repeated spaces; email matching ignores case. Complete equality additionally
compares every stored value, including category, address, remark, and legacy tags. These are separate contracts.

1. Start with only Avery: run `clear`, then `add n/Avery Tan a/Under 14 p/91234567 e/avery@example.com addr/Home`. Attempt to add `AVERY   TAN` with the same phone and a different
   email and category. Expect a duplicate error, with no saved changes. Repeat with a different phone and matching
   email (including different email case); expect rejection again.
1. Add a record with the same name but both a different phone and a different email. Expect success.
1. Add `Jordan Lee` with Avery's phone and a different email. Expect success with a possible-duplicate warning naming
   Avery. Different names sharing contact details are permitted.
1. Run `find n/Jordan`, then attempt to add Avery's duplicate. Expect rejection even though Avery is hidden.
1. With Jordan displayed, attempt to edit the name to `AVERY TAN` while retaining Avery's phone. Expect rejection
   because duplicate checks search the complete roster. The previous filter and roster remain unchanged.
1. Restart and repeat the duplicate attempts. Expect the same decisions after reloading.
1. Run the automated `PersonTest` and `DuplicatePersistenceTest` checks described in the testing guide. Address-only
   and remark-only changes must affect full equality without changing duplicate identity.

### Release command matrix

For each supported command, check a valid form and the applicable invalid forms below. Use mixed-case command words and prefixes for every parameterized command.

| Command | Valid example | Invalid inputs to check | Persistence expectation |
|---------|---------------|-------------------------|-------------------------|
| `add` | `add n/Avery Tan a/Under 14 p/91234567 e/avery@example.com addr/Home` | Missing/empty required fields; repeated or unknown prefixes; `t/sprinter`; invalid category, phone or email | All fields survive restart |
| `edit` | `edit 1 p/92345678` | Missing field; repeated/empty/unknown prefix; `t/sprinter`; zero, negative or very large index | Changed fields survive; omitted fields remain |
| `remark` | `remark 1 r/Sprints` | Missing/repeated `r/`; unsupported prefix; invalid index | Replacement or removal survives restart |
| `delete` | `delete 1` | Missing, zero, negative, nonnumeric or very large index; extra arguments | Selected athlete stays deleted |
| `clear` | `clear` | Any extra argument | Empty roster reloads as empty |
| `list` | `list` | Any extra argument | Does not write roster; active sort remains |
| `find` | `find n/Avery` | Empty query; empty/repeated field prefix; `t/sprinter` | Does not write roster; replaces previous search/filter |
| `filter` | `filter a/Under 14` | Missing/empty/repeated/unknown prefix; unsupported category; extra text | Does not write roster; replaces previous search/filter |
| `sort` | `sort age desc` | Missing/unknown field; invalid order; extra arguments | Does not write roster; ordering resets on restart |
| `help` | `help` | Any extra argument | Opens help even if roster storage is unwritable |
| `exit` | `exit` | Any extra argument | Closes without saving roster; window preferences are saved separately |

### Corrupt-file recovery acceptance procedure

Run this procedure against disposable fixtures, keeping an external copy of each fixture.

1. Close TrackFlow and create a disposable `data/trackflow.json`. Test each fixture separately: empty content,
   invalid JSON, `null`, `{}`, `{"persons":null}`, `{"persons":[null]}`, null required athlete fields, null tag
   entries, duplicate athletes, and invalid age categories or contact fields.
1. Launch the packaged JAR. Expect a controlled loading error and a usable empty roster for every
   invalid fixture, with a log message identifying the separately preserved original file.
1. Run `list`, then add a valid athlete. Verify the preserved file still contains the exact original bytes.
1. Restart. Expect the newly saved roster to load and the original corrupt content to remain available separately.
1. If backup creation fails, verify startup stops before accepting commands and the original file remains unchanged.

### Release verification checklist

Record the tested commit, tester, date, OS/processor, exact `java -version`, and evidence for each result. The table is
an execution record template; **Pending** is not a pass. Update it after integration on the final release commit.

| Check | Result | Evidence to record |
|-------|--------|--------------------|
| Clean build, tests, Checkstyle, coverage and `shadowJar` | Pending | Gradle output and test report |
| Packaged `TrackFlow.jar` launch | Pending | OS/processor, Zulu JDK 25 distribution and visible GUI |
| Every command and invalid-input case in the command matrix | Pending | Commands, expected/actual results |
| Add/edit/remark/delete/clear, save and restart | Pending | Reloaded roster and retained category/address/remarks |
| Unwritable storage: each mutation fails without changing roster/filter/sort | Pending | Original file and display before/after |
| Unwritable storage: list/find/filter/sort/help/exit | Pending | Successful view commands and unchanged roster file |
| Sort/find/filter followed by indexed edit/remark/delete | Pending | Visible target before command and saved target after restart |
| Empty search, then sort and list; invalid command with sorted filter | Pending | Display ordering and unchanged state on failure |
| Duplicate rules, warnings and full-roster checks after reload | Pending | Duplicate procedure and automated test results |
| Every corrupt fixture, backup, replacement roster and restart | Pending | Backup path and byte-for-byte content; loading/recovery owner sign-off |
| Retained tags/remarks and category defaults after reload | Pending | Original and reloaded roster |
| MarkBind build and rendered guide/diagram review | Pending | Build output and page inspection |
| Java CI and MarkBind Action on exact final master commit | Pending | Workflow run links and commit SHA |
