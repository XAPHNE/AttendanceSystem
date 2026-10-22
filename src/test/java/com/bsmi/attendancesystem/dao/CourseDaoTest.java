package com.bsmi.attendancesystem.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bsmi.attendancesystem.CourseData;
import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class CourseDaoTest {
    private static final String INJECTION = "x' OR '1'='1";

    private Database database;
    private CourseDao courses;

    @BeforeEach
    void useFreshDatabase() {
        database = TestDatabases.migratedInMemory();
        courses = new CourseDao(database);
    }

    @Test
    void findAllReturnsDemoCoursesInInsertOrder() {
        assertThat(courses.findAll())
                .extracting(CourseData::getCourse)
                .containsExactly("BCA", "BSc", "MCA");
    }

    @Test
    void findAllReadsEveryColumn() {
        CourseData bca = courses.findAll().getFirst();

        assertThat(bca.getDescription()).isEqualTo("Bachelor of Computer Applications");
        assertThat(bca.getDegree()).isEqualTo("Bachelor");
    }

    @Test
    void findAllOnEmptyTableReturnsEmptyList() throws SQLException {
        TestDatabases.execute(database, "DELETE FROM student_attendance");
        TestDatabases.execute(database, "DELETE FROM course");

        assertThat(courses.findAll()).isEmpty();
    }

    @Test
    void addedCourseIsFoundAndExists() {
        courses.add(new CourseData("BBA", "Bachelor of Business Administration", "Bachelor"));

        assertThat(courses.exists("BBA")).isTrue();
        assertThat(courses.findAll()).extracting(CourseData::getCourse).endsWith("BBA");
    }

    @Test
    void existsIsFalseForUnknownCourse() {
        assertThat(courses.exists("PhD")).isFalse();
    }

    @Test
    void existsIsCaseSensitive() {
        assertThat(courses.exists("bca")).isFalse();
    }

    @Test
    void addingDuplicateNameFailsWithDataAccessException() {
        assertThatThrownBy(() -> courses.add(new CourseData("BCA", "Again", "Bachelor")))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("add course BCA")
                .hasCauseInstanceOf(SQLException.class);
    }

    @Test
    void addingNullFieldFailsWithDataAccessException() {
        assertThatThrownBy(() -> courses.add(new CourseData("BBA", null, "Bachelor")))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    void injectionTextIsStoredLiterallyAndMatchesNothingElse() {
        courses.add(new CourseData(INJECTION, INJECTION, INJECTION));

        assertThat(courses.exists(INJECTION)).isTrue();
        assertThat(courses.findAll()).hasSize(4);
        assertThat(courses.delete(INJECTION)).isTrue();
        assertThat(courses.findAll()).hasSize(3);
    }

    @Test
    void updateChangesDescriptionAndDegree() {
        boolean updated = courses.update(new CourseData("MCA", "Master of Apps", "PG"));

        assertThat(updated).isTrue();
        CourseData mca = courses.findAll().getLast();
        assertThat(mca.getDescription()).isEqualTo("Master of Apps");
        assertThat(mca.getDegree()).isEqualTo("PG");
    }

    @Test
    void updateOfUnknownCourseReturnsFalseAndChangesNothing() {
        List<CourseData> before = courses.findAll();

        assertThat(courses.update(new CourseData("PhD", "x", "y"))).isFalse();
        assertThat(courses.findAll())
                .extracting(CourseData::getDescription)
                .containsExactlyElementsOf(before.stream().map(CourseData::getDescription).toList());
    }

    @Test
    void updateWithInjectionNameChangesNothing() {
        assertThat(courses.update(new CourseData(INJECTION, "hacked", "hacked"))).isFalse();
        assertThat(courses.findAll()).extracting(CourseData::getDescription).doesNotContain("hacked");
    }

    @Test
    void deleteRemovesUnusedCourse() {
        courses.add(new CourseData("BBA", "Business", "Bachelor"));

        assertThat(courses.delete("BBA")).isTrue();
        assertThat(courses.exists("BBA")).isFalse();
    }

    @Test
    void deleteOfUnknownCourseReturnsFalse() {
        assertThat(courses.delete("PhD")).isFalse();
    }

    @Test
    void deletingCourseWithAttendanceFailsWithDataAccessException() {
        assertThatThrownBy(() -> courses.delete("BCA"))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("delete course BCA");
        assertThat(courses.exists("BCA")).isTrue();
    }

    @Test
    void courseWithAttendanceIsInUse() {
        assertThat(courses.isInUse("BCA")).isTrue();
    }

    @Test
    void courseWithOnlyStudentsIsInUse() throws SQLException {
        TestDatabases.execute(database, "DELETE FROM student_attendance");

        assertThat(courses.isInUse("BCA")).isTrue();
    }

    @Test
    void newCourseIsNotInUse() {
        courses.add(new CourseData("BBA", "Business", "Bachelor"));

        assertThat(courses.isInUse("BBA")).isFalse();
    }

    @Test
    void unknownCourseIsNotInUse() {
        assertThat(courses.isInUse("PhD")).isFalse();
    }

    @ParameterizedTest
    @MethodSource("com.bsmi.attendancesystem.dao.TestDatabases#broken")
    void everyOperationOnBrokenDatabaseFailsWithDataAccessException(Database database) {
        CourseDao offline = new CourseDao(database);
        CourseData course = new CourseData("BCA", "x", "y");

        assertThatThrownBy(offline::findAll).isInstanceOf(DataAccessException.class);
        assertThatThrownBy(() -> offline.exists("BCA")).isInstanceOf(DataAccessException.class);
        assertThatThrownBy(() -> offline.isInUse("BCA")).isInstanceOf(DataAccessException.class);
        assertThatThrownBy(() -> offline.add(course)).isInstanceOf(DataAccessException.class);
        assertThatThrownBy(() -> offline.update(course)).isInstanceOf(DataAccessException.class);
        assertThatThrownBy(() -> offline.delete("BCA")).isInstanceOf(DataAccessException.class);
    }
}
