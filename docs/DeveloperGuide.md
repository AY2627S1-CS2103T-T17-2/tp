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

* _{List the sources of reused or adapted ideas, code, documentation, and third-party libraries here, with links to the originals.}_

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

**`Main`** (consisting of classes [`Main`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/Main.java) and [`MainApp`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/MainApp.java)) is in charge of the app launch and shut down.
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

<puml src="diagrams/ArchitectureSequenceDiagram.puml" width="574" />

Each of the four main components (also shown in the diagram above),

* defines its *API* in an `interface` with the same name as the Component.
* provides its functionality through a concrete `{Component Name}Manager` class that implements the corresponding API interface.

For example, the `Logic` component defines its API in `Logic.java` and implements it in `LogicManager.java`. Other components interact with a component through its interface rather than its concrete class, preventing them from coupling to that component's implementation, as illustrated in the following partial class diagram.

<puml src="diagrams/ComponentManagers.puml" width="300" />

The sections below give more details of each component.

### UI component

The **API** of this component is specified in [`Ui.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/ui/Ui.java)

<puml src="diagrams/UiClassDiagram.puml" alt="Structure of the UI Component"/>

The UI consists of a `MainWindow` and its parts, such as `CommandBox`, `ResultDisplay`, `PersonListPanel`, and `StatusBarFooter`. All of these, including `MainWindow`, inherit from the abstract `UiPart` class, which captures common behavior among classes that represent visible GUI parts.

The `UI` component uses the JavaFX UI framework. The layouts of these UI parts are defined in matching `.fxml` files in `src/main/resources/view`. For example, [`MainWindow.fxml`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/resources/view/MainWindow.fxml) specifies the layout of [`MainWindow`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/ui/MainWindow.java).

The `UI` component,

* executes user commands using the `Logic` component.
* listens for changes to `Model` data so that the UI can be updated with the modified data.
* keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
* depends on some classes in the `Model` component because it displays `Person` objects from the model.

### Logic component

**API** : [`Logic.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/logic/Logic.java)

Here's a (partial) class diagram of the `Logic` component:

<puml src="diagrams/LogicClassDiagram.puml" width="550"/>

The sequence diagram below illustrates the interactions within the `Logic` component, taking `execute("delete 1")` API call as an example.

<puml src="diagrams/DeleteSequenceDiagram.puml" alt="Interactions Inside the Logic Component for the `delete 1` Command" />

<box type="info" seamless>

**Note:** The lifeline for `DeleteCommandParser` should end at the destroy marker (X), but due to a limitation of PlantUML, the lifeline continues till the end of diagram.
</box>


How the `Logic` component works:

1. When `Logic` is called upon to execute a command, the command is passed to an `AddressBookParser` object, which in turn creates a parser that matches the command (e.g., `DeleteCommandParser`) and uses it to parse the command.
1. This results in a `Command` object (more precisely, an object of one of its subclasses e.g., `DeleteCommand`) which is executed by the `LogicManager`.
1. The command can communicate with the `Model` when it is executed (e.g. to delete a person).<br>
   Note that although this is shown as a single step in the diagram above for simplicity, the code can require several interactions between the command object and the `Model` to complete the operation.
1. The result of the command execution is encapsulated as a `CommandResult` object which is returned from `Logic`.

Here are the other classes in `Logic` (omitted from the class diagram above) that are used for parsing a user command:

<puml src="diagrams/ParserClasses.puml" width="600"/>

How the parsing works:
* When called upon to parse a user command, the `AddressBookParser` class creates an `XYZCommandParser` (`XYZ` is a placeholder for the specific command name, e.g., `AddCommandParser`). The parser uses the other classes shown above to parse the user command and create an `XYZCommand` object (e.g., `AddCommand`). The `AddressBookParser` returns that object as a `Command` object.
* All `XYZCommandParser` classes, such as `AddCommandParser` and `DeleteCommandParser`, implement the `Parser` interface so they can be treated similarly where appropriate, for example during testing.

