package com.bsmi.attendancesystem.service;

import static com.bsmi.attendancesystem.service.Fields.requireFilled;
import static com.bsmi.attendancesystem.service.Fields.trimmedOrEmpty;

import com.bsmi.attendancesystem.CourseData;
import com.bsmi.attendancesystem.dao.CourseDao;
import java.util.List;

/**
 * The rules for courses: every field is required, names are unique, and a course can't be deleted
 * while students or attendance records use it. Input is trimmed before it is checked or stored.
 */
public final class CourseService {
    private final CourseDao courses;

    public CourseService(CourseDao courses) {
        this.courses = courses;
    }

    public List<CourseData> listAll() {
        return courses.findAll();
    }

    /**
     * Lets the UI check the form before asking the user to confirm.
     *
     * @throws MissingFieldException naming the first empty field
     */
    public void requireComplete(CourseData course) {
        requireFilled(course.getCourse(), "Course");
        requireFilled(course.getDescription(), "Description");
        requireFilled(course.getDegree(), "Degree");
    }

    /** @throws MissingFieldException if the course name is empty */
    public void requireCourseName(String courseName) {
        requireFilled(courseName, "Course");
    }

    /** @throws AlreadyExistsException if the name is taken */
    public void add(CourseData course) {
        CourseData trimmed = trimmed(course);
        requireComplete(trimmed);
        if (courses.exists(trimmed.getCourse())) {
            throw new AlreadyExistsException(describe(trimmed.getCourse()));
        }
        courses.add(trimmed);
    }

    /** @throws NotFoundException if no course has this name */
    public void update(CourseData course) {
        CourseData trimmed = trimmed(course);
        requireComplete(trimmed);
        if (!courses.update(trimmed)) {
            throw new NotFoundException(describe(trimmed.getCourse()));
        }
    }

    /**
     * @throws StillInUseException if students or attendance records use the course
     * @throws NotFoundException if no course has this name
     */
    public void delete(String courseName) {
        String name = trimmedOrEmpty(courseName);
        requireCourseName(name);
        if (courses.isInUse(name)) {
            throw new StillInUseException(describe(name), "students or attendance records");
        }
        if (!courses.delete(name)) {
            throw new NotFoundException(describe(name));
        }
    }

    private static CourseData trimmed(CourseData course) {
        return new CourseData(
                trimmedOrEmpty(course.getCourse()),
                trimmedOrEmpty(course.getDescription()),
                trimmedOrEmpty(course.getDegree()));
    }

    private static String describe(String courseName) {
        return "Course " + courseName;
    }
}
