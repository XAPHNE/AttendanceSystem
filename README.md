# Attendance System
Attendance System is a JavaFX application that allows users to manage and track the attendance of students or employees. It uses an embedded H2 database (no database server to install) and Maven as the build tool. It is developed using IntelliJ IDEA as the IDE.

## Features
- **Login page:** Users can log in to the application using their username and password. The login information is verified with the database
- **Dashboard:** To view graphs and charts of absent/present
- **Add Student:** To add student to the system and perform CRUD operation
- **Available course:** To add courses in the system
- **Attendance:** To Mark Entry and Exit Time for attendance


## Software Requirements
### Before you dive in, make sure to download and install the following:

1. [Git](https://git-scm.com/)
2. [SceneBuilder](https://gluonhq.com/products/scene-builder/)
3. [IntelliJ IDEA IDE](https://www.jetbrains.com/idea/)
4. [JDK 25](https://adoptium.net/) or newer

## Installation
### Configuring SceneBuilder
#### Before opening the `view.fxml` files in SceneBuilder, you need to add essential dependencies.

1. Add the following dependencies to SceneBuilder:
   1. `io.github.palexdev:materialfx:11.17.0`
   2. `org.kordamp.ikonli:ikonli-fontawesome-pack:12.4.0`
   3. `org.kordamp.ikonli:ikonli-fontawesome5-pack:12.4.0`
   4. `org.kordamp.ikonli:ikonli-javafx:12.4.0`

2. To add these dependencies, follow these steps within SceneBuilder:
   1. Select the **Empty** desktop application option.
   2. On the left panel, click the gear icon next to "Library" and the search bar.
   3. Choose **JAR/FXML Manager**.
   4. Click **Search repositories**.
   5. Search and add the dependencies one by one:
      1. `io.github.palexdev:materialfx`
      2. `org.kordamp.ikonli:ikonli-fontawesome-pack`
      3. `org.kordamp.ikonli:ikonli-fontawesome5-pack`
      4. `org.kordamp.ikonli:ikonli-javafx`
   6. Select each item from the search results and click **Add Jar**.
   7. Choose **Import Component**.
   8. Repeat these steps until all dependencies are added.

### Setting Up IntelliJ IDEA IDE
1. Open IntelliJ IDEA and select **Get from VCS**.
2. In the "GitHub" tab, log in to your GitHub account.
3. Click the "Repository URL" tab and paste the project URL: `https://github.com/XAPHNE/AttendanceSystem`. Then click **Clone**.

### Running the Project
Run `./mvnw javafx:run` (Windows: `mvnw.cmd javafx:run`) from the project folder, or press **`Shift + F10`** in IntelliJ on the `src/main/java/com/bsmi/attendancesystem/AttendanceApplication.java` file.

The build stops with a clear message if your JDK is older than 25.

On the first start the app creates its database and fills it with demo data (fictional students, courses and a few days of attendance). Log in with **admin / admin123** (Admin) or **employee / employee123** (Employee).

### Where the data is stored
The database lives in a per-user folder, so the app works from any install location:

| OS      | Folder                                                          |
|---------|-----------------------------------------------------------------|
| Windows | `%APPDATA%\AttendanceSystem`                                    |
| macOS   | `~/Library/Application Support/AttendanceSystem`                |
| Linux   | `$XDG_DATA_HOME/AttendanceSystem` (default `~/.local/share/AttendanceSystem`) |

Delete that folder to start over with fresh demo data. To use another folder, start the app with `-Dattendance.home=<folder>`. To use another database, set `ATTENDANCE_DB_URL`, `ATTENDANCE_DB_USER` and `ATTENDANCE_DB_PASSWORD`.

The schema is created and upgraded by [Flyway](https://documentation.red-gate.com/flyway) from the SQL files in `src/main/resources/db/migration`. To change the schema, add a new `V<next number>__<description>.sql` file there; never edit a file that has already been released.

Earlier versions kept the database in the project's `data/` folder. That folder is no longer used.

### Building a version that runs without Java
Run `./mvnw -Pdist clean package` (Windows: `mvnw.cmd -Pdist clean package`). `target/dist/AttendanceSystem` is then a folder with the app and its own small Java runtime. Copy it to any computer with the same operating system and start `bin/AttendanceSystem` (Windows: `AttendanceSystem.exe`, macOS: `AttendanceSystem.app`). That computer doesn't need Java installed.

To build an installer instead, add `-Ddist.type=` followed by the type:

| OS      | Type  | Note                                                                                              |
|---------|-------|---------------------------------------------------------------------------------------------------|
| Linux   | `DEB` | Needs `fakeroot`. Add `-Ddist.shortcuts=true -Ddist.linux-deps=libgtk-3-0` for a menu entry and the GTK dependency |
| Windows | `MSI` | Needs the [WiX Toolset](https://wixtoolset.org/)                                                  |
| macOS   | `DMG` |                                                                                                   |

How it works: jlink builds a Java runtime that holds only the JDK modules the app needs. jpackage puts that runtime, the app and its libraries in one folder or installer. H2, Flyway and jbcrypt aren't Java modules, and jlink can't put those into a runtime, so they sit next to it in `lib/app/mods`.

### Running the tests
Run `./mvnw verify` (Windows: `mvnw.cmd verify`).

- The UI tests open the real app and click through it, so they need a display. On a Linux machine without one (a server or CI runner) they are skipped; run `xvfb-run ./mvnw verify` to include them. `./mvnw verify -DexcludedGroups=ui` skips them anywhere.
- The build fails if the tests cover less than 90% of the lines in the `dao` and `service` packages. The coverage report is at `target/site/jacoco/index.html`.
- GitHub Actions runs the same build with the UI tests on Linux, Windows and macOS for every push and pull request (`.github/workflows/ci.yml`). A separate job tries the newest Java (27) as an early warning; it is allowed to fail.

### Checking for dependency updates
Dependabot (`.github/dependabot.yml`) checks every week for newer libraries, build plugins, the Maven version and GitHub Actions. It opens a pull request for them, and CI tests the update before it is merged.

To check by hand, run `./mvnw versions:display-dependency-updates versions:display-plugin-updates`. It lists newer stable releases of the libraries and build plugins.

Now, enjoy the benefits of our Attendance System!

## Screenshots

![Attendance System Screenshot](https://user-images.githubusercontent.com/127822494/257903527-0e72b5ec-21c2-4c30-87fc-3f47bed03d33.png "Login Screen")

## License
This project is licensed under the GNU General Public License v3.0 - see the [LICENSE](/LICENSE) file for details.

For more information about the GNU GPL v3.0 license, please visit the [GNU website](https://www.gnu.org/licenses/gpl-3.0.en.html).

## Author
[Subhankar Sarkar](https://xaphne.github.io/about)

## Contributor
[Prithiraj Rabha](https://github.com/prithirabha)