### Model component
**API** : [`Model.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/model/Model.java)

<puml src="diagrams/ModelClassDiagram.puml" width="450" />


The `Model` component,

* stores the address book data i.e., all `Person` objects (which are contained in a `UniquePersonList` object).
* stores the `Person` objects selected by the current filter, such as search results, in a `FilteredList`, and wraps
  that view in a `SortedList` for display ordering. It exposes the sorted view as an unmodifiable
  `ObservableList<Person>` that the UI can observe and bind to, so the UI updates when filtering, sorting, or roster
  data changes.
* stores a `UserPrefs` object that represents the user’s preferences (currently, just the GUI settings). This is exposed to the outside as a `ReadOnlyUserPrefs` object.
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)


<box type="info" seamless>

**Note:** The alternative, arguably more object-oriented, design below keeps a unique list of tags in `AddressBook`, and each `Person` references tags from that list. This lets `AddressBook` maintain one `Tag` object per unique tag instead of each `Person` holding its own `Tag` objects.<br>

<puml src="diagrams/BetterModelClassDiagram.puml" width="450" />
</box>


### Storage component

**API** : [`Storage.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/storage/Storage.java)

<puml src="diagrams/StorageClassDiagram.puml" width="550" />

The `Storage` component,
* can save both address book data and user preference data in JSON format, and read them back into corresponding objects.
* is implemented by `StorageManager`, which delegates the actual JSON file access to `JsonAddressBookStorage` and `JsonUserPrefsStorage` (one class per data file).
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)

### Common classes

Classes used by multiple components are in the `seedu.address.commons` package.

--------------------------------------------------------------------------------------------------------------------

## **Implementation**

This section describes some noteworthy details on how certain features are implemented.

### Command autocomplete

The command autocomplete feature is facilitated by `CommandBox` and `AutocompleteEngine`.
It provides real-time dropdown suggestions for commands based on the user's input, and an intelligent prefix insertion mechanism to guide the user in completing complex commands.

#### Implementation Details

* `AutocompleteEngine` stores a predefined list of command templates (e.g., `"add n/ a/ p/ e/ addr/"`).
* `CommandBox` listens to changes in the `TextField`'s `textProperty`. For every keystroke, it asks the `AutocompleteEngine` for matching suggestions and displays them using a JavaFX `Popup` containing a `ListView`.
* **Selection:** When the user selects a suggestion (via Mouse Click, `Tab`, or `Enter`), the `CommandBox` inserts the command word (e.g., `add `) leaving space for the user to insert an index if required.
* **Incremental Prefix Insertion:** If the user presses `Tab` while the suggestion dropdown is closed, `CommandBox` uses `AutocompleteEngine#getNextPrefix` to scan the current input and append the next missing prefix based on the command's template. This allows the user to construct the command sequentially by pressing `Tab` after entering each parameter's data.

#### Design Considerations

* **Alternative 1:** Insert the entire command template at once (e.g., `add n/ a/ p/ e/ addr/`).
  * Pros: Shows the user all required fields immediately.
  * Cons: If the user accidentally executes the command before filling all fields, the parser fails with an invalid format error. It also requires manual navigation between the placeholders.
* **Alternative 2 (Current Choice):** Incrementally insert prefixes using `Tab`.
  * Pros: Creates a guided, IDE-like snippet experience. Prevents trailing empty prefixes from causing parsing errors.
  * Cons: Requires slightly more complex logic to parse the current text and determine the next missing prefix.

### Age-category filtering

`AddressBookParser` dispatches `filter` to `FilterCommandParser`. The parser requires exactly one lowercase `a/` prefix and no preamble, rejects duplicates, and reuses `ParserUtil.parseAgeCategory` for the same canonical categories and normalization as `add`. Unsupported trailing arguments fail category validation.

