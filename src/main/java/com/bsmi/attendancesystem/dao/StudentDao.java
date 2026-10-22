package com.bsmi.attendancesystem.dao;

import com.bsmi.attendancesystem.StudentData;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Reads and writes the {@code student} table. Students are identified by their student number. */
public final class StudentDao {
    private final Database database;

    public StudentDao(Database database) {
        this.database = database;
    }

    private static final String STUDENT_COLUMNS =
            "studentNum, semester, course, firstName, lastName, gender, birthDate, status, image,"
                    + " card_id";

    /** @return every student, in the order they were added */
    public List<StudentData> findAll() {
        String sql = "SELECT " + STUDENT_COLUMNS + " FROM student ORDER BY id";
        try (Connection connection = database.open();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet rows = statement.executeQuery()) {
            List<StudentData> students = new ArrayList<>();
            while (rows.next()) {
                students.add(readStudent(rows));
            }
            return students;
        } catch (SQLException e) {
            throw new DataAccessException("load the students", e);
        }
    }

    /** @return the student whose card gives this ID when scanned, or empty if nobody has it */
    public Optional<StudentData> findByCardId(String cardId) {
        String sql = "SELECT " + STUDENT_COLUMNS + " FROM student WHERE card_id = ?";
        try (Connection connection = database.open();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, cardId);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? Optional.of(readStudent(rows)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("find the student with card " + cardId, e);
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
     * @throws DataAccessException also when the student number or card ID is taken or a value
     *     breaks a column rule, e.g. an unknown gender
     */
    public void add(StudentData student) {
        String sql =
                "INSERT INTO student (studentNum, semester, course, firstName, lastName, gender,"
                        + " birthDate, status, image, card_id, added_on)"
                        + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_DATE)";
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
                        + " gender = ?, birthDate = ?, status = ?, image = ?, card_id = ?"
                        + " WHERE studentNum = ?";
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

    public int countEnrolled() {
        String sql = "SELECT COUNT(*) FROM student WHERE status = 'Enrolled'";
        try (Connection connection = database.open();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet rows = statement.executeQuery()) {
            rows.next();
            return rows.getInt(1);
        } catch (SQLException e) {
            throw new DataAccessException("count the enrolled students", e);
        }
    }

    /**
     * @return how many of today's enrolled students were added on each of the latest {@code
     *     days} days that had any, oldest first
     */
    public Map<LocalDate, Integer> countEnrolledAddedPerDay(int days) {
        String sql =
                "SELECT added_on, COUNT(*) FROM student WHERE status = 'Enrolled'"
                        + " GROUP BY added_on ORDER BY added_on DESC LIMIT ?";
        try (Connection connection = database.open();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, days);
            try (ResultSet rows = statement.executeQuery()) {
                LinkedHashMap<LocalDate, Integer> newestFirst = new LinkedHashMap<>();
                while (rows.next()) {
                    newestFirst.put(rows.getObject(1, LocalDate.class), rows.getInt(2));
                }
                return newestFirst.reversed();
            }
        } catch (SQLException e) {
            throw new DataAccessException("count the students added per day", e);
        }
    }

    /** @return the courses that students of this semester take, alphabetically */
    public List<String> findCoursesInSemester(String semester) {
        String sql = "SELECT DISTINCT course FROM student WHERE semester = ? ORDER BY course";
        try (Connection connection = database.open();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, semester);
            try (ResultSet rows = statement.executeQuery()) {
                List<String> courses = new ArrayList<>();
                while (rows.next()) {
                    courses.add(rows.getString(1));
                }
                return courses;
            }
        } catch (SQLException e) {
            throw new DataAccessException("load the courses of " + semester, e);
        }
    }

    /** @return the numbers of the students in this semester and course, lowest first */
    public List<Integer> findNumbersIn(String semester, String course) {
        String sql =
                "SELECT studentNum FROM student WHERE semester = ? AND course = ?"
                        + " ORDER BY studentNum";
        try (Connection connection = database.open();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, semester);
            statement.setString(2, course);
            try (ResultSet rows = statement.executeQuery()) {
                List<Integer> numbers = new ArrayList<>();
                while (rows.next()) {
                    numbers.add(rows.getInt(1));
                }
                return numbers;
            }
        } catch (SQLException e) {
            throw new DataAccessException("load the students of " + semester + " " + course, e);
        }
    }

    /**
     * Fills the nine columns after the student number, in table order, starting at {@code first}.
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
        statement.setString(index++, student.getCardId());
        return index;
    }

    /** Reads the {@link #STUDENT_COLUMNS} of the current row. */
    private static StudentData readStudent(ResultSet rows) throws SQLException {
        return new StudentData(
                rows.getInt("studentNum"),
                rows.getString("semester"),
                rows.getString("course"),
                rows.getString("firstName"),
                rows.getString("lastName"),
                rows.getString("gender"),
                rows.getDate("birthDate"),
                rows.getString("status"),
                rows.getString("image"),
                rows.getString("card_id"));
    }
}
