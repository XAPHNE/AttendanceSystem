package com.bsmi.attendancesystem.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;

import com.bsmi.attendancesystem.StudentAttendanceData;
import com.bsmi.attendancesystem.dao.AttendanceDao;
import com.bsmi.attendancesystem.dao.DataAccessException;
import com.bsmi.attendancesystem.dao.Database;
import com.bsmi.attendancesystem.dao.StudentDao;
import com.bsmi.attendancesystem.dao.TestDatabases;
import com.bsmi.attendancesystem.service.AttendanceService.DaySummary;
import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AttendanceServiceTest {
    // The demo data is dated relative to the real today, so the fixed clock stays on that day
    private static final LocalDate TODAY = LocalDate.now();
    private static final LocalDateTime TEN_AM = TODAY.atTime(10, 0, 30, 999_000_000);

    private Database database;
    private AttendanceService service;

    @BeforeEach
    void useFreshDatabase() {
        database = TestDatabases.migratedInMemory();
        service = serviceAt(TEN_AM);
    }

    @Test
    void listTodayReturnsOnlyTodaysRecords() {
        assertThat(service.listToday())
                .hasSize(5)
                .allSatisfy(record -> assertThat(record.getEntryTime()).startsWith(TODAY.toString()));
    }

    @Test
    void markEntryAddsARecordAtTheCurrentSecond() {
        service.markEntry(1001);

        StudentAttendanceData added = service.listToday().getLast();
        assertThat(added.getStudentNum()).isEqualTo("1001");
        assertThat(added.getEntryTime()).isEqualTo(TODAY + " 10:00:30");
    }

    @Test
    void markEntryWithoutAChosenStudentNamesTheField() {
        assertThatThrownBy(() -> service.markEntry(null))
                .isInstanceOf(MissingFieldException.class)
                .hasMessage("Please fill in the Student # field");
    }

    @Test
    void markEntryWhileStillCheckedInIsRefused() {
        service.markEntry(1001);

        assertThatThrownBy(() -> service.markEntry(1001))
                .isInstanceOf(AlreadyExistsException.class)
                .hasMessage("An entry today for Student #1001 already exists");
        assertThat(service.listToday()).hasSize(6);
    }

    @Test
    void markEntryAfterAnExitStartsANewRecord() {
        service.markEntry(1001);
        service.markExit(1001);

        service.markEntry(1001);

        assertThat(service.listToday()).hasSize(7);
    }

    @Test
    void markEntryOfUnknownStudentIsRefused() {
        assertThatThrownBy(() -> service.markEntry(9999))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Student #9999 does not exist");
    }

    @Test
    void markExitClosesTodaysOpenEntry() {
        service.markEntry(1001);
        AttendanceService later = serviceAt(TODAY.atTime(16, 5));

        assertThat(later.markExit(1001)).isEqualTo(1);

        assertThat(later.listToday().getLast().getExitTime()).isEqualTo(TODAY + " 16:05:00");
    }

    @Test
    void markExitWithoutAChosenStudentNamesTheField() {
        assertThatThrownBy(() -> service.markExit(null))
                .isInstanceOf(MissingFieldException.class)
                .hasMessage("Please fill in the Student # field");
    }

    @Test
    void markExitWithoutAnOpenEntryIsRefused() {
        assertThatThrownBy(() -> service.markExit(1001))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("An open entry today for Student #1001 does not exist");
    }

    @Test
    void markExitDoesNotCloseYesterdaysEntry() {
        serviceAt(TODAY.minusDays(1).atTime(10, 0)).markEntry(1001);

        assertThatThrownBy(() -> service.markExit(1001)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void summarizeTodayCountsEnrolledPresentAndAbsent() {
        assertThat(service.summarizeToday()).isEqualTo(new DaySummary(5, 4, 1));
    }

    @Test
    void summarizeTodayOfDayWithoutAttendanceCountsEveryoneAbsent() {
        assertThat(serviceAt(TODAY.plusDays(1).atTime(10, 0)).summarizeToday())
                .isEqualTo(new DaySummary(5, 0, 5));
    }

    @Test
    void summarizeTodayNeverGoesNegativeWhenPresentStudentsLeave() throws SQLException {
        TestDatabases.execute(database, "UPDATE student SET status = 'Graduated'");

        assertThat(service.summarizeToday()).isEqualTo(new DaySummary(0, 0, 0));
    }

    @Test
    void countPresentPerDayFillsEveryDayOldestFirst() {
        AttendanceService tomorrow = serviceAt(TODAY.plusDays(1).atTime(10, 0));

        assertThat(tomorrow.countPresentPerDay(3))
                .containsExactly(
                        entry(TODAY.minusDays(1), 4), entry(TODAY, 4), entry(TODAY.plusDays(1), 0));
    }

    @Test
    void countAbsentPerDayIsTodaysEnrolledMinusPresent() {
        assertThat(service.countAbsentPerDay(5))
                .containsExactly(
                        entry(TODAY.minusDays(4), 1),
                        entry(TODAY.minusDays(3), 2),
                        entry(TODAY.minusDays(2), 1),
                        entry(TODAY.minusDays(1), 1),
                        entry(TODAY, 1));
    }

    @Test
    void countEnrolledAddedPerDayGivesTheLatestDays() {
        assertThat(service.countEnrolledAddedPerDay(1)).containsExactly(entry(TODAY.minusDays(5), 1));
    }

    @Test
    void listCoursesInChosenSemester() {
        assertThat(service.listCoursesIn("3rd Sem")).containsExactly("BCA");
    }

    @Test
    void listCoursesBeforeASemesterIsChosenIsEmpty() {
        assertThat(service.listCoursesIn(null)).isEmpty();
    }

    @Test
    void listStudentNumbersInChosenSemesterAndCourse() {
        assertThat(service.listStudentNumbersIn("3rd Sem", "BCA")).containsExactly(1001, 1002);
    }

    @Test
    void listStudentNumbersBeforeBothAreChosenIsEmpty() {
        assertThat(service.listStudentNumbersIn(null, "BCA")).isEmpty();
        assertThat(service.listStudentNumbersIn("3rd Sem", null)).isEmpty();
    }

    @Test
    void databaseFailuresReachTheCallerUnchanged() {
        Database offline = TestDatabases.unreachable();
        AttendanceService broken =
                new AttendanceService(new AttendanceDao(offline), new StudentDao(offline));

        assertThatThrownBy(broken::listToday).isInstanceOf(DataAccessException.class);
        assertThatThrownBy(() -> broken.markEntry(1001)).isInstanceOf(DataAccessException.class);
        assertThatThrownBy(() -> broken.markExit(1001)).isInstanceOf(DataAccessException.class);
        assertThatThrownBy(broken::summarizeToday).isInstanceOf(DataAccessException.class);
        assertThatThrownBy(() -> broken.countAbsentPerDay(5))
                .isInstanceOf(DataAccessException.class);
        assertThatThrownBy(() -> broken.countEnrolledAddedPerDay(5))
                .isInstanceOf(DataAccessException.class);
        assertThatThrownBy(() -> broken.listCoursesIn("3rd Sem"))
                .isInstanceOf(DataAccessException.class);
    }

    private AttendanceService serviceAt(LocalDateTime now) {
        ZoneId zone = ZoneId.systemDefault();
        Clock fixed = Clock.fixed(now.atZone(zone).toInstant(), zone);
        return new AttendanceService(new AttendanceDao(database), new StudentDao(database), fixed);
    }
}