`FilterCommand` installs an `AgeCategoryPredicate` through `Model.updateFilteredPersonList`. The predicate compares `Person.getAgeCategory()` with the normalized category. Replacing the predicate searches the full underlying roster, preserves its order, and does not mutate records. Feedback distinguishes zero, one, and multiple matches. The filter is a view state and is not persisted across restarts.

The existing `DeleteCommand` resolves indexes against `Model.getFilteredPersonList()`, so it already targets the correct displayed athlete and keeps the predicate active. No delete implementation changes are needed for filtering. `ListAthleteCommand` restores `PREDICATE_SHOW_ALL_PERSONS`; `find` and `filter` replace one another.

`FilterCommandParserTest` covers categories and invalid syntax, `AgeCategoryPredicateTest` covers exact category matching, and `FilterCommandTest` covers routing, feedback, unchanged records, replacing searches, invalid input, compatibility with existing deletion, and `list` restoration.

### Multi-field sorting

`AddressBookParser` dispatches `sort` to `SortCommandParser`. The parser requires one supported field and accepts one
optional order, defaulting to ascending. It accepts short (`asc`, `desc`) and long (`ascending`, `descending`) order
names without considering letter case, while rejecting missing fields, unsupported values, and extra arguments.

`ModelManager` wraps its existing `FilteredList<Person>` in a `SortedList<Person>`. `SortCommand` installs the selected
comparator through `Model.updateSortedPersonList`, changing only the displayed view rather than the underlying
`AddressBook` order. Consequently, sorting composes with `find`, `filter`, and `list`, and indexed commands continue to
resolve athletes against exactly what the user sees. The comparator is session-only and is not persisted.

Text fields are compared without considering letter case. Phone numbers are converted to arbitrary-precision numeric
values for comparison, avoiding overflow and lexicographic ordering errors. Age categories use the competition order
`Under 14`, `Under 16`, `Under 18`, `Under 20`, `Open`.

### \[Proposed\] Undo/redo feature

#### Proposed Implementation

The proposed undo/redo mechanism is facilitated by `VersionedAddressBook`. It extends `AddressBook` with an undo/redo history, stored internally as an `addressBookStateList` and `currentStatePointer`. Additionally, it implements the following operations:

* `VersionedAddressBook#commit()` -- Saves the current address book state in its history.
* `VersionedAddressBook#undo()` -- Restores the previous address book state from its history.
* `VersionedAddressBook#redo()` -- Restores a previously undone address book state from its history.

These operations are exposed in the `Model` interface as `Model#commitAddressBook()`, `Model#undoAddressBook()` and `Model#redoAddressBook()` respectively.

Given below is an example usage scenario and how the undo/redo mechanism behaves at each step.

Step 1. The user launches the application for the first time. The `VersionedAddressBook` will be initialized with the initial address book state, and the `currentStatePointer` pointing to that single address book state.

<puml src="diagrams/UndoRedoState0.puml" alt="UndoRedoState0" />

Step 2. The user executes `delete 5` command to delete the 5th athlete in the address book. The `delete` command calls `Model#commitAddressBook()`, causing the modified state of the address book after the `delete 5` command executes to be saved in the `addressBookStateList`, and the `currentStatePointer` is shifted to the newly inserted address book state.

<puml src="diagrams/UndoRedoState1.puml" alt="UndoRedoState1" />

Step 3. The user executes `add n/David …​` to add a new athlete. The `add` command also calls `Model#commitAddressBook()`, causing another modified address book state to be saved into the `addressBookStateList`.

<puml src="diagrams/UndoRedoState2.puml" alt="UndoRedoState2" />

<box type="info" seamless>

**Note:** If a command fails its execution, it will not call `Model#commitAddressBook()`, so the address book state will not be saved into the `addressBookStateList`.
</box>

Step 4. The user now decides that adding the athlete was a mistake, and decides to undo that action by executing the `undo` command. The `undo` command will call `Model#undoAddressBook()`, which will shift the `currentStatePointer` once to the left, pointing it to the previous address book state, and restores the address book to that state.

