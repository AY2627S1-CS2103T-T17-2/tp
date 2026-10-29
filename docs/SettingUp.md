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

   Follow the [SE-EDU Java coding standard (Basic + Intermediate)](https://se-education.org/guides/conventions/java/intermediate.html).
   The repository's `config/checkstyle/checkstyle.xml` checks a subset of these rules; review comments for accuracy
   as well as checking their format.

   If using IDEA, follow the guide [_[se-edu/guides] IDEA: Configuring the code style_](https://se-education.org/guides/tutorials/intellijCodeStyle.html) to set up IDEA's coding style to match ours.

   <box type="tip" seamless>

   **Tip:**
    Optionally, follow the guide [_[se-edu/guides] Using Checkstyle_](https://se-education.org/guides/tutorials/checkstyle.html) to learn how to use Checkstyle in IDEA, for example to report problems _as_ you write code.
   </box>

1. **Enable assertions**

   In IntelliJ's run configuration for `trackflow.Main`, add `-ea` to the VM options. Gradle's `run` task
   enables assertions, and Gradle tests enable them by default. For a direct development launch, use
   `java -ea -jar TrackFlow.jar`. Validate user input with exceptions; use assertions for internal invariants.

1. **Follow Git conventions**

   The course requires the [SE-EDU Git commit subject conventions](https://se-education.org/guides/conventions/git.html):
   use an imperative, capitalized subject, aim for 50 characters, never exceed 72, and omit a final period.
   A commit body is optional; when present, separate it with a blank line and wrap paragraphs at 72 characters.
   Advanced Java rules, the Markdown style guide, and the Google documentation style guide are optional under the
   [course standards](https://nus-cs2103-ay2627-s1.github.io/website/admin/standardsAndConventions.html).

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

--------------------------------------------------------------------------------------------------------------------

## Documenting code changes

Write descriptive Javadoc headers for classes, public constructors, and public methods. Simple getters/setters,
test code, and overrides whose inherited documentation applies exactly may omit a header. An override with a
different contract should explain it, using `{@inheritDoc}` where helpful.

Start method summaries with a verb such as "Returns", "Creates", or "Updates". Explain behavior callers need to
know: accepted values, normalization, empty or null handling, side effects, returned views or copies, and failures.
Keep comments consistent with the implementation; do not promise validation, persistence, or features that the
method does not provide. Use English with American spelling.

Use `@param` for every parameter or omit the tags when the description already explains all parameters.
Include useful `@return` and `@throws` descriptions. Keep the opening `/**` on its own line, separate the summary
from block tags with a blank line, and end tag descriptions with punctuation. Link related API types with
`{@link ...}` so generated documentation can resolve them.

Run the following from the repository root before opening a documentation PR:

```bash
./gradlew check javadoc
```

On Windows, use `gradlew.bat`. Open `build/docs/javadoc/index.html` to inspect the generated API documentation.
The Javadoc tool can also report optional missing tags or comments on simple accessors/constants; those warnings
are separate from the course standard's documented exceptions. Resolve malformed markup and broken references,
and manually verify that each changed header describes the current behavior.

Checkstyle enforces only part of the standard. During review, also check comment accuracy, athlete-facing
vocabulary, American spelling in Java comments, verb-based method names, and wrapped-line indentation
(8 spaces beyond the original line). Preserve documented storage keys when changing human-readable wording.
Both Checkstyle tasks fail on warnings as well as errors. Javadoc opening markers must be on their own line.
