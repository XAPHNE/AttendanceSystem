package com.bsmi.attendancesystem;

public class StudentAttendanceData {
    private String studentNum;
    private String semester;
    private String course;
    private String entryTime;
    private String exitTime;
    private String status;
    public StudentAttendanceData(String studentNum, String semester,String course, String entryTime, String exitTime, String status) {
        this.studentNum = studentNum;
        this.semester = semester;
        this.course = course;
        this.entryTime = entryTime;
        this.exitTime = exitTime;
        this.status = status;
    }
    public String getStudentNum() {
        return studentNum;
    }

    public void setStudentNum(String studentNum) {
        this.studentNum = studentNum;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(String entryTime) {
        this.entryTime = entryTime;
    }

    public String getExitTime() {
        return exitTime;
    }

    public void setExitTime(String exitTime) {
        this.exitTime = exitTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