<puml src="diagrams/UndoRedoState3.puml" alt="UndoRedoState3" />


<box type="info" seamless>

**Note:** If the `currentStatePointer` is at index 0, pointing to the initial AddressBook state, then there are no previous AddressBook states to restore. The `undo` command uses `Model#canUndoAddressBook()` to check if this is the case. If so, it will return an error to the user rather
than attempting to perform the undo.
</box>

The following sequence diagram shows how an undo operation goes through the `Logic` component:

<puml src="diagrams/UndoSequenceDiagram-Logic.puml" alt="UndoSequenceDiagram-Logic" />

<box type="info" seamless>

**Note:** The lifeline for `UndoCommand` should end at the destroy marker (X), but due to a limitation of PlantUML, it continues to the end of the diagram.
</box>

Similarly, how an undo operation goes through the `Model` component is shown below:

<puml src="diagrams/UndoSequenceDiagram-Model.puml" alt="UndoSequenceDiagram-Model" />

The `redo` command does the opposite — it calls `Model#redoAddressBook()`, which shifts the `currentStatePointer` once to the right, pointing to the previously undone state, and restores the address book to that state.

<box type="info" seamless>

**Note:** If the `currentStatePointer` is at index `addressBookStateList.size() - 1`, pointing to the latest address book state, then there are no undone AddressBook states to restore. The `redo` command uses `Model#canRedoAddressBook()` to check if this is the case. If so, it will return an error to the user rather than attempting to perform the redo.
</box>

Step 5. The user then decides to execute the command `list`. Commands that do not modify the address book, such as `list`, will usually not call `Model#commitAddressBook()`, `Model#undoAddressBook()` or `Model#redoAddressBook()`. Thus, the `addressBookStateList` remains unchanged.

<puml src="diagrams/UndoRedoState4.puml" alt="UndoRedoState4" />

Step 6. The user executes `clear`, which calls `Model#commitAddressBook()`. Since the `currentStatePointer` is not pointing at the end of the `addressBookStateList`, all address book states after the `currentStatePointer` will be purged. Reason: It no longer makes sense to redo the `add n/David …` command. This is the behavior that most modern desktop applications follow.

<puml src="diagrams/UndoRedoState5.puml" alt="UndoRedoState5" />

The following activity diagram summarizes what happens when a user executes a new command:

<puml src="diagrams/CommitActivityDiagram.puml" width="250" />

#### Design considerations:

**Aspect: How undo & redo execute:**

* **Alternative 1 (current choice):** Saves the entire address book.
  * Pros: Easy to implement.
  * Cons: May have performance issues in terms of memory usage.

* **Alternative 2:** Individual command knows how to undo/redo by
  itself.
  * Pros: Will use less memory (e.g. for `delete`, just save the athlete being deleted).
  * Cons: We must ensure that the implementation of each individual command is correct.

_{more aspects and alternatives to be added}_

### \[Proposed\] Data archiving

_{Explain here how the data archiving feature will be implemented}_


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

**Requirements scope**: This appendix records the intended product requirements, including features beyond the minimum viable product (MVP). It is not a statement that all features are implemented. The MVP comprises adding, listing, and permanently deleting athletes, automatic local persistence, and clear command feedback. An athlete's required MVP fields are name, age category, phone number, and email address. Age-category filtering is a nice-to-have enhancement beyond the required MVP.

The longer-term scope includes roster editing and searching, event organization, guardian links, athlete logs, and recovery tools. Cloud synchronization and multi-user access were considered but are excluded from the selected single-user, local product. A coach operates their own roster; athletes and guardians are records, not application users.

### MVP feature responsibilities

Age-category filtering (US11) is a medium-priority nice-to-have enhancement. Coaches can maintain and review their roster using add, list, and delete; filtering makes reviewing a competition category faster. Its scope includes category validation, match counts, feedback for empty results, compatibility with displayed indexes, restoring the roster with `list`, tests, and user-guide examples. The delete feature owner is responsible for implementing deletion.

