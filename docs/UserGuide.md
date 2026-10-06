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

   * `add n/John Doe a/Open p/98765432 e/johnd@example.com` : Adds an athlete named `John Doe` with age category `Open`.

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
  For example, `n/NAME [t/TAG]` can be used as `n/John Doe t/friend` or as `n/John Doe`.

* Items followed by `...` can appear zero or more times.<br>
  For example, `[t/TAG]... ` may be omitted, or written as `t/friend` or `t/friend t/family`.

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

Shows a message explaining how to access the help page.

![help message](images/helpMessage.png)

Format: `help`


### Adding an athlete: `add`

Adds an athlete with a name, age category, phone number, and email address.

Format: `add n/NAME a/AGE_CATEGORY p/PHONE_NUMBER e/EMAIL`

* All four fields are required and may appear in any order. Each parameter may appear only once.
* Age category must be `Under 14`, `Under 16`, `Under 18`, `Under 20`, or `Open`. Category input ignores case and normalizes repeated spaces.
* Names retain the existing rule: nonblank alphanumeric characters and spaces only.
* Phones retain the existing rule: digits only, with at least three digits. Formatted numbers such as `+65 9123 4567` are not supported in this version.
* Emails retain the existing email validation rules.
* A duplicate is rejected only when normalized name, age category, phone, and email all match. Name comparisons ignore case and repeated spaces; email comparisons ignore case. Shared names or family contact details alone are allowed.
* Add does not accept addresses, tags, or remarks. Existing `edit` and `remark` commands can add those details afterward. **`a/` means age category for `add`, but address for `edit`.** Editing or adding a remark preserves the athlete's age category; category editing is not available yet.
* Athletes are saved automatically. Older saved entries without an age category load as `Open`, retaining their
  existing address, tags, and remarks.

Examples:

* `add n/Avery Tan a/Under 14 p/91234567 e/avery.tan@example.com`
* `add e/jordan.lee@example.com p/92345678 a/open n/Jordan Lee`

Successful output:

`New athlete added: Avery Tan; Age category: Under 14; Phone: 91234567; Email: avery.tan@example.com`

A duplicate produces `This athlete already exists in the roster: Avery Tan.` Failed validation leaves the command text available for correction.

This Basic Add version retains lowercase parameter prefixes and insertion-order display. Command words are
case-insensitive. Alphabetical sorting and rollback after a save failure are deferred; a save failure can leave a
change visible in memory without retaining it on disk.

### Listing all athletes: `list`

Shows the complete athlete roster.

Format: `list`

The command also restores the complete roster after a `find` command. It does not accept parameters.

* An empty roster displays `The athlete roster is empty.`
* A roster with one athlete displays `Displaying 1 athlete.`
* A larger roster displays `Displaying COUNT athletes.`, where `COUNT` is the number of athletes displayed.
* Supplying a parameter, such as `list 1`, displays
  `The list command does not accept parameters. Format: list`.

Examples:

* `list`
* `LiSt` (command words are case-insensitive)

### Editing an athlete: `edit`

Edits an existing athlete in the roster.

Format: `edit INDEX [n/NAME] [p/PHONE] [e/EMAIL] [a/ADDRESS] [t/TAG]... `

* Edits the athlete at the specified `INDEX`. The index refers to the index number shown in the displayed athlete list. The index **must be a positive integer** 1, 2, 3, ...
* At least one of the optional fields must be provided.
* Existing values will be updated to the input values.
* When editing tags, all of the athlete's existing tags are removed; adding tags is not cumulative.
* To remove all of an athlete's tags, enter `t/` without a tag after it.

Examples:
*  `edit 1 p/91234567 e/johndoe@example.com` Edits the phone number and email address of the 1st athlete to be `91234567` and `johndoe@example.com` respectively.
*  `edit 2 n/Betsy Crower t/` Edits the name of the 2nd athlete to be `Betsy Crower` and clears all existing tags.

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

### Locating athletes by name: `find`

Finds athletes whose names contain any of the given keywords.

Format: `find KEYWORD [MORE_KEYWORDS]`

* The search is case-insensitive; for example, `hans` matches `Hans`.
* Keyword order does not matter; for example, `Hans Bo` matches `Bo Hans`.
* The search considers only names.
* Only full words match; for example, `Han` does not match `Hans`.
* Athletes matching at least one keyword are returned (an `OR` search); for example, `Hans Bo` returns `Hans Gruber` and `Bo Yang`.

Examples:
* `find John` returns `john` and `John Doe`
* `find alex david` returns `Alex Yeoh`, `David Li`<br>
  ![result for 'find alex david'](images/findAlexDavidResult.png)

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
2. **If you minimize the Help Window** and then run the `help` command (or use the `Help` menu, or the keyboard shortcut `F1`) again, the original Help Window will remain minimized, and no new Help Window will appear. The remedy is to manually restore the minimized Help Window.

--------------------------------------------------------------------------------------------------------------------

## Command summary

Action     | Format, Examples
-----------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------
**Add**    | `add n/NAME a/AGE_CATEGORY p/PHONE_NUMBER e/EMAIL` <br> e.g., `add n/James Ho a/Under 18 p/22224444 e/jamesho@example.com`
**Clear**  | `clear`
**Delete** | `delete INDEX`<br> e.g., `delete 3`
**Edit**   | `edit INDEX [n/NAME] [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [t/TAG]... `<br> e.g.,`edit 2 n/James Lee e/jameslee@example.com`
**Find**   | `find KEYWORD [MORE_KEYWORDS]`<br> e.g., `find James Jake`
**List**   | `list`
**Remark** | `remark INDEX r/[REMARK]`<br> e.g., `remark 1 r/100m personal best: 12.34s`
**Help**   | `help`
**Exit**   | `exit`
