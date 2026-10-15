[![Java CI](https://github.com/AY2627S1-CS2103T-T17-2/tp/actions/workflows/gradle.yml/badge.svg)](https://github.com/AY2627S1-CS2103T-T17-2/tp/actions/workflows/gradle.yml)

![Ui](docs/images/Ui.png)

# TrackFlow

**TrackFlow** is a keyboard-driven roster and contact manager for **track and field head and assistant coaches**. TrackFlow v1.3 stores athlete names, age categories, contact details, addresses, and remarks locally. Coaches can add, edit, search, filter, sort, and delete athletes using short text commands.

While it has a Graphical User Interface (GUI), most user interactions occur using a Command Line Interface (CLI). It is strictly optimized for touch-typist coaches working at a desk between practices and meets who strongly prefer rapid, keyboard-driven text entry over navigating nested GUI drop-downs and bloated sports-management web platforms.

### Key Features

* **Command autocomplete**: Real-time dropdown suggestions for commands and incremental prefix completion using `Tab` to guide rapid, error-free command entry.
* **Age-category filtering**: Use `filter a/AGE_CATEGORY` (e.g., `filter a/Under 14`) to filter athletes by competition category; `list` restores the full roster.
* **Flexible athlete search**: Search partial keywords across supported fields or scope searches by specific prefixes (e.g., `find ave`, `find n/avery`, or `find r/injured`).
* **Multi-field sorting**: Sort the displayed roster by name, age category, phone, email, or address in ascending or descending order (e.g., `sort age desc`).
* **Athlete remarks**: Attach coach notes, status updates, or medical flags directly to athlete profiles with `remark INDEX r/REMARK`.

### Upcoming Features `[coming in v2.0]`

* **Guardian & emergency contact linking**: Map student-athlete cards directly to parent/guardian profiles, enabling single-command lookups of emergency numbers during road meets.
* **Relay squads & event grouping**: Organize athletes into designated relay squads (e.g., `group g/4x100mA c/1,4,7,12`), track specific relay legs, and log alternate runners.
* **Personal Best (PB) & performance logs**: Track appendable performance milestones and competition results directly on contact profiles.

### Documentation
For detailed instructions on how to use and develop this application, refer to our project website:
* **[User Guide](https://ay2627s1-cs2103t-t17-2.github.io/tp/UserGuide.html)**
* **[Developer Guide](https://ay2627s1-cs2103t-t17-2.github.io/tp/DeveloperGuide.html)**

### Acknowledgments
* This project is based on the AddressBook-Level3 project created by the [SE-EDU initiative](https://se-education.org).
* The UI uses the [Plus Jakarta Sans](https://github.com/tokotype/PlusJakartaSans) font by Tokotype, licensed under the [SIL Open Font License 1.1](src/main/resources/fonts/OFL.txt).
