package com.fonsecakarsten.ahsl.Calendar;

import java.io.Serializable;

public class calendar implements Serializable {
    private String weekDay;
    private String CourseName;
    private String Assignment;
    private String Category;
    private String AssignmentType;

    public calendar() {
        this.weekDay = "";
        this.CourseName = "";
        this.Assignment = "";
        this.Category = "";
        this.AssignmentType = "";
    }

    public calendar(String WeekDay, String CourseName, String Assignment, String Category, String AssignmentType) {
        this.weekDay = WeekDay;
        this.CourseName = CourseName;
        this.Assignment = Assignment;
        this.Category = Category;
        this.AssignmentType = AssignmentType;
    }

    public void setWeekDay(String weekDay) {
        this.weekDay = weekDay;
    }

    public String getWeekDay() {
        return weekDay;
    }

    public String getCourseName() {
        return CourseName;
    }

    public void setCourseName(String CourseName) {
        this.CourseName = CourseName;
    }

    public String getAssignment() {
        return Assignment;
    }

    public void setAssignment(String Assignment) {
        this.Assignment = Assignment;
    }

    public String getAssignmentType() {
        return AssignmentType;
    }

    public void setAssignmentType(String AssignmentType) {
        this.AssignmentType = AssignmentType;
    }

    public String getCategory(String Category) {
        return Category;
    }

    public void setCategory(String Category) {
        this.Category = Category;
    }

}
