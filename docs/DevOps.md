---
  layout: default.md
  title: "DevOps guide"
  pageNav: 3
---

# DevOps guide

<!-- * Table of Contents -->
<page-nav-print />

<!-- -------------------------------------------------------------------------------------------------------------------- -->

## Build automation

This project uses Gradle for **build automation and dependency management**. **We recommend reading [this Gradle tutorial from se-edu/guides](https://se-education.org/guides/tutorials/gradle.html).**


Use the [Zulu JDK 25 setup](SettingUp.md#runtime-and-build-requirements). The following commands perform common Gradle tasks.


* **`clean`**: Deletes the files created during the previous build tasks (e.g. files in the `build` folder).<br>
  For example: `./gradlew clean`

* **`shadowJar`**: Uses the Shadow plugin to create the fat JAR file `build/libs/TrackFlow.jar`.<br>
  For example: `./gradlew shadowJar`

* **`run`**: Builds and runs the application.<br>
  **`runShadow`**: Builds the application as a fat JAR, then runs it.

* **`checkstyleMain`**: Runs the code style check for the main code base.<br>
  **`checkstyleTest`**: Runs the code style check for the test code base.

* **`test`**
  * `./gradlew test`: Runs all tests.
  * `./gradlew clean test`: Cleans the project before running all tests

--------------------------------------------------------------------------------------------------------------------

## Continuous integration (CI)

The workflow files are in `.github/workflows`. `docs.yml` runs MarkBind deployment on pushes to `master`
and supports manual runs through `workflow_dispatch`. Java CI has separate triggers in `gradle.yml`:
pushes and pull requests, with jobs on Linux, macOS, and Windows. Java CI does not declare `workflow_dispatch`.

### Code coverage

As part of CI, Gradle generates JaCoCo coverage reports from the tests, and CI uploads the coverage data to Codecov. Codecov then provides information about test coverage.

The Linux coverage upload uses `codecov/codecov-action@v7`. Its
[`fail_ci_if_error` input](https://github.com/codecov/codecov-action/blob/v7/action.yml) defaults to `false`,
and `gradle.yml` does not override it. Codecov upload errors therefore do not fail CI by default; setting
`fail_ci_if_error: true` makes those errors fail the step. This behavior is controlled by the action's input,
not inferred from the absence of `continue-on-error`. Check the upload logs separately from the Gradle test
and coverage results. The local JaCoCo report remains available in `build/reports/jacoco/`.

To enable Codecov for forks of this project, follow the steps given in [this se-edu guide](https://se-education.org/guides/tutorials/codecov.html).

### Repository-wide checks

In addition to Gradle checks, CI runs repository-wide checks. Unlike Gradle checks, which cover files used in the build, these checks cover every repository file and enforce rules that are hard to apply on development machines, such as line-ending requirements.

These checks are implemented as POSIX shell scripts, and thus can only be run on POSIX-compliant operating systems such as macOS and Linux. To run all checks locally on these operating systems, execute the following in the repository root directory:

`./.github/run-checks.sh`

Any warnings or errors will be printed out to the console.

**If adding new checks:**

* Checks are implemented as executable `check-*` scripts within the `.github` directory. The `run-checks.sh` script will automatically pick up and run files named as such. That is, you can add more such files if you need and the CI will do the rest.

* Check scripts should print out errors in the format `SEVERITY:FILENAME:LINE: MESSAGE`
  * SEVERITY is either ERROR or WARN.
  * FILENAME is the path to the file relative to the current directory.
  * LINE is the line of the file where the error occurred and MESSAGE is the message explaining the error.

* Check scripts must exit with a non-zero exit code if any errors occur.

--------------------------------------------------------------------------------------------------------------------

## Making a release

Here are the steps to create a new release.

1. Ensure `MainApp.VERSION` is `v1.4` and the Gradle project version is `1.4`.
1. Run `./gradlew clean check coverage shadowJar` and the repository checks. Build the documentation using the
   [documentation guide](Documentation.md).
1. Run `java -jar TrackFlow.jar` from a disposable folder containing a copy of `build/libs/TrackFlow.jar`.
   Follow the [manual testing instructions](DeveloperGuide.md#appendix-instructions-for-manual-testing),
   including saving changes and restarting with the runtime from the [setup guide](SettingUp.md#runtime-and-build-requirements).
1. Confirm Java CI and MarkBind Action have passed for the final `master` commit. Resolve any failed or missing runs.
1. Tag that verified commit `v1.4` and create the GitHub release. Upload `TrackFlow.jar` and state the exact tested
   runtime requirements and any remaining limitations in the release notes.
