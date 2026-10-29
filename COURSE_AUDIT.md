# TrackFlow course compliance audit

Audit date: October 29, 2026 (Singapore). Branch: `fix-docs`. Current milestone: Week 11 / v1.5.

The local automated checks pass after the fixes below. Release readiness still depends on the open items in
this record, especially publishing v1.5 and testing the packaged GUI across supported environments.
This is a repository and public GitHub evidence review, not a guarantee of grading compliance.

## Applicable standards

The [course standards page](https://nus-cs2103-ay2627-s1.github.io/website/admin/standardsAndConventions.html)
requires the [basic and intermediate Java rules](https://se-education.org/guides/conventions/java/intermediate.html)
and [Git commit subject conventions](https://se-education.org/guides/conventions/git.html).
Commit bodies are optional, but must follow the basic body conventions when supplied. Advanced Java rules,
Markdown style, and Google documentation style are optional. The full Java guide therefore includes rules
beyond the required minimum. The Java guide refers uncovered topics to Google Java Style.

[Checkstyle](https://se-education.org/guides/tutorials/checkstyle.html) is a checking tool, not an additional
complete specification. Manual review remains necessary for meaningful names, accurate contracts and comments,
appropriate decomposition, and defensive design.

The original links also need the current [Week 11 requirements](https://nus-cs2103-ay2627-s1.github.io/website/schedule/week11/project.html),
the [tP constraints](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-constraints.html),
[deliverables](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-deliverables.html), and
[expectations](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-expectations.html).

## Repository checks and fixes

| Area | Evidence and result |
| --- | --- |
| Java formatting and structural rules | Both Checkstyle reports contain zero violations. The configuration checks naming, imports, braces, layout, indentation and Javadoc. |
| Warning enforcement | Added `maxWarnings = 0` so naming/layout warnings fail Gradle checks instead of passing CI. |
| Javadoc format | Expanded single-line Javadocs and added an explicit check for opening markers on their own line. Corrected the missing varargs summary in `CollectionUtil`. |
| Encapsulation | Made `PersonCard.person` private; no caller uses the field directly. |
| Required associations | `LogicManager`, `StorageManager`, JSON storage constructors and `Prefix` now reject null dependencies immediately. |
| Athlete invariants | `Person` rejects null tag elements as well as a null set. Regression tests cover rejection and defensive copying. |
| Comments and setup | Corrected remaining “behaviour” in Java comments. Added IntelliJ assertions and required Git conventions to `docs/SettingUp.md`. |
| Automated behavior checks | Clean Gradle run: 567 tests, zero failures/errors/skips. Includes parser boundaries, duplicate identity, ordering/filtering, real JSON reload, failed-save isolation, startup recovery and window-bound calculations. |
| Packaging | `shadowJar` builds `build/libs/TrackFlow.jar`, approximately 12 MiB. Gradle and `MainApp.VERSION` identify v1.5. |
| Documentation | MarkBind builds all ten pages; generated HTML has no missing local links, assets or anchors. DG has class/sequence PlantUML sources, requirements, acknowledgements and manual tests. |
| Local launch | The packaged JAR reached UI startup and stayed alive for eight seconds on Apple Silicon with Zulu 25.0.4.1 JDK FX; the isolated test process was then stopped. No interactive GUI assertions were made. |
| Repository checks | `./.github/run-checks.sh` passes. |

The [error-handling chapter](https://nus-cs2103-ay2627-s1.github.io/website/se-book-adapted/chapters/errorHandling.html)
supports validating compulsory references and protecting internal state. Existing code already uses exceptions,
assertions and logging; it stages roster changes before saving, preserves unreadable roster files, validates
deserialized data, copies search collections and exposes unmodifiable roster/tag views. The added guards close
specific gaps; they do not establish that every possible internal misuse has been excluded.

Javadoc generation succeeds but reports missing-tag/comment warnings, including accessors and constants for
which the course does not require headers. Those tool warnings are separate from the zero-violation Checkstyle
result. Constructor tests and helper tests do not exercise the complete JavaFX GUI.

## Week 11 status

Public repository: [AY2627S1-CS2103T-T17-2/tp](https://github.com/AY2627S1-CS2103T-T17-2/tp).
The following GitHub state was read during this audit and can change.

| Requirement | Status |
| --- | --- |
| Alpha-test reporting | All five members have five issues labeled `alpha-bug`: Austin-Chia, antonydchakola-hub, chetangent, ken2k7 and qiyuannn. This verifies the reporting threshold, not resolution or test quality. |
| Fix bugs and improve quality | Repository contains alpha-fix commits and regression tests; the local suite passes. Remaining issues still need release triage. |
| Update UG, DG and landing page | Present and locally buildable. Confirm the published pages and actual screenshots match the final JAR. |
| Settle authorship | Author markers exist. Each member must inspect their attributed lines on the course RepoSense dashboard; markers alone cannot certify ownership. |
| Release v1.5 | Still open: the releases API lists v1.4 and v1.3, each with `TrackFlow.jar`, but no v1.5. Publish the tested JAR and website by October 29, 23:59 Singapore time. Detailed v1.5 release notes are optional. |
| Close milestone | v1.5 is open with three open and 38 closed items. Triage/reschedule remaining items, then close it after release. |
| Team beta test | Recommended October 30, 16:00–18:00 Singapore time, using the released JAR and guides. |

No issues, labels, milestones, releases or authorship declarations were changed by this audit.
The three open milestone items are [documentation/Javadoc formatting #187](https://github.com/AY2627S1-CS2103T-T17-2/tp/issues/187),
[clear safeguards #148](https://github.com/AY2627S1-CS2103T-T17-2/tp/issues/148), and
[actual UI screenshots #145](https://github.com/AY2627S1-CS2103T-T17-2/tp/issues/145).
This checkout already has an actual app screenshot with event labels and a permanent-clear/backup warning in the
UG. The confirmation step in #148 is a proposed enhancement, not a mandatory course feature. Recheck deployment
and acceptance of those fixes before resolving the reports.

## Week 10 prerequisites

The [Week 10 page](https://nus-cs2103-ay2627-s1.github.io/website/schedule/week10/project.html) also covers
planning, a postmortem, an alpha release, defensive code, parallel PRs and UML updates.

The v1.4 release was published October 22 at 20:43 Singapore time with a JAR, before that day's 23:59 deadline;
its milestone is closed. Release-note completeness and historical feature scope were not established here.
Recent public PR lifetimes provide overlapping PR examples for every member:

| Member | Overlapping PRs |
| --- | --- |
| Austin-Chia | [127](https://github.com/AY2627S1-CS2103T-T17-2/tp/pull/127), [122](https://github.com/AY2627S1-CS2103T-T17-2/tp/pull/122) |
| antonydchakola-hub | [106](https://github.com/AY2627S1-CS2103T-T17-2/tp/pull/106), [105](https://github.com/AY2627S1-CS2103T-T17-2/tp/pull/105) |
| chetangent | [110](https://github.com/AY2627S1-CS2103T-T17-2/tp/pull/110), [108](https://github.com/AY2627S1-CS2103T-T17-2/tp/pull/108) |
| ken2k7 | [177](https://github.com/AY2627S1-CS2103T-T17-2/tp/pull/177), [174](https://github.com/AY2627S1-CS2103T-T17-2/tp/pull/174) |
| qiyuannn | [131](https://github.com/AY2627S1-CS2103T-T17-2/tp/pull/131), [121](https://github.com/AY2627S1-CS2103T-T17-2/tp/pull/121) |

Shared postmortem notes, historical task assignments and each member's timely UML contribution still require
team/course records. Existing DG diagrams do not by themselves prove individual participation or deadlines.

## Constraints and remaining release checks

| Area | Local evidence or remaining action |
| --- | --- |
| Brownfield, OOP and typing | AB3-derived layered Java application; CLI commands and autocomplete; README identifies typing-oriented coaches. Historical delivery and individual effort require separate assessment. |
| Single user and local text data | JSON roster/preferences; no DBMS or required remote service. Application-home paths, save/reload and recovery have automated tests. |
| Java 25, portability and one package | Build targets Java 25 and produces one JAR below 100 MB. Smoke-test that same JAR on Windows, Linux, Intel macOS and Apple Silicon. |
| JavaFX compatibility | The build bundles JavaFX 17.0.7 for Windows/Linux/Intel macOS, without a separate Apple Silicon dependency. Current setup prescribes Zulu JDK FX on macOS. Plain-JDK and architecture compatibility remain release risks until tested; documentation alone cannot establish them. |
| GUI and website usability | Manually test 1920×1080 at 100%/125% and usability at 1280×720/150%, long rosters, help and shutdown. Check published UG/DG in current Chrome/Firefox across the supported OSes. |
| External software | JavaFX/Jackson/JUnit and the font are acknowledged. Confirm course approval for any added libraries/services and completeness of reuse attribution with the team. |
| Final portfolios and demo | Only the unpublished PPP template exists in `docs/team`. Track the individual PPP and product demo against their later deliverable deadlines; they are not completed by this audit. |

Before release, run the manual appendix in `docs/DeveloperGuide.md` on the actual packaged JAR and record the
runtime, platform and results. Confirm green Java CI and successful website deployment for the release commit.
Changes on `fix-docs` do not update the team release or published website until merged and deployed.

The Week 10 feature-freeze rule starts after the v1.5 deadline plus its ten-hour buffer: October 30 at 10:00
Singapore time. It limits changes per member to functional source under `src/main`, including comments and blank
lines; settle authorship and source cleanup before the deadline. This audit does not rewrite historical commits
or manufacture team participation evidence.
