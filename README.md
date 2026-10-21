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
