package com.bsmi.attendancesystem.ui;

import static org.assertj.core.api.Assertions.assertThat;

import com.bsmi.attendancesystem.CourseData;
import java.util.List;
import javafx.scene.control.TableView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DashboardTest extends AppScreenTest {
    @BeforeEach
    void logInAsAdmin() {
        logInAndDismissWelcome("admin", "admin123");
    }

    @Test
    void emptyStudentFormAsksForTheStudentNumber() {
        clickOn("#addStudent_btn");
        clickOn("#addStudent_addBtn");

        assertThat(alertText()).isEqualTo("Please fill in the Student # field");
    }

    @Test
    void addedCourseAppearsInTheTable() {
        addCourse("PhD");

        assertThat(alertText()).isEqualTo("Successfully Added!");
        pressAlertButton("OK");
        assertThat(courseNames()).contains("PhD");
    }

    @Test
    void cancellingADeleteKeepsTheCourse() {
        addCourse("PhD");
        pressAlertButton("OK");

        deleteCourse("PhD");
        assertThat(alertText()).isEqualTo("Are you sure you want to delete Course: PhD?");
        pressAlertButton("Cancel");

        assertThat(courseNames()).contains("PhD");
    }

    @Test
    void confirmingADeleteRemovesTheCourse() {
        addCourse("PhD");
        pressAlertButton("OK");

        deleteCourse("PhD");
        pressAlertButton("OK");

        assertThat(alertText()).isEqualTo("Successfully Deleted!");
        pressAlertButton("OK");
        assertThat(courseNames()).doesNotContain("PhD");
    }

    @Test
    void courseUsedByStudentsCannotBeDeleted() {
        deleteCourse("BCA");
        pressAlertButton("OK");

        assertThat(alertText())
                .isEqualTo("Course BCA can't be deleted while students or attendance records use it");
        pressAlertButton("OK");
        assertThat(courseNames()).contains("BCA");
    }

    @Test
    void confirmedLogoutReturnsToTheLoginScreen() {
        clickOn("#logout_btn");
        assertThat(alertText()).isEqualTo("Are you sure you want to logout?");
        pressAlertButton("OK");

        assertThat(lookup("#loginButton").tryQuery()).isPresent();
        assertThat(lookup("#logout_btn").tryQuery()).isEmpty();
    }

    @Test
    void cancelledLogoutStaysOnTheDashboard() {
        clickOn("#logout_btn");
        pressAlertButton("Cancel");

        assertThat(lookup("#logout_btn").tryQuery()).isPresent();
        assertThat(lookup("#loginButton").tryQuery()).isEmpty();
    }

    private void addCourse(String name) {
        clickOn("#availableCourses_btn");
        clickOn("#availableCourse_course").write(name);
        clickOn("#availableCourse_description").write("Doctor of Philosophy");
        clickOn("#availableCourse_degree").write("Doctorate");
        clickOn("#availableCourse_addBtn");
    }

    private void deleteCourse(String name) {
        clickOn("#availableCourses_btn");
        clickOn("#availableCourse_clearBtn");
        clickOn("#availableCourse_course").write(name);
        clickOn("#availableCourse_deleteBtn");
    }

    private List<String> courseNames() {
        TableView<CourseData> table = lookup("#availableCourse_tableView").queryTableView();
        return table.getItems().stream().map(CourseData::getCourse).toList();
    }
}
