---
  layout: default.md
  title: "User Guide"
  pageNav: 3
---

# TrackFlow User Guide

TrackFlow is a **desktop application for managing a track-and-field athlete roster**. It is optimized for coaches who
prefer typing commands through a Command Line Interface (CLI), while retaining the benefits of a Graphical User
Interface (GUI).

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## Quick start

1. Install **Azul Zulu JDK 25**. On macOS, use the **JDK FX** distribution, which includes JavaFX,
   and select the package matching your processor (Apple Silicon or Intel). See the
   [setup guide](SettingUp.md#runtime-and-build-requirements) for runtime requirements and packaging limitations.

1. Download the latest `.jar` file from
   [the TrackFlow releases page](https://github.com/AY2627S1-CS2103T-T17-2/tp/releases).

1. Copy the file to the folder you want to use as the _home folder_ for TrackFlow.

1. Open a terminal, `cd` to the folder containing the JAR file, and run `java -jar TrackFlow.jar`.<br>
   The GUI opens with sample athletes. The image below is a design mockup: its grade/event fields and command examples do not represent v1.3. Use the commands documented in this guide.<br>
   ![TrackFlow design mockup](images/Ui.png)

1. Type a command in the command box and press Enter to execute it. For example, type **`help`** and press Enter to open the help window.<br>
   Some example commands you can try:

   * `list` : Lists all athletes.

   * `add n/John Doe a/Open p/98765432 e/johnd@example.com addr/123 Main Street` : Adds an athlete named `John Doe` with age category `Open`.

   * `filter a/Under 14`: Shows athletes in that age category. Use `list` to show everyone again.

   * `sort name`: Sorts the displayed athletes by name in ascending order.

   * Run `list`, then `delete 3`: Displays the complete roster and deletes the athlete shown at index 3. This requires at least three athletes in the roster.

   * `clear` : Deletes all athletes.

   * `exit` : Exits the app.

1. Refer to the [Features](#features) section below for details of each command.

--------------------------------------------------------------------------------------------------------------------

## Features

<box type="info" seamless>

**Notes about the command format:**<br>

* Words in `UPPER_CASE` are the parameters to be supplied by the user.<br>
  For example, in `add n/NAME`, replace `NAME` with a value such as `John Doe`.

* Items in square brackets are optional.<br>
  For example, `edit INDEX [n/NAME]` can be used as `edit 1 n/John Doe` or as part of an edit with other fields.

* Items followed by `...` can appear zero or more times.<br>
  For example, `find KEYWORD [MORE_KEYWORDS]...` can include `Avery` or `Avery Tan`.

* Field prefixes can be in any order. For `find`, put unprefixed keywords before field prefixes.<br>
  For example, if the command specifies `n/NAME p/PHONE_NUMBER`, `p/PHONE_NUMBER n/NAME` is also acceptable.

* Command words and parameter prefixes are case-insensitive. For example, `LIST`, `List`, and `list` invoke the same
  command, while `n/Avery` and `N/Avery` supply the same name parameter.

* The `list`, `help`, `clear`, and `exit` commands do not accept parameters.

* Leading and trailing whitespace around a command is ignored.

* If you are using a PDF version of this document, be careful when copying and pasting commands that span multiple lines as space characters surrounding line-breaks may be omitted when copied over to the application.
</box>

### Command autocomplete

TrackFlow provides an autocomplete feature to speed up typing and ensure correct command formats.

* As you type a command word in the command box, a dropdown menu will appear with suggested commands, including mandatory index parameters and prefixes (e.g., `edit INDEX n/ a/ p/ e/ addr/`, `delete INDEX`, `remark INDEX r/`). Suggestions are matched case-insensitively.
* Use the **Up** and **Down** arrow keys to navigate the list of suggestions.
* Press **Tab**, or **Click** on a suggestion, to select it. This will insert the command word (followed by a space) into the command box and close the suggestions dropdown.
* Pressing **Enter** does **not** select or apply a suggestion; it closes the suggestions dropdown and executes the command currently typed in the command box.
* After you enter the command word (or select it with **Tab**), press **Tab** again to automatically insert the next prefix in the sequence. After you type the data for a parameter (or an index, if required), press **Tab** again to insert the next prefix. You can keep pressing **Tab** to step through all the prefixes for the command.
* Prefixes already entered in the command box (even in uppercase or in a different order) are recognized and skipped automatically.

### Command feedback and errors

After a successful command, TrackFlow displays a confirmation message and clears the command box. If parsing,
validation, or command execution fails, TrackFlow displays an error and leaves the entered command in the command box
so that it can be corrected.

* Submitting an empty or whitespace-only command displays
  `No command entered. Type a command and try again.`
* An unrecognized command displays `Unknown command: COMMAND.`, where `COMMAND` is the command word entered. For
  example, `enrol n/Avery` displays `Unknown command: enrol.`
* Validation errors explain the accepted format or constraint. The roster is not changed when parsing or validation
  fails.
* Unsupported parameters such as `t/` are rejected with an error. See [Known issues](#known-issues)
  for limitations when slash-containing text resembles a prefix.

### Viewing help: `help`

Opens a resizable, scrollable help window listing every supported command, its purpose,
parameters, and examples.

Format: `help`


### Adding an athlete: `add`

Adds an athlete with a name, age category, phone number, email address, and address.

Format: `add n/NAME a/AGE_CATEGORY p/PHONE_NUMBER e/EMAIL addr/ADDRESS`

* All five fields are required.
* Fields may appear in any order. Each parameter may appear only once.
* Age category must be `Under 14`, `Under 16`, `Under 18`, `Under 20`, or `Open`. Category input ignores case and normalizes repeated spaces.
* Names must be nonblank and contain only alphanumeric characters, spaces, hyphens, and apostrophes (for example, `Mary-Jane` or `O'Brien`).
* Phone numbers must contain at least three digits. They may also include spaces, the characters `+`, `-`, `(`, `)`, and notes in brackets such as `(HP)` (for example, `+65 9123 4567` or `9123 4567 (HP)`). Two numbers with the same digits are treated as the same number, so the brackets and spaces are ignored when checking for duplicates.
* Emails must have the form `local-part@domain`. The local part consists of alphanumeric groups separated by `+`, `_`, `.`, or `-`. Domain labels consist of alphanumeric groups separated by hyphens; the final label must have at least two characters. Spaces are not allowed.
* Add and edit reject a duplicate when the normalized name matches and either the phone or email matches, regardless of age category. Name comparisons ignore case and repeated spaces; email comparisons ignore case. A different name sharing a phone or email is allowed with a possible-duplicate warning naming the matching athletes. Both checks search the full roster.
* Use `addr/ADDRESS` to supply the required nonblank address.
* Add does not accept tags or remarks. `edit` updates athlete details and addresses, and `remark` updates remarks. **`a/` means age category for `add`, `edit`, and `filter`.** Use `addr/` with `edit` to update addresses.
* Athletes are saved automatically. Older saved entries without an age category load as `Open`, retaining their
  existing address, tags, and remarks. Saved rosters are checked using the same duplicate rule on loading.

Examples:

* `add n/Avery Tan a/Under 14 p/91234567 e/avery.tan@example.com addr/123 Main Street`
* `add e/jordan.lee@example.com p/92345678 a/open n/Jordan Lee addr/123 Main Street`

Successful output:

`New athlete added: Avery Tan; Age category: Under 14; Phone: 91234567; Email: avery.tan@example.com`

A duplicate produces `This athlete already exists in the roster: Avery Tan.` Failed validation leaves the command text available for correction.

Command words and parameter prefixes are case-insensitive. Use `sort` to change the displayed order. TrackFlow reports
a roster-changing command as successful only after saving it. If saving fails, the roster and displayed list remain
unchanged so that the application never presents an unsaved change as permanent.

### Listing all athletes: `list`

Shows the complete athlete roster.

Format: `list`

The command also restores the complete roster after a `find` or `filter` command, retaining the current sort order.
Without an active sort, athletes appear in saved roster order. It does not accept parameters.

* An empty roster displays `The athlete roster is empty.`
* A roster with one athlete displays `Displaying 1 athlete.`
* A larger roster displays `Displaying COUNT athletes.`, where `COUNT` is the number of athletes displayed.
* Supplying a parameter, such as `list 1`, displays
  `The list command does not accept parameters. Format: list`.

Examples:

* `list`
* `LiSt` (command words are case-insensitive)

### Filtering athletes by age category: `filter`

Use `filter` to show athletes whose recorded age category matches the category you specify.

Format: `filter a/AGE_CATEGORY`

* Supply exactly one `a/` parameter. Accepted categories are `Under 14`, `Under 16`, `Under 18`, `Under 20`, and `Open`.
* Category values ignore case and repeated spaces: `filter a/under   14` is equivalent to `filter A/Under 14`.
* Matching uses the recorded category, not an exact age or eligibility calculation. `Under 16` does not include athletes recorded as `Under 14`.
* Each `filter` searches the entire roster and replaces any previous `find` or `filter`. Likewise, `find` replaces the age filter. Matching athletes retain the current displayed ordering, including any active sort, and receive indexes starting from 1.
* Filtering does not change or delete athlete records. Use `list` to show everyone again, including after no matches. TrackFlow does not save the filter between application sessions.
* Missing `a/` shows command usage. An empty or unsupported category (for example, `filter a/Under 15`) shows `Age category must be Under 14, Under 16, Under 18, Under 20, or Open.` Repeated `a/` parameters and extra arguments are rejected. An invalid filter leaves the previous display and roster unchanged.

Feedback examples:

* Two matches: `Displaying 2 athletes in age category Under 14.`
* One match: `Displaying 1 athlete in age category Under 14.`
* No matches (including an empty roster): `No athletes found in age category Under 14 (0 matches).` The displayed list is empty; this is a successful search, not an error.

To filter the roster and return to the complete list, follow these steps:

1. Run `filter a/Under 14`. TrackFlow displays only athletes recorded as `Under 14`.
1. Review the displayed athletes. If you use an indexed command, use the indexes in this filtered list. An index outside the displayed list is invalid even if it exists in the full roster.
1. Run `list` to display the complete roster again.

### Editing an athlete: `edit`

Edits an existing athlete in the roster.

Format: `edit INDEX [n/NAME] [a/AGE_CATEGORY] [p/PHONE] [e/EMAIL] [addr/ADDRESS]`

* Edits the athlete at the specified `INDEX`. The index refers to the index number shown in the displayed athlete list. The index **must be a positive integer** 1, 2, 3, ...
* At least one of the optional fields must be provided.
* Existing values will be updated to the input values.
* Omitted fields retain their current values. Stored tags and remarks are preserved.
* `a/` now means age category, replacing the old address syntax. Supported categories are `Under 14`, `Under 16`, `Under 18`, `Under 20`, and `Open`; case and extra whitespace are normalized.
* Use `addr/ADDRESS` to edit the address. An empty or whitespace-only address is rejected.
* Repeated field prefixes, empty or invalid values, and `t/` or `r/` parameters are rejected.
* Edits that create a duplicate athlete are rejected using the same normalized name and contact comparison as `add`.
* A successful edit displays the full roster again; failed edits leave the roster and displayed list unchanged.

Examples:

*  `edit 1 addr/123 Main Street` Updates the 1st athlete's address.
*  `edit 1 p/91234567 e/johndoe@example.com` Edits the phone number and email address of the 1st athlete to be `91234567` and `johndoe@example.com` respectively.
*  `edit 2 n/Betsy Crower a/Under 16` Edits the name and age category of the 2nd athlete, retaining their other details.

### Adding or removing an athlete remark: `remark`

Replaces the remark of an athlete in the currently displayed list.

Format: `remark INDEX r/[REMARK]`

* `INDEX` refers to the index number shown in the displayed athlete list and must be a positive integer.
* Exactly one `r/` prefix is required. The prefix is case-insensitive.
* A new remark replaces the existing remark; remarks are not appended.
* Use an empty remark, such as `remark 1 r/`, to remove the athlete's existing remark.
* Remarks form part of the saved athlete record and remain after restarting TrackFlow.
* Updating a remark preserves the athlete's age category and other details.
* A successful remark update displays the full roster again and retains the current sort order.

Examples:

* `remark 1 r/100m personal best: 12.34s`
* `remark 2 r/` removes the 2nd athlete's remark.

### Finding athletes across all or selected fields: `find`

Finds athletes using broad keywords, field-specific values, or both.

Format: `find [KEYWORD]... [n/NAME] [a/AGE_CATEGORY] [p/PHONE] [e/EMAIL] [addr/ADDRESS] [r/REMARK]`

* Searchable fields are name, age category, phone, email, address, and remark.
* Matching is case-insensitive and accepts partial values.
* Before any field prefix, each space-separated value is a keyword. An athlete is returned when at least one keyword occurs in at least one
  searchable field (an `OR` search). This broad form remains useful when the field is unknown.
* Prefix a value to restrict it to one field: `n/` name, `a/` age category, `p/` phone, `e/` email, `addr/` address,
  or `r/` remark. For example, `find n/und` searches names without matching every `Under` age category.
* When several field prefixes are given, an athlete must match **every** one (an `AND` search). For example,
  `find n/avery a/Under 16` returns only athletes named like `avery` **and** in the `Under 16` category.
* Broad and field-specific criteria can be combined. An athlete is returned when at least one broad keyword matches
  **and** every field-specific value matches. Each field prefix may appear at most once and cannot have an empty value.
* Broad keyword order does not matter: `find Avery Tan` and `find Tan Avery` return the same results. A prefixed value is matched as one phrase, so `find n/Avery Tan` matches that name but `find n/Tan Avery` does not. Put broad keywords before any field prefixes. Each athlete appears at most once.
* Each `find` searches the entire roster and replaces any previous `find` or `filter`. Matching athletes retain the
  current displayed ordering, including any active sort, and receive indexes starting from 1. Use `list` to restore the complete roster.
* An empty or whitespace-only search is rejected and displays the accepted command format.

Feedback:

* No matches: `No matching athletes found.`
* One match: `Displaying 1 matching athlete.`
* Multiple matches: `Displaying COUNT matching athletes.`

Examples:

* `find ave` searches partial names and can return `Avery Tan`.
* `find under` searches age categories such as `Under 14` and `Under 16`.
* `find n/und` searches only names, so an athlete named `Underwood` can be found without matching age categories.
* `find a/under 16` searches only age categories.
* `find 9123` searches partial phone numbers.
* `find p/9123` searches only phone numbers.
* `find tan@exam` searches partial email addresses.
* `find r/injured` searches only remarks.
* `find avery 9123 under` returns athletes matching any of the three keywords.
* `find n/avery r/relay` returns athletes whose name contains `avery` or whose remark contains `relay`.

### Sorting the displayed athletes: `sort`

Sorts the currently displayed athlete list by a selected field.

Format: `sort FIELD [ORDER]`

Available fields:

* `name` sorts names alphabetically without considering letter case.
* `age` sorts competition categories in this order: `Under 14`, `Under 16`, `Under 18`, `Under 20`, `Open`.
* `phone` sorts phone numbers numerically rather than alphabetically.
* `email` sorts email addresses alphabetically without considering letter case.
* `address` sorts addresses alphabetically without considering letter case. Empty addresses appear first in ascending
  order and last in descending order.

Available orders are `asc` or `ascending`, and `desc` or `descending`. If `ORDER` is omitted, ascending order is used.
Field and order names are case-insensitive.

Sorting changes only the displayed order; it does not rewrite the saved roster order. The selected ordering remains
active for the rest of the application session, including after `find`, `filter`, `list`, and roster-changing commands
such as `add`, `edit`, `remark`, and `delete`. New or edited athletes automatically move to the correct position in the
displayed list. A later `sort` command replaces the selected ordering. Indexed commands such as `edit`, `remark`, and
`delete` use the indexes shown in the sorted list.

Examples:

* `sort name` sorts names in ascending order.
* `sort name desc` sorts names in descending order.
* `sort age ascending` places `Under 14` before the older categories and `Open`.
* `sort age desc` reverses the category order.
* `sort phone` sorts by numeric phone value.

Successful output follows this format:
`Sorted the displayed athlete list by FIELD in ORDER order.`

Invalid sort commands leave the displayed list unchanged. Examples of errors include:

* `sort` or `sort name asc extra`: displays the accepted command format because a field is missing or there are too
  many arguments.
* `sort height`: displays `Unknown sort field: height. Available fields: name, age, phone, email, address.`
* `sort name upwards`: displays `Unknown sort order: upwards. Use asc, ascending, desc, or descending.`

### Deleting an athlete: `delete`

Deletes the specified athlete from the roster.

Format: `delete INDEX`

* Deletes the athlete at the specified `INDEX`.
* The index refers to the index number shown in the displayed athlete list.
* The index **must be a positive integer** 1, 2, 3, ...
* On success, the deleted athlete's details are shown, e.g. `Athlete deleted: Avery Tan; Age category: Under 18; Phone: 91234567; Email: avery@example.com`.

Examples:

* `list` followed by `delete 2` deletes the 2nd athlete in the roster.
* `find Betsy` followed by `delete 1` deletes the 1st athlete in the results of the `find` command.

### Clearing all entries: `clear`

Deletes all athletes from the roster.

Format: `clear`

The command does not accept parameters.

### Exiting the program: `exit`

Exits the program.

Format: `exit`

The command does not accept parameters.

### Saving the data

TrackFlow automatically saves after every successful command that changes roster data: `add`, `edit`, `remark`,
`delete`, and `clear`. Commands that only change the displayed view or open a window do not rewrite the data file.
TrackFlow writes the complete update to a temporary file before replacing the existing roster, so an interrupted write
does not leave a partially written roster when atomic replacement is supported by the filesystem. On other
filesystems, TrackFlow moves the completed temporary file into place without an atomicity guarantee.
If saving fails, the command reports an error and the in-memory roster, filter, and sort order remain unchanged.
Fix the file or folder permissions, then retry the command. `list`, `find`, `filter`, `sort`, `help`, and `exit` remain
available without saving the roster. You do not need to save manually.

### Editing the data file

TrackFlow saves roster data to `data/trackflow.json`, relative to the folder from which you launch the application.
Following the quick-start instructions places it beside the JAR in the `data` subfolder. Close TrackFlow and make a
separate backup before editing this file manually.

<box type="warning" seamless>

**Caution:**
If the saved roster cannot be loaded, TrackFlow preserves the original file in
`data/trackflow.json.unreadable-<UUID>.bak`, logs the backup path, and starts with an empty roster.
Later roster changes leave the backup intact. If the backup cannot be created, TrackFlow stops startup to
protect the original file. Keep the preserved copy if you need to recover its records manually.
</box>

### Archiving data files [Future]

Archiving and restoring individual athlete records are not available in v1.3. Keep external copies of roster files
when you need a historical snapshot. No release version is committed for in-app archiving.

--------------------------------------------------------------------------------------------------------------------

## FAQ

**Q**: How do I transfer my data to another computer?<br>
**A**: Install TrackFlow on the other computer and overwrite the data file it creates with the data file from your
previous TrackFlow home folder. Close TrackFlow on both computers first and back up any destination roster before
replacing it. Launch from the destination home folder so TrackFlow reads the copied `data/trackflow.json`.

--------------------------------------------------------------------------------------------------------------------

## Known issues

* **Slash-containing input:** a single-letter token followed by `/`, such as `A/B` in an address, may be interpreted
  as an unsupported parameter. For broad searches, `find wrong/value` is treated as literal text, while
  `find x/value` is rejected as an unsupported prefix.

* **Runtime packaging:** the bundled JavaFX dependencies do not include an Apple Silicon classifier. Use the Zulu
  JDK 25 FX runtime on macOS and verify the packaged JAR on your machine. See the
  [setup guide](SettingUp.md#runtime-and-build-requirements) for runtime requirements.


--------------------------------------------------------------------------------------------------------------------

## Command summary

Action     | Format, Examples
-----------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------
**Add**    | `add n/NAME a/AGE_CATEGORY p/PHONE_NUMBER e/EMAIL addr/ADDRESS` <br> e.g., `add n/James Ho a/Under 18 p/22224444 e/jamesho@example.com addr/123 Main Street`
**Clear**  | `clear`
**Delete** | `delete INDEX`<br> e.g., `delete 3`
**Edit**   | `edit INDEX [n/NAME] [a/AGE_CATEGORY] [p/PHONE_NUMBER] [e/EMAIL] [addr/ADDRESS]`<br> e.g.,`edit 2 n/James Lee e/jameslee@example.com`
**Filter** | `filter a/AGE_CATEGORY`<br> e.g., `filter a/Under 14`
**Find**   | `find [KEYWORD]... [n/NAME] [a/AGE_CATEGORY] [p/PHONE] [e/EMAIL] [addr/ADDRESS] [r/REMARK]`<br> e.g., `find n/avery r/relay`
**List**   | `list`
**Remark** | `remark INDEX r/[REMARK]`<br> e.g., `remark 1 r/100m personal best: 12.34s`
**Sort**   | `sort FIELD [ORDER]`<br> e.g., `sort age desc`
**Help**   | `help`
**Exit**   | `exit`
