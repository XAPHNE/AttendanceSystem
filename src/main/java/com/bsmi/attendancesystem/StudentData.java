package com.bsmi.attendancesystem;

import java.sql.Date;

public class StudentData {
    private Integer studentNum;
    private String semester;
    private String course;
    private String firstName;
    private String lastName;
    private String gender;
    private Date birthDate;
    private String status;
    private String image;
    private String cardId;

    /** A student without a card. */
    public StudentData (Integer studentNum, String semester, String course, String firstName, String lastName, String gender, Date birthDate, String status, String image) {
        this(studentNum, semester, course, firstName, lastName, gender, birthDate, status, image, null);
    }

    /** @param cardId what the student's card gives when scanned, or null for no card */
    public StudentData (Integer studentNum, String semester, String course, String firstName, String lastName, String gender, Date birthDate, String status, String image, String cardId) {
        this.studentNum = studentNum;
        this.semester = semester;
        this.course = course;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.birthDate = birthDate;
        this.status = status;
        this.image = image;
        this.cardId = cardId;
    }

    public Integer getStudentNum() {
        return studentNum;
    }

    public String getSemester() {
        return semester;
    }

    public String getCourse() {
        return course;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getGender() {
        return gender;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public String getStatus() {
        return status;
    }

    public String getImage() {
        return image;
    }

    public String getCardId() {
        return cardId;
    }
}
