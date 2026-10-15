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


**Converting to PDF**

* See the guide [_se-edu/guides **Saving web documents as PDF files**_](https://se-education.org/guides/tutorials/savingPdf.html).
