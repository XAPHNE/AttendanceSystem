package com.bsmi.attendancesystem.dao;

import com.bsmi.attendancesystem.StudentAttendanceData;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads and writes the {@code student_attendance} table. Days run from midnight to midnight; a
 * record is "open" while the student has an entry time but no exit time yet.
 */
public final class AttendanceDao {
    // Only student_attendance has entry_time, so this works in joins without a table prefix
    private static final String ON_DAY = "entry_time >= ? AND entry_time < ?";

    private final Database database;

    public AttendanceDao(Database database) {
        this.database = database;
    }

    /** @return the records whose entry time is on that day, in the order they were made */
    public List<StudentAttendanceData> findOn(LocalDate day) {
        String sql =
                "SELECT s.studentNum, s.semester, s.course, a.entry_time, a.exit_time, a.status"
                        + " FROM student_attendance a JOIN student s ON a.student_id = s.id"
                        + " WHERE "
                        + ON_DAY
                        + " ORDER BY a.id";
        try (Connection connection = database.open();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            setDay(statement, 1, day);
            try (ResultSet rows = statement.executeQuery()) {
                List<StudentAttendanceData> records = new ArrayList<>();
                while (rows.next()) {
                    records.add(
                            new StudentAttendanceData(
                                    rows.getString("studentNum"),
                                    rows.getString("semester"),
                                    rows.getString("course"),
                                    rows.getString("entry_time"),
                                    rows.getString("exit_time"),
                                    rows.getString("status")));
                }
                return records;
            }
        } catch (SQLException e) {
            throw new DataAccessException("load the attendance of " + day, e);
        }
    }

    public boolean hasOpenEntry(int studentNum, LocalDate day) {
        String sql =
                "SELECT 1 FROM student_attendance a JOIN student s ON a.student_id = s.id"
                        + " WHERE s.studentNum = ? AND a.exit_time IS NULL AND "
                        + ON_DAY;
        try (Connection connection = database.open();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, studentNum);
            setDay(statement, 2, day);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next();
            }
        } catch (SQLException e) {
            throw new DataAccessException(
                    "check whether student #" + studentNum + " is checked in", e);
        }
    }

    /**
     * Records the student as present from {@code entryTime}, for the course they are enrolled in.
     *
     * @return false if no student has that number
     */
    public boolean markEntry(int studentNum, LocalDateTime entryTime) {
        String sql =
                "INSERT INTO student_attendance (student_id, course_id, entry_time, status)"
                        + " SELECT s.id, c.id, ?, 'Present'"
                        + " FROM student s JOIN course c ON c.course = s.course"
                        + " WHERE s.studentNum = ?";
        try (Connection connection = database.open();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, entryTime);
            statement.setInt(2, studentNum);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("mark the entry of student #" + studentNum, e);
        }
    }

    /**
     * Sets {@code exitTime} on the student's open records of that same day.
     *
     * @return how many records were closed; 0 if the student had none open
     */
    public int markExit(int studentNum, LocalDateTime exitTime) {
        String sql =
                "UPDATE student_attendance SET exit_time = ? WHERE exit_time IS NULL AND "
                        + ON_DAY
                        + " AND student_id IN (SELECT id FROM student WHERE studentNum = ?)";
        try (Connection connection = database.open();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, exitTime);
            setDay(statement, 2, exitTime.toLocalDate());
            statement.setInt(4, studentNum);
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("mark the exit of student #" + studentNum, e);
        }
    }

    /**
     * Counts each enrolled student who was present at least once a day. Students who are no
     * longer enrolled are left out, so "absent = enrolled - present" can't go below zero.
     *
     * @return present students per day from {@code first} to {@code last}, oldest first; days
     *     without anyone present are left out
     */
    public Map<LocalDate, Integer> countPresentEnrolledPerDay(LocalDate first, LocalDate last) {
        String sql =
                "SELECT CAST(a.entry_time AS DATE) AS attendance_day, COUNT(DISTINCT a.student_id)"
                        + " FROM student_attendance a JOIN student s ON a.student_id = s.id"
                        + " WHERE a.status = 'Present' AND s.status = 'Enrolled' AND "
                        + ON_DAY
                        + " GROUP BY attendance_day ORDER BY attendance_day";
        try (Connection connection = database.open();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, first.atStartOfDay());
            statement.setObject(2, last.plusDays(1).atStartOfDay());
            try (ResultSet rows = statement.executeQuery()) {
                Map<LocalDate, Integer> presentPerDay = new LinkedHashMap<>();
                while (rows.next()) {
                    presentPerDay.put(rows.getObject(1, LocalDate.class), rows.getInt(2));
                }
                return presentPerDay;
            }
        } catch (SQLException e) {
            throw new DataAccessException("count who was present", e);
        }
    }

    /** Fills two placeholders with the start of {@code day} and the start of the next day. */
    private static void setDay(PreparedStatement statement, int first, LocalDate day)
            throws SQLException {
        statement.setObject(first, day.atStartOfDay());
        statement.setObject(first + 1, day.plusDays(1).atStartOfDay());
    }
}
