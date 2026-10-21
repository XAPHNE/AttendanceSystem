package com.bsmi.attendancesystem.service;

import static com.bsmi.attendancesystem.service.Fields.requireFilled;
import static com.bsmi.attendancesystem.service.Fields.trimmedOrEmpty;

import com.bsmi.attendancesystem.StudentData;
import com.bsmi.attendancesystem.dao.StudentDao;
import java.time.LocalDate;
import java.util.List;

/**
 * The rules for students: every field except the photo is required, student numbers are positive
 * and unique, and the birth date is in the past. Text is trimmed before it is checked or stored.
 */
public final class StudentService {
    private static final String STUDENT_NUMBER = "Student #";

    private final StudentDao students;

    public StudentService(StudentDao students) {
        this.students = students;
    }

    public List<StudentData> listAll() {
        return students.findAll();
    }

    /**
     * Turns the text of the Student # field into a number.
     *
     * @throws MissingFieldException if the text is empty
     * @throws InvalidValueException if the text isn't a positive whole number
     */
    public int parseStudentNumber(String text) {
        requireFilled(text, STUDENT_NUMBER);
        try {
            int number = Integer.parseInt(text.strip());
            if (number > 0) {
                return number;
            }
        } catch (NumberFormatException tooLongOrNotANumber) {
            // reported below, the same way as zero or a negative number
        }
        throw new InvalidValueException(STUDENT_NUMBER, "a positive whole number");
    }

    /**
     * Lets the UI check the form before asking the user to confirm.
     *
     * @throws MissingFieldException naming the first empty field, in form order
     * @throws InvalidValueException if the birth date is today or later
     */
    public void requireValid(StudentData student) {
        if (student.getStudentNum() == null) {
            throw new MissingFieldException(STUDENT_NUMBER);
        }
        requireFilled(student.getSemester(), "Semester");
        requireFilled(student.getCourse(), "Course");
        requireFilled(student.getFirstName(), "First Name");
        requireFilled(student.getLastName(), "Last Name");
        requireFilled(student.getGender(), "Gender");
        if (student.getBirthDate() == null) {
            throw new MissingFieldException("Birth Date");
        }
        if (!student.getBirthDate().toLocalDate().isBefore(LocalDate.now())) {
            throw new InvalidValueException("Birth Date", "in the past");
        }
        requireFilled(student.getStatus(), "Status");
    }

    /** @throws AlreadyExistsException if the student number is taken */
    public void add(StudentData student) {
        StudentData trimmed = trimmed(student);
        requireValid(trimmed);
        if (students.exists(trimmed.getStudentNum())) {
            throw new AlreadyExistsException(describe(trimmed.getStudentNum()));
        }
        students.add(trimmed);
    }

    /** @throws NotFoundException if no student has this number */
    public void update(StudentData student) {
        StudentData trimmed = trimmed(student);
        requireValid(trimmed);
        if (!students.update(trimmed)) {
            throw new NotFoundException(describe(trimmed.getStudentNum()));
        }
    }

    /**
     * Deletes the student and their attendance records.
     *
     * @throws NotFoundException if no student has this number
     */
    public void delete(int studentNum) {
        if (!students.delete(studentNum)) {
            throw new NotFoundException(describe(studentNum));
        }
    }

    private static StudentData trimmed(StudentData student) {
        return new StudentData(
                student.getStudentNum(),
                trimmedOrEmpty(student.getSemester()),
                trimmedOrEmpty(student.getCourse()),
                trimmedOrEmpty(student.getFirstName()),
                trimmedOrEmpty(student.getLastName()),
                trimmedOrEmpty(student.getGender()),
                student.getBirthDate(),
                trimmedOrEmpty(student.getStatus()),
                trimmedOrEmpty(student.getImage()));
    }

    private static String describe(int studentNum) {
        return STUDENT_NUMBER + studentNum;
    }
}
