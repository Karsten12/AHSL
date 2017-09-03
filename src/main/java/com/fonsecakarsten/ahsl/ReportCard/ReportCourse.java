package com.fonsecakarsten.ahsl.ReportCard;


import java.io.Serializable;

public class ReportCourse implements Serializable {

    private String course;
    private String letterGrade;
    private String period;
    private String teacher;

    public ReportCourse() {
        this.course = "";
        this.letterGrade = "";
        this.period = "";
        this.teacher = "";
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public String getLetterGrade() {
        return letterGrade;
    }

    public void setLetterGrade(String letterGrade) {
        this.letterGrade = letterGrade;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

}
