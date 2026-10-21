package com.bsmi.attendancesystem.dao;

import com.bsmi.attendancesystem.CourseData;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Reads and writes the {@code course} table. Courses are identified by their name. */
public final class CourseDao {
    private final Database database;

    public CourseDao(Database database) {
        this.database = database;
    }

    /** @return every course, in the order they were added */
    public List<CourseData> findAll() {
        String sql = "SELECT course, description, degree FROM course ORDER BY id";
        try (Connection connection = database.open();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet rows = statement.executeQuery()) {
            List<CourseData> courses = new ArrayList<>();
            while (rows.next()) {
                courses.add(
                        new CourseData(
                                rows.getString("course"),
                                rows.getString("description"),
                                rows.getString("degree")));
            }
            return courses;
        } catch (SQLException e) {
            throw new DataAccessException("load the courses", e);
        }
    }

    public boolean exists(String courseName) {
        String sql = "SELECT 1 FROM course WHERE course = ?";
        try (Connection connection = database.open();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, courseName);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next();
            }
        } catch (SQLException e) {
            throw new DataAccessException("check whether course " + courseName + " exists", e);
        }
    }

    /** @return true if a student is in this course or attendance was recorded for it */
    public boolean isInUse(String courseName) {
        String sql =
                "SELECT EXISTS (SELECT 1 FROM student WHERE course = ?)"
                        + " OR EXISTS (SELECT 1 FROM student_attendance a"
                        + " JOIN course c ON c.id = a.course_id WHERE c.course = ?)";
        try (Connection connection = database.open();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, courseName);
            statement.setString(2, courseName);
            try (ResultSet rows = statement.executeQuery()) {
                rows.next();
                return rows.getBoolean(1);
            }
        } catch (SQLException e) {
            throw new DataAccessException("check whether course " + courseName + " is used", e);
        }
    }

    /** @throws DataAccessException also when a course with this name already exists */
    public void add(CourseData course) {
        String sql = "INSERT INTO course (course, description, degree) VALUES (?, ?, ?)";
        try (Connection connection = database.open();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, course.getCourse());
            statement.setString(2, course.getDescription());
            statement.setString(3, course.getDegree());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("add course " + course.getCourse(), e);
        }
    }

    /**
     * Changes the description and degree of the course with the same name.
     *
     * @return false if no course has that name
     */
    public boolean update(CourseData course) {
        String sql = "UPDATE course SET description = ?, degree = ? WHERE course = ?";
        try (Connection connection = database.open();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, course.getDescription());
            statement.setString(2, course.getDegree());
            statement.setString(3, course.getCourse());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("update course " + course.getCourse(), e);
        }
    }

    /**
     * @return false if no course has that name
     * @throws DataAccessException also when attendance records still point at the course
     */
    public boolean delete(String courseName) {
        String sql = "DELETE FROM course WHERE course = ?";
        try (Connection connection = database.open();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, courseName);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("delete course " + courseName, e);
        }
    }
}
