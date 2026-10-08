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

1. Ensure that Java `25` or later is installed on your computer.<br>
   **Mac users:** Ensure you have the precise JDK version prescribed [here](https://se-education.org/guides/tutorials/javaInstallationMac.html).

1. Download the latest `.jar` file from
   [the TrackFlow releases page](https://github.com/AY2627S1-CS2103T-T17-2/tp/releases).

1. Copy the file to the folder you want to use as the _home folder_ for TrackFlow.

1. Open a terminal, `cd` to the folder containing the JAR file, and run `java -jar addressbook.jar`.<br>
   A GUI similar to the one below should appear in a few seconds. Note how the app contains some sample data.<br>
   ![Ui](images/Ui.png)

1. Type a command in the command box and press Enter to execute it. For example, type **`help`** and press Enter to open the help window.<br>
   Some example commands you can try:

   * `list` : Lists all athletes.

   * `add n/John Doe a/Open p/98765432 e/johnd@example.com addr/123 Main Street` : Adds an athlete named `John Doe` with age category `Open`.

   * `filter a/Under 14`: Shows athletes in that age category. Use `list` to show everyone again.

   * `sort name`: Sorts the displayed athletes by name in ascending order.

   * `delete 3` : Deletes the 3rd athlete shown in the current list.

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

* Parameters can be in any order.<br>
  For example, if the command specifies `n/NAME p/PHONE_NUMBER`, `p/PHONE_NUMBER n/NAME` is also acceptable.

* Command words are case-insensitive. For example, `LIST`, `List`, and `list` invoke the same command. Parameter
  prefixes remain lowercase and case-sensitive; for example, use `n/NAME`, not `N/NAME`.

* The `list` command does not accept parameters. Extra text supplied to `help`, `clear`, or `exit` is currently ignored.

* Leading and trailing whitespace around a command is ignored.

* If you are using a PDF version of this document, be careful when copying and pasting commands that span multiple lines as space characters surrounding line-breaks may be omitted when copied over to the application.
</box>

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
* Names retain the existing rule: nonblank alphanumeric characters and spaces only.
* Phones retain the existing rule: digits only, with at least three digits. Formatted numbers such as `+65 9123 4567` are not supported in this version.
* Emails retain the existing email validation rules.
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

This Basic Add version retains lowercase parameter prefixes. Command words are case-insensitive. Use `sort` to change
the displayed order. Rollback after a save failure is deferred; a save failure can leave a change visible in memory
without retaining it on disk.

### Listing all athletes: `list`

Shows the complete athlete roster.

Format: `list`

The command also restores the complete roster after a `find` or `filter` command. It does not accept parameters.

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
* Category values ignore case and repeated spaces: `filter a/under   14` is equivalent to `filter a/Under 14`. The prefix must be lowercase `a/`.
* Matching uses the recorded category, not an exact age or eligibility calculation. `Under 16` does not include athletes recorded as `Under 14`.
* Each `filter` searches the entire roster and replaces any previous `find` or `filter`. Likewise, `find` replaces the age filter. Matching athletes retain their roster order and receive indexes starting from 1.
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
* A new remark replaces the existing remark; remarks are not appended.
* Use an empty remark, such as `remark 1 r/`, to remove the athlete's existing remark.
* Updating a remark preserves the athlete's age category and other details.

Examples:

* `remark 1 r/100m personal best: 12.34s`
* `remark 2 r/` removes the 2nd athlete's remark.

### Finding athletes across all fields: `find`

Finds athletes when any recorded field contains any of the given keywords.

Format: `find KEYWORD [MORE_KEYWORDS]`

* Searchable fields are name, age category, phone, email, address, remark, and tags.
* Matching is case-insensitive and accepts partial values. For example, `find ave` matches `Avery Tan`, and
  `find 9123` matches a phone number such as `91234567`.
* Each space-separated value is a keyword. An athlete is returned when at least one keyword occurs in at least one
  searchable field (an `OR` search).
* Keyword order does not matter. Duplicate matching fields do not cause an athlete to appear more than once.
* Each `find` searches the entire roster and replaces any previous `find` or `filter`. Matching athletes retain their
  roster order and receive indexes starting from 1. Use `list` to restore the complete roster.
* An empty or whitespace-only search is rejected and displays the accepted command format.

Feedback:

* No matches: `No matching athletes found.`
* One match: `Displaying 1 matching athlete.`
* Multiple matches: `Displaying COUNT matching athletes.`

Examples:

* `find ave` searches partial names and can return `Avery Tan`.
* `find under` searches age categories such as `Under 14` and `Under 16`.
* `find 9123` searches partial phone numbers.
* `find tan@exam` searches partial email addresses.
* `find sprint` searches addresses, remarks, and tags as well as the other fields.
* `find avery 9123 under` returns athletes matching any of the three keywords.

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

### Exiting the program: `exit`

Exits the program.

Format: `exit`

### Saving the data

TrackFlow automatically saves data after every successful command. You do not need to save manually.

### Editing the data file

TrackFlow data is saved automatically as a JSON file `[JAR file location]/data/addressbook.json`. Advanced users are
welcome to update data directly by editing that data file.

<box type="warning" seamless>

**Caution:**
If your changes make the data file invalid, TrackFlow starts with an empty roster at the next run. The invalid file
remains on disk until you run a successful command. Still, we recommend backing up the file before editing it.<br>
Furthermore, certain edits can cause TrackFlow to behave unexpectedly (e.g., if a value entered is outside of the
acceptable range). Therefore, edit the data file only if you are confident that you can update it correctly.
</box>

### Archiving data files `[coming in v2.0]`

_Details coming soon ..._

--------------------------------------------------------------------------------------------------------------------

## FAQ

**Q**: How do I transfer my data to another computer?<br>
**A**: Install TrackFlow on the other computer and overwrite the data file it creates with the data file from your
previous TrackFlow home folder.

--------------------------------------------------------------------------------------------------------------------

## Known issues

1. **When using multiple screens**, if you move the application to a secondary screen, and later switch to using only the primary screen, the GUI will open off-screen. The remedy is to delete the `preferences.json` file created by the application before running the application again.
2. **If you minimize the Help Window** and then run the `help` command again, the original Help Window will remain minimized, and no new Help Window will appear. The remedy is to manually restore the minimized Help Window.

--------------------------------------------------------------------------------------------------------------------

## Command summary

Action     | Format, Examples
-----------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------
**Add**    | `add n/NAME a/AGE_CATEGORY p/PHONE_NUMBER e/EMAIL addr/ADDRESS` <br> e.g., `add n/James Ho a/Under 18 p/22224444 e/jamesho@example.com addr/123 Main Street`
**Clear**  | `clear`
**Delete** | `delete INDEX`<br> e.g., `delete 3`
**Edit**   | `edit INDEX [n/NAME] [a/AGE_CATEGORY] [p/PHONE_NUMBER] [e/EMAIL] [addr/ADDRESS]`<br> e.g.,`edit 2 n/James Lee e/jameslee@example.com`
**Filter** | `filter a/AGE_CATEGORY`<br> e.g., `filter a/Under 14`
**Find**   | `find KEYWORD [MORE_KEYWORDS]`<br> e.g., `find avery 9123 under`
**List**   | `list`
**Remark** | `remark INDEX r/[REMARK]`<br> e.g., `remark 1 r/100m personal best: 12.34s`
**Sort**   | `sort FIELD [ORDER]`<br> e.g., `sort age desc`
**Help**   | `help`
**Exit**   | `exit`
