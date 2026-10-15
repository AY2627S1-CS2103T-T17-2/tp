---
  layout: default.md
  title: "Setting up and getting started"
  pageNav: 3
---

# Setting up and getting started

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## Runtime and build requirements

Use **Azul Zulu JDK 25** for development and release verification. Configure `JAVA_HOME`, the IntelliJ project SDK,
and the Gradle JVM to point to that installation. Confirm `java -version` and `./gradlew --version` both use Java 25
before building. Do not use JDK 17 or assume a later major Java version has been tested.

On macOS, select the **JDK FX** distribution, which includes JavaFX, and match the runtime to your processor
(Apple Silicon or Intel). Java CI requests Zulu 25 with `java-package: jdk+fx`. The build also bundles JavaFX 17.0.7
for Windows, Linux, and Intel macOS. It does not declare a separate Apple Silicon JavaFX dependency, so the packaged
JAR must be checked with the prescribed runtime on each supported architecture before release. A successful unit
test run does not establish GUI runtime compatibility.

## Setting up the project on your computer

<box type="warning" seamless>

**Caution:** Follow the steps below precisely. The setup may fail if you skip or change a step.
</box>

First, **fork** this repo, and **clone** the fork into your computer.

If you plan to use IntelliJ IDEA (highly recommended):

1. **Configure the JDK**: Follow the guide [_[se-edu/guides] IDEA: Configuring the JDK_](https://se-education.org/guides/tutorials/intellijJdk.html) to configure both the project SDK and Gradle JVM to use **Azul Zulu JDK 25** (JDK FX on macOS).
1. **Import the project as a Gradle project**: Follow the guide [_[se-edu/guides] IDEA: Importing a Gradle project_](https://se-education.org/guides/tutorials/intellijImportGradleProject.html) to import the project into IDEA.
   <box type="warning" seamless>
   Note: Importing a Gradle project is slightly different from importing a normal Java project.
   </box>
1. **Verify the setup**:
   1. Run `trackflow.Main` and try a few commands.
   1. [Run the tests](Testing.md) to ensure they all pass.

--------------------------------------------------------------------------------------------------------------------

## Before writing code

1. **Configure the coding style**

   If using IDEA, follow the guide [_[se-edu/guides] IDEA: Configuring the code style_](https://se-education.org/guides/tutorials/intellijCodeStyle.html) to set up IDEA's coding style to match ours.

   <box type="tip" seamless>

   **Tip:**
    Optionally, follow the guide [_[se-edu/guides] Using Checkstyle_](https://se-education.org/guides/tutorials/checkstyle.html) to learn how to use Checkstyle in IDEA, for example to report problems _as_ you write code.
   </box>

1. **Set up CI**

   This project includes GitHub Actions configuration files in `.github/workflows`. Java CI is configured for pushes and pull requests; MarkBind deployment is configured for pushes to `master`.
   Check the Actions page for a run against the exact release commit. Configuration alone does not confirm that a run occurred.

1. **Learn the design**

   When you are ready to start coding, we recommend that you get some sense of the overall design by reading about [TrackFlow’s architecture](DeveloperGuide.md#architecture).

1. **Read the tutorials**
   These upstream tutorials explain the original project. Their class names and paths may differ from TrackFlow;
   use `trackflow.Main`, `TrackFlowParser`, `AthleteRoster`, and `JsonRosterStorage` in this repository.

   * [Tracing code](https://se-education.org/guides/tutorials/ab3TracingCode.html)
   * [Adding a new command](https://se-education.org/guides/tutorials/ab3AddRemark.html)
   * [Removing fields](https://se-education.org/guides/tutorials/ab3RemovingFields.html)
