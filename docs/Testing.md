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

## Release verification

Run `./gradlew clean check coverage shadowJar` to build `build/libs/TrackFlow.jar`, run the automated tests,
and check Java style. On Windows, use `gradlew.bat` in place of `./gradlew`.

For focused duplicate/equality checks, run:

```bash
./gradlew test --tests trackflow.model.person.PersonTest --tests trackflow.logic.DuplicatePersistenceTest
```

Automated results are in `build/reports/tests/test/index.html`; Checkstyle reports are in
`build/reports/checkstyle/`. Verify guide examples and startup behavior using the
[manual testing procedures](DeveloperGuide.md#appendix-instructions-for-manual-testing). Record the tested commit,
platform, runtime, and outcome in the [release checklist](DeveloperGuide.md#release-verification-checklist).
A failed or unperformed check remains a release item; do not mark it passed from documentation review alone.
