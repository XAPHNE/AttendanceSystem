package com.bsmi.attendancesystem.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;

import com.bsmi.attendancesystem.StudentData;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class StudentDaoTest {
    private static final String INJECTION = "x' OR '1'='1";

    private Database database;
    private StudentDao students;

    @BeforeEach
    void useFreshDatabase() {
        database = TestDatabases.migratedInMemory();
        students = new StudentDao(database);
    }

    @Test
    void findAllReturnsDemoStudentsInInsertOrder() {
        assertThat(students.findAll())
                .extracting(StudentData::getStudentNum)
                .containsExactly(1001, 1002, 1003, 1004, 1005, 1006);
    }

    @Test
    void findAllReadsEveryColumn() {
        StudentData asha = students.findAll().getFirst();

        assertThat(asha.getSemester()).isEqualTo("3rd Sem");
        assertThat(asha.getCourse()).isEqualTo("BCA");
        assertThat(asha.getFirstName()).isEqualTo("Asha");
        assertThat(asha.getLastName()).isEqualTo("Verma");
        assertThat(asha.getGender()).isEqualTo("Female");
        assertThat(asha.getBirthDate()).isEqualTo(Date.valueOf("2005-04-12"));
        assertThat(asha.getStatus()).isEqualTo("Enrolled");
        assertThat(asha.getImage()).isEmpty();
    }

    @Test
    void findAllOnEmptyTableReturnsEmptyList() throws SQLException {
        TestDatabases.execute(database, "DELETE FROM student");

        assertThat(students.findAll()).isEmpty();
    }

    @Test
    void addedStudentIsFoundWithEveryValue() {
        StudentData zoya = student(2001, "Zoya");

        students.add(zoya);

        StudentData stored = students.findAll().getLast();
        assertThat(stored).usingRecursiveComparison().isEqualTo(zoya);
        assertThat(students.exists(2001)).isTrue();
    }

    @Test
    void addedStudentIsDatedToday() throws SQLException {
        students.add(student(2001, "Zoya"));

        assertThat(addedOn(2001)).isEqualTo(LocalDate.now());
    }

    @Test
    void existsIsFalseForUnknownNumber() {
        assertThat(students.exists(9999)).isFalse();
    }

    @Test
    void addingTakenNumberFailsWithDataAccessException() {
        assertThatThrownBy(() -> students.add(student(1001, "Again")))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("add student #1001")
                .hasCauseInstanceOf(SQLException.class);
    }

    @Test
    void addingUnknownGenderFailsWithDataAccessException() {
        StudentData robot =
                new StudentData(
                        2001, "1st Sem", "BCA", "R", "B", "Robot", birthDate(), "Enrolled", "");

        assertThatThrownBy(() -> students.add(robot)).isInstanceOf(DataAccessException.class);
        assertThat(students.exists(2001)).isFalse();
    }

    @Test
    void addingNullFieldFailsWithDataAccessException() {
        StudentData noName =
                new StudentData(
                        2001, "1st Sem", "BCA", null, "B", "Male", birthDate(), "Enrolled", "");

        assertThatThrownBy(() -> students.add(noName)).isInstanceOf(DataAccessException.class);
    }

    @Test
    void injectionTextIsStoredLiterally() {
        students.add(student(2001, INJECTION));

        assertThat(students.findAll().getLast().getFirstName()).isEqualTo(INJECTION);
        assertThat(students.findAll()).hasSize(7);
    }

    @Test
    void windowsImagePathKeepsSingleBackslashes() {
        String path = "C:\\Users\\me\\photo.png";
        StudentData withPhoto =
                new StudentData(
                        2001, "1st Sem", "BCA", "A", "B", "Male", birthDate(), "Enrolled", path);

        students.add(withPhoto);

        assertThat(students.findAll().getLast().getImage()).isEqualTo(path);
    }

    @Test
    void updateChangesEveryFieldButTheNumber() {
        StudentData changed =
                new StudentData(
                        1001, "4th Sem", "MCA", "Asha", "Rao", "Female", birthDate(), "Dropped Out",
                        "a.png");

        assertThat(students.update(changed)).isTrue();

        assertThat(students.findAll().getFirst()).usingRecursiveComparison().isEqualTo(changed);
    }

    @Test
    void updateOfUnknownNumberReturnsFalseAndAddsNothing() {
        assertThat(students.update(student(9999, "Ghost"))).isFalse();
        assertThat(students.exists(9999)).isFalse();
    }

    @Test
    void updateWithInjectionTextChangesOnlyThatStudent() {
        students.update(student(1001, INJECTION));

        assertThat(students.findAll())
                .extracting(StudentData::getFirstName)
                .containsOnlyOnce(INJECTION)
                .contains("Rohan", "Meera");
    }

    @Test
    void deleteRemovesTheStudentAndTheirAttendance() throws SQLException {
        long ashaId = idOf(1001);
        assertThat(attendanceRowsOfId(ashaId)).isPositive();

        assertThat(students.delete(1001)).isTrue();

        assertThat(students.exists(1001)).isFalse();
        assertThat(attendanceRowsOfId(ashaId)).isZero();
    }

    @Test
    void deleteOfUnknownNumberReturnsFalse() {
        assertThat(students.delete(9999)).isFalse();
        assertThat(students.findAll()).hasSize(6);
    }

    @Test
    void countEnrolledLeavesOutOtherStatuses() {
        assertThat(students.countEnrolled()).isEqualTo(5);
    }

    @Test
    void countEnrolledOfEmptyTableIsZero() throws SQLException {
        TestDatabases.execute(database, "DELETE FROM student");

        assertThat(students.countEnrolled()).isZero();
    }

    @Test
    void countEnrolledAddedPerDayGivesTheLatestDaysOldestFirst() {
        LocalDate today = LocalDate.now();

        assertThat(students.countEnrolledAddedPerDay(5))
                .containsExactly(
                        entry(today.minusDays(60), 1),
                        entry(today.minusDays(30), 2),
                        entry(today.minusDays(10), 1),
                        entry(today.minusDays(5), 1));
        assertThat(students.countEnrolledAddedPerDay(2))
                .containsExactly(entry(today.minusDays(10), 1), entry(today.minusDays(5), 1));
    }

    @Test
    void findCoursesInSemesterListsEachCourseOnceAlphabetically() {
        students.add(new StudentData(2001, "3rd Sem", "BCA", "Zoya", "Ali", "Female",
                birthDate(), "Enrolled", ""));
        students.add(new StudentData(2002, "3rd Sem", "BBA", "Ravi", "Ali", "Male",
                birthDate(), "Enrolled", ""));

        assertThat(students.findCoursesInSemester("3rd Sem")).containsExactly("BBA", "BCA");
    }

    @Test
    void findCoursesInUnknownOrInjectedSemesterIsEmpty() {
        assertThat(students.findCoursesInSemester("9th Sem")).isEmpty();
        assertThat(students.findCoursesInSemester(INJECTION)).isEmpty();
    }

    @Test
    void findNumbersInListsThatSemesterAndCourseLowestFirst() {
        students.add(new StudentData(999, "3rd Sem", "BCA", "Zoya", "Ali", "Female",
                birthDate(), "Enrolled", ""));

        assertThat(students.findNumbersIn("3rd Sem", "BCA")).containsExactly(999, 1001, 1002);
        assertThat(students.findNumbersIn("3rd Sem", "BSc")).isEmpty();
        assertThat(students.findNumbersIn(INJECTION, INJECTION)).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("com.bsmi.attendancesystem.dao.TestDatabases#broken")
    void everyOperationOnBrokenDatabaseFailsWithDataAccessException(Database database) {
        StudentDao offline = new StudentDao(database);
        StudentData student = student(2001, "Zoya");

        assertThatThrownBy(offline::findAll).isInstanceOf(DataAccessException.class);
        assertThatThrownBy(() -> offline.exists(2001)).isInstanceOf(DataAccessException.class);
        assertThatThrownBy(() -> offline.add(student)).isInstanceOf(DataAccessException.class);
        assertThatThrownBy(() -> offline.update(student)).isInstanceOf(DataAccessException.class);
        assertThatThrownBy(() -> offline.delete(2001)).isInstanceOf(DataAccessException.class);
        assertThatThrownBy(offline::countEnrolled).isInstanceOf(DataAccessException.class);
        assertThatThrownBy(() -> offline.countEnrolledAddedPerDay(5))
                .isInstanceOf(DataAccessException.class);
        assertThatThrownBy(() -> offline.findCoursesInSemester("3rd Sem"))
                .isInstanceOf(DataAccessException.class);
        assertThatThrownBy(() -> offline.findNumbersIn("3rd Sem", "BCA"))
                .isInstanceOf(DataAccessException.class);
    }

    private static StudentData student(int studentNum, String firstName) {
        return new StudentData(
                studentNum, "1st Sem", "BSc", firstName, "Ali", "Female", birthDate(), "Enrolled",
                "");
    }

    private static Date birthDate() {
        return Date.valueOf("2006-05-01");
    }

    private LocalDate addedOn(int studentNum) throws SQLException {
        String sql = "SELECT added_on FROM student WHERE studentNum = ?";
        return LocalDate.parse(firstValue(sql, studentNum));
    }

    private long idOf(int studentNum) throws SQLException {
        return Long.parseLong(firstValue("SELECT id FROM student WHERE studentNum = ?", studentNum));
    }

    private long attendanceRowsOfId(long studentId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM student_attendance WHERE student_id = ?";
        return Long.parseLong(firstValue(sql, studentId));
    }

    private String firstValue(String sql, long parameter) throws SQLException {
        try (Connection connection = database.open();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, parameter);
            try (ResultSet rows = statement.executeQuery()) {
                rows.next();
                return rows.getString(1);
            }
        }
    }
}
