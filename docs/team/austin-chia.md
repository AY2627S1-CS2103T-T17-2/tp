---
  layout: default.md
  title: "Austin Chia - Project Portfolio"
---

# Austin Chia - Project Portfolio

### Project: TrackFlow
TrackFlow is a desktop application for track and field coaches and sports administrators to manage their athlete rosters efficiently. It provides a fast, CLI-optimized interface alongside a clean graphical view to track athletes, contact details, ages, and performance tags.

### Code contributions
- **Search & Sort Logic**: Implemented the `sort reset` mechanism to restore the roster list to its default saved order. Fixed name search spacing mechanisms and remark feedback consistency.
- **Age-Category Filtering**: Created the age-category architecture and updated the `filter` command logic to support finding athletes by specific age brackets.
- **Validation**: Improved duplicate handling and implemented phone-note duplicate detection (AF01) to prevent silent collisions in the athlete roster.

### Testing
- Wrote unit tests for age-category model filtering and verification of the sort-reset feature. Ensured duplicate detection mechanisms were thoroughly covered.

### Documentation
- **User Guide**: Consistently updated the UG across versions (v1.1 to v1.4). Authored the usage guidelines for age-category filtering.
- **Developer Guide**: Authored the unified `ModelClassDiagram` UML to reflect roster interface changes and collection relationships.
- **DevOps**: Pinned MarkBind configurations and actively fixed GitHub Actions docs deployment workflow (`docs.yml`).

### Project management and team contributions
- **Role**: Documentation
- Monitored and assigned documentation workload among team members to keep styling and templates consistent. Fixed Markdown pipeline compilation issues before release deadlines.