Automatic local persistence remains required application behavior (US04, US07, US08). The team shares responsibility for persistence integration; persistence is not a standalone individual feature assignment. AB3 provides basic persistence, name search, editing, help, and command parsing. The parsing owner's scope is to improve parsing and validation to meet TrackFlow rules, including required fields, supported categories, duplicate parameters, and actionable errors.

### User stories

Priorities: `* * *` = high (essential to the core workflow), `* *` = medium (useful enhancements), `*` = low (optional future capabilities). **MVP** identifies the initial scope; **Future** retains a requirement beyond that scope without committing it to a semester release; **Excluded** records a considered idea outside the selected product. Priority does not imply implementation status.

| ID | Priority | Scope | As a ... | I want to ... | So that I can ... |
|----|----------|-------|----------|---------------|------------------|
| US01 | `* * *` | MVP | coach | add an athlete's name, age category, phone number, and email | maintain the essential information needed to contact and organize athletes |
| US02 | `* * *` | MVP | coach | view all athletes and sort them by useful fields and directions | locate athletes and select the correct displayed record |
| US03 | `* * *` | MVP | coach | permanently delete one selected athlete | remove a record I no longer need |
| US04 | `* * *` | MVP | returning coach | recover saved roster changes when I reopen TrackFlow | continue work without re-entering athletes |
| US05 | `* * *` | MVP | coach | receive clear success messages and actionable errors | know whether a command worked and correct mistakes |
| US06 | `* * *` | MVP | coach | have matching-name records with a shared phone or email rejected, and receive warnings for differently named records sharing contacts | avoid redundant entries without excluding different athletes |
| US07 | `* * *` | MVP | coach | keep my previous roster unchanged when a change cannot be saved | avoid believing an unsaved update is permanent |
| US08 | `* * *` | MVP | coach | have unreadable saved data preserved separately when loading fails | retain the possibility of recovering it while starting a new roster |
| US09 | `* *` | Future | coach | edit an athlete's details directly | correct information without deleting and re-entering the record |
| US10 | `* *` | Future | coach | find athletes using partial values from any recorded field | locate records without scanning the full roster or remembering an exact value |
| US11 | `* *` | Future | coach | filter athletes by age category | review athletes in a competition category |
| US12 | `* *` | Future | coach | assign and remove event-specialization tags for athletes | identify athletes who train for particular events and keep their specializations current |
| US13 | `* *` | Future | new coach using TrackFlow | view built-in command help | learn or recall how to operate the application |
| US14 | `* *` | Future | frequent user | recall previously entered commands | reduce repeated typing |
| US15 | `* *` | Future | coach | archive and restore athlete records | keep departed athletes' information without including them in the active roster |
| US16 | `* *` | Future | coach | record guardians and add or remove their links to athletes | find the appropriate family contact and keep those links current |
| US17 | `* *` | Future | coach | organize athletes into relay squads and event groups | review who belongs to each team or event |
| US18 | `* *` | Future | coach | record and review personal bests by event | track an athlete's performance progress |
| US19 | `* *` | Future | coach | record and review relevant medical notes | consult recorded considerations when planning training |
| US20 | `* *` | Future | coach | record and review competition eligibility | identify athletes recorded as eligible for an event |
| US21 | `* *` | Future | coach | record and review participation logs | track athletes' involvement in training or competitions |
| US22 | `* *` | Future | coach | undo and redo roster changes | recover from accidental changes or reapply them |
| US23 | `* *` | Future | coach | view an athlete's contact details, event tags, guardian links, and status notes together | prepare for a meet without searching through separate records |
| US25 | `* *` | Future | coach | view all event-specialization tags currently used in the roster | review which disciplines the team covers |
| US26 | `* *` | Future | coach | view the contact information of athletes in an event group | communicate practice or meet updates to that group |
| US27 | `* *` | Future | coach | link multiple guardians to one athlete | retain alternative contacts when a guardian is unavailable |
| US28 | `* *` | Future | coach managing siblings | link one guardian record to multiple athletes | update shared contact details only once |
| US29 | `* *` | Future | coach organizing travel | identify athletes without a guardian phone number | collect missing emergency contact information before departure |
| US31 | `* *` | Future | relay coach | assign or replace athletes on specific relay legs | maintain a clear running order and adjust the lineup when availability changes |
| US32 | `* *` | Future | relay coach | designate alternate runners for a relay squad | record replacements before a meet |
| US34 | `* *` | Future | coach | view an athlete's relay-squad and event-group memberships | review the athlete's assignments before making changes |
| US35 | `* *` | Future | coach | correct an inaccurate personal-best or status log entry | keep the athlete's recorded history reliable |
| US36 | `*` | Future | coach | import and export roster records in bulk | transfer my own records without entering each one manually |
| US37 | `*` | Future | coach | store names with characters beyond the MVP's supported set | preserve athletes' preferred name spellings |
| US38 | `*` | Future | coach | store multiple phone numbers and phone extensions | retain alternative ways of contacting an athlete |
| US39 | `*` | Future | coach | check whether an email address can receive mail | distinguish a structurally valid address from a reachable one |
| US40 | `*` | Future | keyboard-oriented coach | close TrackFlow with a text command | finish a session from the command box |
| US41 | `*` | Excluded | coach using several computers | synchronize my roster through cloud storage | access the same changes on different devices |
| US42 | `*` | Excluded | coach working with other coaches | share and collaboratively edit a roster | coordinate updates with colleagues |

