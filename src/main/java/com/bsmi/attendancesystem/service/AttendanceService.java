package com.bsmi.attendancesystem.service;

import com.bsmi.attendancesystem.StudentAttendanceData;
import com.bsmi.attendancesystem.dao.AttendanceDao;
import com.bsmi.attendancesystem.dao.StudentDao;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The rules for attendance and the numbers on the Home screen. A student checks in (entry) and
 * out (exit) on the same day, and can't check in again while still checked in. Times are taken
 * from the clock, to the second.
 */
public final class AttendanceService {
    private static final String STUDENT_NUMBER = "Student #";

    private final AttendanceDao attendance;
    private final StudentDao students;
    private final Clock clock;

    public AttendanceService(AttendanceDao attendance, StudentDao students) {
        this(attendance, students, Clock.systemDefaultZone());
    }

    /** @param clock decides what "now" and "today" are, so tests can fix them */
    public AttendanceService(AttendanceDao attendance, StudentDao students, Clock clock) {
        this.attendance = attendance;
        this.students = students;
        this.clock = clock;
    }

    public List<StudentAttendanceData> listToday() {
        return attendance.findOn(today());
    }

    /**
     * @throws MissingFieldException if no student is chosen
     * @throws AlreadyExistsException if the student is still checked in from earlier today
     * @throws NotFoundException if no student has this number
     */
    public void markEntry(Integer studentNum) {
        requireChosen(studentNum);
        if (attendance.hasOpenEntry(studentNum, today())) {
            throw new AlreadyExistsException("An entry today for " + describe(studentNum));
        }
        if (!attendance.markEntry(studentNum, now())) {
            throw new NotFoundException(describe(studentNum));
        }
    }

    /**
     * @return how many open records of today got an exit time
     * @throws MissingFieldException if no student is chosen
     * @throws NotFoundException if the student has no open entry today
     */
    public int markExit(Integer studentNum) {
        requireChosen(studentNum);
        int closed = attendance.markExit(studentNum, now());
        if (closed == 0) {
            throw new NotFoundException("An open entry today for " + describe(studentNum));
        }
        return closed;
    }

    public DaySummary summarizeToday() {
        int enrolled = students.countEnrolled();
        int present = attendance.countPresentEnrolledPerDay(today(), today()).getOrDefault(today(), 0);
        return new DaySummary(enrolled, present, enrolled - present);
    }

    /** @return present students on each of the last {@code days} days, oldest first, 0 included */
    public Map<LocalDate, Integer> countPresentPerDay(int days) {
        LocalDate first = today().minusDays(days - 1L);
        Map<LocalDate, Integer> stored = attendance.countPresentEnrolledPerDay(first, today());
        Map<LocalDate, Integer> everyDay = new LinkedHashMap<>();
        for (LocalDate day = first; !day.isAfter(today()); day = day.plusDays(1)) {
            everyDay.put(day, stored.getOrDefault(day, 0));
        }
        return everyDay;
    }

    /**
     * Uses today's number of enrolled students for every day, because the database doesn't keep
     * who was enrolled on earlier days.
     *
     * @return absent students on each of the last {@code days} days, oldest first
     */
    public Map<LocalDate, Integer> countAbsentPerDay(int days) {
        int enrolled = students.countEnrolled();
        Map<LocalDate, Integer> absentPerDay = new LinkedHashMap<>();
        countPresentPerDay(days).forEach((day, present) -> absentPerDay.put(day, enrolled - present));
        return absentPerDay;
    }

    /** @return how many enrolled students were added on each of the latest {@code days} such days */
    public Map<LocalDate, Integer> countEnrolledAddedPerDay(int days) {
        return students.countEnrolledAddedPerDay(days);
    }

    /** @return the courses to offer once a semester is chosen; none if it isn't */
    public List<String> listCoursesIn(String semester) {
        return semester == null ? List.of() : students.findCoursesInSemester(semester);
    }

    /** @return the student numbers to offer once a semester and course are chosen */
    public List<Integer> listStudentNumbersIn(String semester, String course) {
        return semester == null || course == null
                ? List.of()
                : students.findNumbersIn(semester, course);
    }

    private static void requireChosen(Integer studentNum) {
        if (studentNum == null) {
            throw new MissingFieldException(STUDENT_NUMBER);
        }
    }

    private LocalDate today() {
        return LocalDate.now(clock);
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock).truncatedTo(ChronoUnit.SECONDS);
    }

    private static String describe(int studentNum) {
        return STUDENT_NUMBER + studentNum;
    }

    /** The three numbers at the top of the Home screen. */
    public record DaySummary(int enrolled, int present, int absent) {}
}
