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

Install Java 25 and Graphviz (`dot` on `PATH`) to render PlantUML diagrams. Open the generated `docs/_site/index.html`
and inspect the User Guide, Developer Guide, and diagrams for readable text, clipping, and broken links.
The deployment workflow selects MarkBind `^7.2.1`, while `docs/package.json` specifies `^5.1.0`; both environments
must be checked before release until the release owner aligns them.

**Style guidance:**

* Follow the [course documentation guidance](https://nus-cs2103-ay2627-s1.github.io/website/se-book-adapted/chapters/documentation.html): write for the intended reader, keep examples concrete, and maintain the guides alongside behavior changes.
* Follow the [**_Google developer documentation style guide_**](https://developers.google.com/style).
* Use `[Future]` or `[Projected]` for unimplemented features. Do not assign a release version unless the team has committed it.
* Describe verified behavior as current functionality. Label acceptance targets and known release gaps explicitly.
* Keep class names and source links consistent with `src/main/java/trackflow`; keep diagram sources beside their references.
* Follow the [Java coding standard](https://se-education.org/guides/conventions/java/intermediate.html) for Java examples and the [Git conventions](https://se-education.org/guides/conventions/git.html) for commits.
* Also relevant is the [_se-edu/guides **Markdown coding standard**_](https://se-education.org/guides/conventions/markdown.html).


**Converting to PDF**

* See the guide [_se-edu/guides **Saving web documents as PDF files**_](https://se-education.org/guides/tutorials/savingPdf.html).