Email reachability checking (US39) is a considered optional enhancement, not part of the MVP's structural email validation. Its design must preserve offline access to the roster and must not require a TrackFlow-operated remote server.

### Use cases

For every use case below, the **system** is TrackFlow and the **primary actor** is the coach. **MSS** means main success scenario. These use cases describe intended behavior, not verified implementation. UC01–UC04 are within the required MVP; UC05 describes the nice-to-have age-category filtering enhancement. Other future requirements remain recorded in the user stories.

#### UC01: Register an athlete

**Related stories**: US01, US02, US05, US06, US07.<br>
**Precondition**: TrackFlow is open.<br>
**Success postcondition**: One complete athlete record is saved and displayed in the roster.

**MSS**

1. The coach requests to view the roster.
2. TrackFlow displays the roster with current indexes, or indicates that it is empty.
3. The coach submits the new athlete's name, age category, phone number, and email address.
4. TrackFlow validates the input, checks for a duplicate, and saves the new record.
5. TrackFlow refreshes the alphabetical roster and displays the added athlete's details.

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
2. TrackFlow displays the alphabetical roster and current indexes.
3. The coach checks the athlete's details and requests deletion using the athlete's displayed index.
4. TrackFlow saves the deletion, refreshes and renumbers the roster, and displays the deleted athlete's details.

Use case ends. The MVP has no deletion confirmation, archive, or undo operation.

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

#### UC03: Correct an athlete's details in the MVP

**Related stories**: US01, US02, US03, US05.<br>
**Precondition**: TrackFlow is open and the athlete is in the roster.<br>
**Success postcondition**: The incorrect record is replaced by a saved record containing the corrected details.

**MSS**

1. The coach requests to list athletes.
2. TrackFlow displays the roster with details and current indexes.
3. The coach notes the details to retain and removes the incorrect record using UC02.
4. The coach registers the athlete with corrected details using UC01.
5. TrackFlow displays the saved replacement in the roster.

Use case ends. Direct editing (US09) is a future enhancement.

**Extensions**

* 3a. Deletion fails.
  * 3a1. TrackFlow retains the original record and reports the error as described in UC02.
  * Use case ends.
