package com.bsmi.attendancesystem.service;

import static com.bsmi.attendancesystem.service.Fields.requireFilled;
import static com.bsmi.attendancesystem.service.Fields.trimmedOrEmpty;

import com.bsmi.attendancesystem.StudentData;
import com.bsmi.attendancesystem.dao.StudentDao;
import java.time.LocalDate;
import java.util.List;

/**
 * The rules for students: every field except the photo and the card ID is required, student
 * numbers and card IDs are unique, student numbers are positive, and the birth date is in the
 * past. Text is trimmed before it is checked or stored, and a blank card ID means "no card".
 */
public final class StudentService {
    private static final String STUDENT_NUMBER = "Student #";
    private static final String CARD_ID = "Card ID";
    /** The size of the {@code card_id} column. */
    static final int CARD_ID_MAX_LENGTH = 64;

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
     * @throws InvalidValueException if the birth date is today or later, or the card ID is longer
     *     than {@value #CARD_ID_MAX_LENGTH} characters
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
        if (student.getCardId() != null && student.getCardId().length() > CARD_ID_MAX_LENGTH) {
            throw new InvalidValueException(
                    CARD_ID, "at most " + CARD_ID_MAX_LENGTH + " characters long");
        }
    }

    /** @throws AlreadyExistsException if the student number or the card ID is taken */
    public void add(StudentData student) {
        StudentData trimmed = trimmed(student);
        requireValid(trimmed);
        if (students.exists(trimmed.getStudentNum())) {
            throw new AlreadyExistsException(describe(trimmed.getStudentNum()));
        }
        requireCardFree(trimmed);
        students.add(trimmed);
    }

    /**
     * @throws AlreadyExistsException if another student has the card ID
     * @throws NotFoundException if no student has this number
     */
    public void update(StudentData student) {
        StudentData trimmed = trimmed(student);
        requireValid(trimmed);
        requireCardFree(trimmed);
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

    /** A student may keep their own card, but not take someone else's. */
    private void requireCardFree(StudentData student) {
        if (student.getCardId() == null) {
            return;
        }
        boolean takenByAnother =
                students.findByCardId(student.getCardId())
                        .filter(owner -> !owner.getStudentNum().equals(student.getStudentNum()))
                        .isPresent();
        if (takenByAnother) {
            throw new AlreadyExistsException(CARD_ID + " " + student.getCardId());
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
                trimmedOrEmpty(student.getImage()),
                trimmedOrNoCard(student.getCardId()));
    }

    private static String trimmedOrNoCard(String cardId) {
        String trimmed = trimmedOrEmpty(cardId);
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String describe(int studentNum) {
        return STUDENT_NUMBER + studentNum;
    }
}
