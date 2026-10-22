package com.bsmi.attendancesystem.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;

import com.bsmi.attendancesystem.StudentAttendanceData;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class AttendanceDaoTest {
    // The demo data is dated relative to the database's CURRENT_DATE, i.e. the real today
    private static final LocalDate TODAY = LocalDate.now();

    private Database database;
    private AttendanceDao attendance;

    @BeforeEach
    void useFreshDatabase() {
        database = TestDatabases.migratedInMemory();
        attendance = new AttendanceDao(database);
    }

    @Test
    void findOnReturnsThatDaysRecordsInTheOrderTheyWereMade() {
        assertThat(attendance.findOn(TODAY))
                .extracting(StudentAttendanceData::getStudentNum)
                .containsExactly("1001", "1002", "1003", "1004", "1005");
    }

    @Test
    void findOnReadsEveryColumn() {
        StudentAttendanceData kabir = attendance.findOn(TODAY).get(3);

        assertThat(kabir.getSemester()).isEqualTo("5th Sem");
        assertThat(kabir.getCourse()).isEqualTo("BSc");
        assertThat(kabir.getEntryTime()).startsWith(TODAY + " 09:00");
        assertThat(kabir.getExitTime()).startsWith(TODAY + " 15:00");
        assertThat(kabir.getStatus()).isEqualTo("Absent");
    }

    @Test
    void findOnDayWithoutRecordsReturnsEmptyList() {
        assertThat(attendance.findOn(TODAY.plusDays(1))).isEmpty();
        assertThat(attendance.findOn(TODAY.minusDays(5))).isEmpty();
    }

    @Test
    void markEntryAddsAnOpenPresentRecordForTheStudentsCourse() {
        LocalDateTime entry = TODAY.atTime(10, 15, 30);

        assertThat(attendance.markEntry(1001, entry)).isTrue();

        StudentAttendanceData added = attendance.findOn(TODAY).getLast();
        assertThat(added.getStudentNum()).isEqualTo("1001");
        assertThat(added.getCourse()).isEqualTo("BCA");
        assertThat(added.getEntryTime()).startsWith(TODAY + " 10:15:30");
        assertThat(added.getExitTime()).isNull();
        assertThat(added.getStatus()).isEqualTo("Present");
        assertThat(attendance.hasOpenEntry(1001, TODAY)).isTrue();
    }

    @Test
    void markEntryOfUnknownStudentReturnsFalseAndAddsNothing() {
        assertThat(attendance.markEntry(9999, TODAY.atTime(10, 0))).isFalse();
        assertThat(attendance.findOn(TODAY)).hasSize(5);
    }

    @Test
    void markEntryOfStudentWithoutAKnownCourseReturnsFalse() throws SQLException {
        TestDatabases.execute(database, "UPDATE student SET course = 'Gone' WHERE studentNum = 1001");

        assertThat(attendance.markEntry(1001, TODAY.atTime(10, 0))).isFalse();
    }

    @Test
    void hasOpenEntryIsFalseOnceEveryRecordHasAnExit() {
        assertThat(attendance.hasOpenEntry(1001, TODAY)).isFalse();
    }

    @Test
    void hasOpenEntryOnlyLooksAtThatDay() {
        attendance.markEntry(1001, TODAY.minusDays(1).atTime(10, 0));

        assertThat(attendance.hasOpenEntry(1001, TODAY)).isFalse();
        assertThat(attendance.hasOpenEntry(1001, TODAY.minusDays(1))).isTrue();
    }

    @Test
    void markExitClosesTheOpenRecordOfThatDay() {
        attendance.markEntry(1001, TODAY.atTime(10, 0));

        assertThat(attendance.markExit(1001, TODAY.atTime(16, 45, 5))).isEqualTo(1);

        assertThat(attendance.findOn(TODAY).getLast().getExitTime())
                .startsWith(TODAY + " 16:45:05");
        assertThat(attendance.hasOpenEntry(1001, TODAY)).isFalse();
    }

    @Test
    void markExitLeavesOtherStudentsAndOtherDaysOpen() {
        attendance.markEntry(1001, TODAY.atTime(10, 0));
        attendance.markEntry(1002, TODAY.atTime(10, 0));
        attendance.markEntry(1001, TODAY.minusDays(1).atTime(10, 0));

        attendance.markExit(1001, TODAY.atTime(16, 0));

        assertThat(attendance.hasOpenEntry(1002, TODAY)).isTrue();
        assertThat(attendance.hasOpenEntry(1001, TODAY.minusDays(1))).isTrue();
    }

    @Test
    void markExitWithoutOpenRecordReturnsZero() {
        assertThat(attendance.markExit(1001, TODAY.atTime(16, 0))).isZero();
        assertThat(attendance.markExit(9999, TODAY.atTime(16, 0))).isZero();
    }

    @Test
    void countPresentEnrolledPerDayCountsEachPresentStudentOncePerDay() {
        assertThat(attendance.countPresentEnrolledPerDay(TODAY.minusDays(4), TODAY))
                .containsExactly(
                        entry(TODAY.minusDays(4), 4),
                        entry(TODAY.minusDays(3), 3),
                        entry(TODAY.minusDays(2), 4),
                        entry(TODAY.minusDays(1), 4),
                        entry(TODAY, 4));
    }

    @Test
    void countPresentEnrolledPerDayCountsAStudentWithTwoRecordsOnce() {
        attendance.markEntry(1001, TODAY.atTime(16, 0));

        assertThat(attendance.countPresentEnrolledPerDay(TODAY, TODAY)).containsEntry(TODAY, 4);
    }

    @Test
    void countPresentEnrolledPerDayLeavesOutStudentsNoLongerEnrolled() throws SQLException {
        TestDatabases.execute(
                database, "UPDATE student SET status = 'Graduated' WHERE studentNum = 1001");

        assertThat(attendance.countPresentEnrolledPerDay(TODAY, TODAY)).containsEntry(TODAY, 3);
    }

    @Test
    void countPresentEnrolledPerDayLeavesOutDaysWithoutAnyonePresent() {
        assertThat(attendance.countPresentEnrolledPerDay(TODAY.plusDays(1), TODAY.plusDays(3)))
                .isEmpty();
    }

    @Test
    void injectionTextInACourseNameDoesNotBreakEntry() throws SQLException {
        String injection = "x' OR '1'='1";
        TestDatabases.execute(
                database,
                "INSERT INTO course (course, description, degree) VALUES ('"
                        + injection.replace("'", "''")
                        + "', 'd', 'Bachelor')");
        TestDatabases.execute(
                database,
                "UPDATE student SET course = '"
                        + injection.replace("'", "''")
                        + "' WHERE studentNum = 1001");

        assertThat(attendance.markEntry(1001, TODAY.atTime(10, 0))).isTrue();
        assertThat(attendance.findOn(TODAY).getLast().getCourse()).isEqualTo(injection);
    }

    @ParameterizedTest
    @MethodSource("com.bsmi.attendancesystem.dao.TestDatabases#broken")
    void everyOperationOnBrokenDatabaseFailsWithDataAccessException(Database database) {
        AttendanceDao offline = new AttendanceDao(database);
        LocalDateTime now = TODAY.atTime(10, 0);

        assertThatThrownBy(() -> offline.findOn(TODAY)).isInstanceOf(DataAccessException.class);
        assertThatThrownBy(() -> offline.hasOpenEntry(1001, TODAY))
                .isInstanceOf(DataAccessException.class);
        assertThatThrownBy(() -> offline.markEntry(1001, now))
                .isInstanceOf(DataAccessException.class);
        assertThatThrownBy(() -> offline.markExit(1001, now))
                .isInstanceOf(DataAccessException.class);
        assertThatThrownBy(() -> offline.countPresentEnrolledPerDay(TODAY, TODAY))
                .isInstanceOf(DataAccessException.class);
    }
}