* 4a. Adding the corrected record fails.
  * 4a1. TrackFlow reports the error as described in UC01. The earlier successful deletion remains in effect; these are two separate operations.
  * The coach may correct the input and resume at step 4. If the coach stops, the deleted record remains absent.

#### UC04: Continue working with a saved roster

**Related stories**: US04, US07, US08.<br>
**Precondition**: TrackFlow is open and a roster change has been saved successfully.<br>
**Success postcondition**: On reopening, the coach sees the saved roster, including successful additions and deletions.

**MSS**

1. The coach closes TrackFlow.
2. The coach launches TrackFlow again.
3. TrackFlow loads the saved roster and displays the number of athletes loaded.
4. The coach requests to list athletes.
5. TrackFlow displays the saved athletes with their current indexes.

Use case ends.

**Extensions**

* 3a. No saved roster exists, as on first use.
  * 3a1. TrackFlow starts with an empty roster and explains that no saved roster was found.
  * Use case resumes at step 4.
* 3b. The saved roster cannot be recovered.
  * 3b1. TrackFlow explains the loading failure and starts with an empty roster while preserving the unrecovered data.
  * 3b2. Subsequent successful changes create a new active roster without replacing the preserved data. TrackFlow does not claim the old records were restored; the preserved copy remains until the coach removes it outside TrackFlow.
  * Use case resumes at step 4.

#### UC05: Filter athletes by age category

**Scope**: Nice-to-have enhancement beyond the required MVP.<br>
**Related stories**: US02, US03, US05, US11.<br>
**Precondition**: TrackFlow is open.<br>
**Success postcondition**: Only matching athletes are displayed; stored records are unchanged.

**MSS**

1. The coach enters `filter a/AGE_CATEGORY` with one supported category.
1. TrackFlow searches the full roster and displays matching athletes with current indexes and the match count.
1. The coach enters `list` to restore the complete roster, or uses the displayed indexes with existing commands.

**Extensions**

* **1a.** The category is missing, invalid, repeated, or accompanied by extra arguments. TrackFlow reports the error and retains the previous display and roster.
* **2a.** No athletes match, including when the roster is empty. TrackFlow displays an empty list and an explicit zero-match message. `list` remains available.

### Non-Functional Requirements

These are product requirements and acceptance targets, not claims about the current build. Platform, packaging, storage, and display requirements reflect the relevant [course product constraints](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-constraints.html).

| ID | Requirement |
|----|-------------|
| NFR01 | TrackFlow shall run on Windows, Linux, and macOS with Java 25 installed, without requiring another Java version. |
| NFR02 | TrackFlow shall be distributed as a single JAR that runs without an installer or separately installed application dependencies other than Java 25. |
| NFR03 | Core roster operations and local persistence shall work without internet access, an account, or a TrackFlow-operated remote server. |
| NFR04 | TrackFlow shall support one coach using a local roster. Shared accounts, simultaneous editing, and another user's access to the data file during normal operation are outside its scope. |
| NFR05 | Roster data shall use locally stored, human-editable text files rather than a database management system. Valid manual edits made while TrackFlow is closed shall be loaded on the next launch. |
| NFR06 | Adding, listing, and deleting athletes shall be possible entirely by keyboard. Successful commands shall clear and refocus the command box; failed commands shall retain their text for correction. |
| NFR07 | Success and failure shall be communicated in text, without relying on color alone. Validation failures shall identify the input problem and shall not change the roster. |
| NFR08 | A change shall be reported as successful only after it has been saved. A save failure shall leave the observable roster unchanged. Successfully saved changes shall survive normal closure and reopening. |
| NFR09 | A loading failure shall not silently overwrite unrecovered data. That data shall remain separately preserved even if the coach makes changes to the new empty roster. |
| NFR10 | As a performance target, with 1,000 athlete records on a computer with a dual-core processor, 8 GB RAM, and local SSD storage, at least 95 of 100 consecutive add, list, or delete commands shall update the display within two seconds of submission, with persistence included for changes. |
| NFR11 | At 1920 × 1080 or higher with 100% and 125% display scaling, the interface shall avoid clipping essential controls and feedback. At 1280 × 720 or higher with 150% scaling, all functions shall remain accessible, including through scrolling or resizing where necessary. |
| NFR12 | For identical input and roster state, validation shall report the same first error. Listing shall use a stable ordering by normalized name, age category, normalized phone, and normalized email, in that order. |

