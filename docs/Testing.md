---
  layout: default.md
  title: "Testing guide"
  pageNav: 3
---

# Testing guide

<!-- * Table of Contents -->
<page-nav-print />

<!-- -------------------------------------------------------------------------------------------------------------------- -->

## Running tests

Use the [Zulu JDK 25 setup](SettingUp.md#runtime-and-build-requirements) for both IntelliJ and Gradle.
You can run tests in two ways.

* **Method 1: Using IntelliJ JUnit test runner**
  * To run all tests, right-click on the `src/test/java` folder and choose `Run 'All Tests'`
  * To run a subset of tests, you can right-click on a test package,
    test class, or a test and choose the corresponding run action (for example, `Run 'PersonTest'`)
* **Method 2: Using Gradle**
  * Open a console and run the command `gradlew clean test` (Mac/Linux: `./gradlew clean test`)

<box type="info" seamless>

**Link**: Read [this Gradle Tutorial from the se-edu/guides](https://se-education.org/guides/tutorials/gradle.html) to learn more about using Gradle.
</box>

--------------------------------------------------------------------------------------------------------------------

## Types of tests

This project has three types of tests:

1. *Unit tests* target the lowest-level methods and classes.<br>
   For example: `trackflow.commons.util.StringUtilTest`
1. *Integration tests* check how multiple code units work together; the individual units are assumed to work.<br>
   For example: `trackflow.storage.StorageManagerTest`
1. *Hybrid tests* combine unit and integration testing. These tests check both the individual units and how they work together.<br>
   For example: `trackflow.logic.LogicManagerTest`

## Reading and reviewing tests

Test names describe the operation, scenario and expected outcome. Read the fixture setup first: each `@BeforeEach`
or `@TempDir` establishes the starting roster and isolates saved files from a real installation. An assertion on the
displayed list checks the view; it does not by itself establish that the complete roster or saved JSON is correct.

| Area | Useful starting points | What the checks establish |
| --- | --- | --- |
| Command syntax and escaping | `TrackFlowParserTest`, `ArgumentTokenizerTest`, `ParserRobustnessTest` | Accepted/rejected input, one-time slash decoding, and unchanged state after rejection |
| Search and indexed commands | `PersonContainsKeywordsPredicateTest`, `FilterCommandTest`, `SortCommandTest` | Broad OR versus scoped AND, replacement of filters, and indexes in the displayed order |
| Identity and saved records | `PersonTest`, `DuplicatePersistenceTest`, `JsonSerializableRosterTest` | Duplicate identity, complete equality, and decisions after reloading |
| Save failures and startup | `LogicManagerTest`, `MainAppTest`, `StartupProcessTest` | Publication only after saving, preservation of unreadable data, and application-home paths |
| Screen placement | `WindowPlacementTest` | Bounds calculations for screens and saved preferences; actual rendering still needs manual checks |

For failure tests, check the expected exception or feedback as well as the unchanged roster, view and saved file.
For persistence tests, look for a fresh storage read or model reconstruction rather than an assertion against only
the original in-memory object. Comments explain unusual inputs, isolation decisions and regression risks; ordinary
setup and self-explanatory assertions do not need a comment on every line.

The automated suite does not drive the complete JavaFX interface. Follow the DG's
[manual testing appendix](DeveloperGuide.md#appendix-instructions-for-manual-testing) on the final packaged JAR for
keyboard behaviour, rendering, scrolling, screen scaling and platform/runtime compatibility. Record the exact build
and environment tested; passing helper tests alone is not evidence of those UI properties.

## Build and focused tests

Run `./gradlew clean check coverage shadowJar` to build `build/libs/TrackFlow.jar`, run the automated tests,
and check Java style. On Windows, use `gradlew.bat` in place of `./gradlew`.

For focused duplicate/equality checks, run:

```bash
./gradlew test --tests trackflow.model.person.PersonTest --tests trackflow.logic.DuplicatePersistenceTest
```

For parser boundaries, double-slash escapes, literal paths, empty values, duplicate parameters, null inputs,
multiline input, large indexes, and rejected-command checks, run:

```bash
./gradlew test \
    --tests trackflow.logic.ParserRobustnessTest \
    --tests trackflow.logic.parser.ArgumentTokenizerTest \
    --tests trackflow.logic.parser.ParserUtilTest \
    --tests trackflow.logic.parser.TrackFlowParserTest
```

`ParserRobustnessTest` uses real JSON storage in a temporary folder to check that parsed values survive reload and
that invalid commands preserve the roster, current predicate/comparator, displayed athletes, and saved file.
The tests distinguish Java `null` API inputs from the literal text `null`, exercise duplicate parameters even when
one value is empty or escaped, and verify that restarts and unrelated edits do not decode stored text again.
The tokenizer tests cover every single-letter escape in both cases, multiple whitespace boundaries, long slash runs,
URLs, network paths, and ordinary quotes/backslashes. Parser tests check all 120 orders of the five add fields.
Unicode-boundary regressions cover non-breaking, figure, narrow non-breaking, and em spaces in command words,
prefixes, escaped text, broad searches, and sort arguments. They also check that whitespace inside addresses and
remarks survives JSON reload and that unknown or duplicate prefixes still fail without changing saved data.
Existing `JsonRosterStorageTest` and `MainAppTest` also check null JSON roots/entries and preservation of malformed data.

Name-sort regressions in `SortCommandTest` and `LogicManagerTest` cover repeated spaces, equivalent case/spacing
keys, ascending and descending order, and subsequent adds/edits. A fresh JSON read verifies that comparison-key
normalization does not rewrite name text or saved roster order.

Phone-sort regressions use the shared significant-digit rule: bracketed notes are ignored and `(+65)` country codes
are retained. They cover both directions, numeric notes, formatting, zero-valued and large numbers, add/edit updates,
and unchanged phone text after JSON reload.

For startup/preferences, strict JSON roots, application-home paths and unavailable file logging, run:

```bash
./gradlew test \
    --tests trackflow.MainAppTest \
    --tests trackflow.storage.JsonUserPrefsStorageTest \
    --tests trackflow.commons.core.GuiSettingsTest \
    --tests trackflow.commons.util.JsonUtilTest \
    --tests trackflow.commons.util.ApplicationHomeTest \
    --tests trackflow.StartupProcessTest
```

`StartupProcessTest` packages the compiled application classes into a temporary JAR and starts bounded child JVMs
with the normal test runtime dependencies. It checks recovery, saving and reopening from another directory, a home
path containing spaces and `%u`, temporary-file cleanup, and console fallback when the log path is blocked.
It initializes the application and runs real commands/storage without opening a GUI; it does not replace native
window tests or launch verification of the final release JAR on each supported runtime/platform.

Automated results are in `build/reports/tests/test/index.html`; Checkstyle reports are in
`build/reports/checkstyle/`. Verify guide examples and startup behavior using the
[manual testing procedures](DeveloperGuide.md#appendix-instructions-for-manual-testing).

## Week 11 release testing

For the [Week 11 v1.5 milestone](https://nus-cs2103-ay2627-s1.github.io/website/schedule/week11/project.html),
test the packaged JAR in a fresh writable folder with Java 25. Record the JAR version, OS, processor,
runtime, resolution and display scale alongside each result. Use the manual testing appendix for command,
save/restart, help-window and display checks; compare the result with the published UG.

Each member must submit at least five genuine issues labeled `alpha-bug`. Assign feature testing across the
team, fix or triage the reports, and repeat affected checks on the final JAR. Issue counts establish reporting
participation; they do not establish that every bug is fixed or that the release passed its smoke test.

Before releasing, check RepoSense attribution for each member and verify the final website and JAR against
the same source revision. Publish the v1.5 JAR and website by October 29, 2026, 23:59 Singapore time, then
reschedule or close outstanding milestone items and close the milestone. The course recommends a team beta
test of the released JAR and guides on October 30, 16:00–18:00 Singapore time.
