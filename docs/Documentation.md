---
  layout: default.md
  title: "Documentation guide"
  pageNav: 3
---

# Documentation Guide

* We use [**MarkBind**](https://markbind.org/) to manage documentation.
* The `docs/` folder contains the source files for the documentation website.
* To learn how set it up and maintain the project website, follow the guide [[se-edu/guides] Working with Forked MarkBind sites](https://se-education.org/guides/tutorials/markbind-forked-sites.html) for project documentation.

## Building and checking the site

Use the dependency versions recorded in `docs/package-lock.json` for a reproducible local build:

```bash
cd docs
npm ci
npm run build
```

Use the [Zulu JDK 25 setup](SettingUp.md#runtime-and-build-requirements) and install Graphviz (`dot` on `PATH`) to render PlantUML diagrams. Open the generated `docs/_site/index.html`
and inspect the User Guide, Developer Guide, and diagrams for readable text, clipping, and broken links.

**Style guidance:**

* Follow the [**_Google developer documentation style guide_**](https://developers.google.com/style).
* Keep class names and source links consistent with `src/main/java/trackflow`; keep diagram sources beside their references.
* Also relevant is the [_se-edu/guides **Markdown coding standard**_](https://se-education.org/guides/conventions/markdown.html).

## UML diagram conventions

Use the [CS2103/T UML reference sheet](https://nus-cs2103-ay2627-s1.github.io/website/admin/uml-reference-sheet.html)
as the notation reference. The colours, fonts, and layout choices below are TrackFlow conventions. The shared
`diagrams/style.puml` controls appearance; each author must check the meaning of relationships against the Java code.
Apply these conventions when updating a diagram and keep the corresponding DG explanation consistent.

### Shared appearance

* Include `style.puml` relative to the diagram: `!include style.puml` for sources in `docs/diagrams/`, or
  `!include ../style.puml` for sources one directory deeper. Change common colours and fonts in the shared file.
* Use the existing component palette. Class and participant text is white on the base component colour;
  explicitly labelled interfaces use the component's `T1` shade with dark text and borders. The shared style
  also provides readable fonts, notes, method visibility symbols, and generic parameter boxes.

| Component | Base colour macro | Interface background macro |
| --- | --- | --- |
| UI | `UI_COLOR` | `UI_COLOR_T1` |
| Logic and parsing | `LOGIC_COLOR` | `LOGIC_COLOR_T1` |
| Model | `MODEL_COLOR` | `MODEL_COLOR_T1` |
| Storage | `STORAGE_COLOR` | `STORAGE_COLOR_T1` |

* Keep the shared `hide circle` setting: use explicit UML keywords instead of PlantUML's circled class-type icons.
  Members are hidden by default. Use `show members` or a targeted command such as `show Parser members` after
  declaring the class when those details help explain the design. Do not reduce font sizes to squeeze in more content.

### Class diagram notation

* Use current Java class names and signatures. A partial diagram may omit unrelated classes, attributes, or
  operations; identify its scope in a legend or the DG. Remove obsolete names and visible placeholder classes.
* Show interfaces with `<<interface>>` and a dashed realization line ending in a hollow triangle at the interface.
  Use a solid line with a hollow triangle pointing to the superclass for class inheritance.
* Mark abstract classes with `{abstract}` or italics, and mark abstract operations with PlantUML's `{abstract}`
  modifier. Use `{static}` for class-level members so that they render underlined. Use `+`, `-`, `#`, and `~`
  for public, private, protected, and package visibility. Keep attributes and operations in their respective compartments.
* Use the dashed type-parameter box for generics. It may extend above the class box; that is intentional UML notation.
  Keep its text and border visible, with enough space to avoid covering the class name or `<<interface>>` label.
  Do not disable it with `genericDisplay old`.
* Use solid associations for retained instance references and dashed dependencies for temporary use or creation.
  Mark creation dependencies with `<<create>>` when useful. Show an association as a line or an attribute, not both.
* Put multiplicities at the association ends they describe. Omit them when they are outside the diagram's scope;
  an omitted multiplicity is unspecified. Do not add multiplicities to inheritance or dependency arrows.
* Reserve a filled diamond at the whole for a justified whole-part composition. Construction or a `final` field
  alone does not establish composition. A hollow diamond denotes aggregation; use an ordinary association when
  a stronger whole-part meaning is not justified.
* Package boxes group the classes in the labelled package or component view. A referenced class may sit outside
  the box when it belongs elsewhere, such as `EditCommand` outside the parsing package. State any simplified grouping.

### Sequence diagram notation

* Distinguish `objectName:ClassName` or `:ClassName` participants from class-level participants labelled `<<class>>`
  for static calls. Use solid arrows for calls and dashed arrows for replies.
* Show activation bars while participants execute, including the caller while it waits for a synchronous call.
  Start a created object's lifeline at its creation message and end each depicted activation consistently with
  the return of control. Do not extend an activation through unrelated later calls.
* Return labels must describe actual returned values. Leave void replies unlabelled; constructor replies may also
  omit a label. Do not use a reply label to invent a status value for a method that returns `void`.
* Show destruction only when it occurs in the scenario. A local variable going out of scope does not establish
  that its object is destroyed. Explain partial traces, abbreviated arguments, omitted calls, and error paths in
  notes or adjacent DG text. Use guarded `alt`, `opt`, and `loop` frames for depicted branches or repetitions.
* A horizontal message may cross other lifelines on its way to the receiver. Keep its arrowhead, label, and
  destination clear; these crossings do not represent additional calls to the intermediate participants.
* Preserve literal command text in the rendered image. Escape formatting characters when needed, and verify that
  examples such as `A//B` still show both input slashes while the decoded result shows `A/B`.

### Layout and readability

* Arrange related classes in clear rows or columns. Reduce crossings and avoid lines passing through class boxes
  or labels. Remove redundant dependency arrows when signatures already convey the information and the diagram
  remains clear. Keep meaningful structural associations and explain the scope of any partial view.
* Use local layout hints where needed. The current class diagrams use `skinparam linetype polyline` and
  `skinparam ArrowFontStyle plain`. Hidden PlantUML links may constrain spacing or ordering, but must not introduce
  visible relationships or placeholder classes. Do not change a relationship's meaning to improve its position.
* Keep detailed explanations in the DG and use compact legends for diagram scope. Folded-corner notes may sit
  outside a package box, but their connectors should not obscure the main relationships.
* Size each DG image for the available page width. Check the embedded image as well as the full-size render;
  avoid clipping, excessive enlargement of PNGs, or shrinking text until it becomes unreadable. For a wide trace,
  provide a descriptive link to the generated PNG so readers can open it at full size.

### Before opening or updating a PR

1. Edit the `.puml` source for the diagrams you own and keep authorship accurate. Coordinate shared style changes
   with the team; leave other members' diagram sources for their owners unless a broader change is agreed.
1. Compare names, signatures, relationships, multiplicities, and message order with the current Java implementation.
   Confirm that every shown return value and branch matches the scenario.
1. Update the DG reference, descriptive `alt` text, and explanation in the same change. Show the scope of representative
   classes and abbreviated traces. Keep the image width consistent with the final layout. Keep self-closing `puml`
   embedding tags on one line with this MarkBind version; check that the headings and links after each diagram
   render correctly rather than appearing as raw Markdown.
1. Run the documentation build described above. A successful build checks rendering syntax; it does not establish
   semantic correctness. Visually inspect the diagrams for incorrect arrowheads, missing labels, faint borders,
   overlaps, clipping, and small text. After a shared style change, inspect every affected diagram.
1. Ask a teammate to review notation, code accuracy, and readability. Commit the source diagrams, shared style, and
   relevant documentation; generated previews and temporary rendering files are local review aids.

### Picking up the shared UML changes

1. After the shared UML/style PR is merged, update your local `master` from the team repository. Create your UML
   branch from that version, or bring those changes into an existing UML branch using the team's usual Git workflow.
1. Read this section and update the diagram sources allocated to you. Reuse `style.puml` rather than copying its
   definitions into another file. Change the component colour macros to match the component you are documenting.
1. Use `diagrams/LogicClassDiagram.puml` as an example of component relationships and abstract commands,
   `diagrams/ParserClasses.puml` for interfaces, generics, and helper dependencies, and
   `diagrams/tracing/LogicSequenceDiagram.puml` for static calls, object creation, and activation bars.
   Their code-specific relationships describe the Logic component; adapt the notation to your own implementation.
1. Update the matching DG section, build the site, inspect the result, and follow the PR checklist above. Keep the
   diagram source and its DG changes together so that reviewers can compare the code, picture, and explanation.


**Converting to PDF**

* See the guide [_se-edu/guides **Saving web documents as PDF files**_](https://se-education.org/guides/tutorials/savingPdf.html).

## Project portfolio pages

`team/johndoe.md` is an unpublished template excluded by `site.json`. Copy it to a file named after your GitHub
handle, replace the template instructions with your actual contributions, and add working evidence links before
publishing. Remove unused headings and do not copy example accomplishments as personal claims.