### Glossary

| Term | Definition |
|------|------------|
| Active roster | The athlete records currently maintained and displayed by the coach, excluding any separately preserved unrecovered data and, if archiving is introduced, archived records. |
| Age category | A coach-assigned competition grouping: Under 14, Under 16, Under 18, Under 20, or Open. The MVP stores the category, not an exact age or date of birth, and does not verify competition eligibility. |
| Athlete | A student-athlete whose contact and age-category information is recorded in TrackFlow. An athlete does not log in to the application. |
| Archive / restore | A future capability to remove a record from the active roster while retaining it, and later return it to the active roster. This differs from permanent deletion. |
| CLI / GUI | Command-line interface / graphical user interface. TrackFlow accepts typed commands in a graphical window and uses that window to display records and feedback. |
| Displayed index | An athlete's positive, one-based position in the current displayed list. It is not a permanent identifier and may change after roster updates. |
| Duplicate athlete record | A record with the same normalized name and either the same phone or email as another record, regardless of age category. Differently named records sharing contacts are allowed with a possible-duplicate warning on add or edit. |
| Eligibility | Whether an athlete meets the recorded participation conditions for a competition or event. Eligibility logs are a future feature, distinct from assigning an age category. |
| Event specialization | An athletics discipline, such as sprinting or long jump, associated with an athlete through a future tagging feature. |
| Guardian | A parent or other responsible adult whose future contact record may be linked to one or more athletes. |
| Local persistence | Automatically saving roster changes on the coach's computer and loading them in a later application session. |
| MVP | Minimum viable product: the initial complete add, list, delete, and save/reload workflow with validation and command feedback. |
| Normalized field | A value prepared for comparison: surrounding whitespace is removed; name and age-category internal spaces are collapsed; name, category, and email comparisons ignore letter case; phone comparisons ignore permitted formatting characters and use the digits. Display formatting may be retained. |
| Participation log | A future record of an athlete's involvement in training or competition. |
| Personal best | An athlete's best recorded result for a particular event, intended for a future performance log. |
| Relay squad / event group | A future grouping of athletes for a relay team or a particular athletics event. |
| Unrecovered data | Previously saved roster data that TrackFlow could not load and preserves separately for possible recovery. Preserving it does not mean that its records have been restored. |

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

   1. Double-click the JAR file.<br>
      Expected: The GUI opens with a set of sample contacts. The window size may not be optimal.

1. Saving window preferences

   1. Resize the window to an optimal size. Move the window to a different location. Close the window.

   1. Relaunch the app by double-clicking the JAR file.<br>
       Expected: The most recent window size and location are retained.

1. _{ more test cases … }_

### Deleting an athlete

1. Deleting an athlete while all athletes are being shown

   1. Prerequisites: List all athletes using the `list` command, with multiple athletes in the list.

   1. Test case: `delete 1`<br>
      Expected: The first contact is deleted from the list. The status message shows the deleted contact's details.

   1. Test case: `delete 0`<br>
      Expected: No athlete is deleted. The status message shows error details.

   1. Other incorrect delete commands to try: `delete`, `delete x`, `...` (where x is larger than the list size)<br>
      Expected: Similar to previous.

1. _{ more test cases … }_

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

### Saving data

1. Dealing with missing/corrupted data files

   1. _{Explain how to simulate missing or corrupted data files and state the expected behavior.}_

1. _{ more test cases … }_
