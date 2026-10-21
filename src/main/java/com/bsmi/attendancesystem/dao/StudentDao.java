package com.bsmi.attendancesystem.dao;

import com.bsmi.attendancesystem.StudentData;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Reads and writes the {@code student} table. Students are identified by their student number. */
public final class StudentDao {
    private final Database database;

    public StudentDao(Database database) {
        this.database = database;
    }

    /** @return every student, in the order they were added */
    public List<StudentData> findAll() {
        String sql =
                "SELECT studentNum, semester, course, firstName, lastName, gender, birthDate,"
                        + " status, image FROM student ORDER BY id";
        try (Connection connection = database.open();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet rows = statement.executeQuery()) {
            List<StudentData> students = new ArrayList<>();
            while (rows.next()) {
                students.add(
                        new StudentData(
                                rows.getInt("studentNum"),
                                rows.getString("semester"),
                                rows.getString("course"),
                                rows.getString("firstName"),
                                rows.getString("lastName"),
                                rows.getString("gender"),
                                rows.getDate("birthDate"),
                                rows.getString("status"),
                                rows.getString("image")));
            }
            return students;
        } catch (SQLException e) {
            throw new DataAccessException("load the students", e);
        }
    }

    public boolean exists(int studentNum) {
        String sql = "SELECT 1 FROM student WHERE studentNum = ?";
        try (Connection connection = database.open();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, studentNum);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next();
            }
        } catch (SQLException e) {
            throw new DataAccessException("check whether student #" + studentNum + " exists", e);
        }
    }

    /**
     * Stores the student with today as the date they were added.
     *
     * @throws DataAccessException also when the student number is taken or a value breaks a
     *     column rule, e.g. an unknown gender
     */
    public void add(StudentData student) {
        String sql =
                "INSERT INTO student (studentNum, semester, course, firstName, lastName, gender,"
                        + " birthDate, status, image, added_on)"
                        + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_DATE)";
        try (Connection connection = database.open();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, student.getStudentNum());
            setDetails(statement, student, 2);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("add student #" + student.getStudentNum(), e);
        }
    }

    /**
     * Changes everything except the student number of the student with the same number.
     *
     * @return false if no student has that number
     */
    public boolean update(StudentData student) {
        String sql =
                "UPDATE student SET semester = ?, course = ?, firstName = ?, lastName = ?,"
                        + " gender = ?, birthDate = ?, status = ?, image = ? WHERE studentNum = ?";
        try (Connection connection = database.open();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            int next = setDetails(statement, student, 1);
            statement.setInt(next, student.getStudentNum());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("update student #" + student.getStudentNum(), e);
        }
    }

    /**
     * Deletes the student together with their attendance records.
     *
     * @return false if no student has that number
     */
    public boolean delete(int studentNum) {
        String sql = "DELETE FROM student WHERE studentNum = ?";
        try (Connection connection = database.open();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, studentNum);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("delete student #" + studentNum, e);
        }
    }

    /**
     * Fills the eight columns after the student number, in table order, starting at {@code first}.
     *
     * @return the index of the next placeholder
     */
    private static int setDetails(PreparedStatement statement, StudentData student, int first)
            throws SQLException {
        int index = first;
        statement.setString(index++, student.getSemester());
        statement.setString(index++, student.getCourse());
        statement.setString(index++, student.getFirstName());
        statement.setString(index++, student.getLastName());
        statement.setString(index++, student.getGender());
        statement.setDate(index++, student.getBirthDate());
        statement.setString(index++, student.getStatus());
        statement.setString(index++, student.getImage());
        return index;
    }
}
