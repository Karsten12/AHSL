package com.fonsecakarsten.ahsl.Grades;

import java.io.Serializable;

public class GradeDetail implements Serializable {
    private String detailName;
    private String category;
    private String dueDate;
    private String comment;
    private String submissions;
    private String displayScore;
    private String displayPercent;
    private double pointsEarned;
    private double totalPoints;
    private String lastUpdateDate;
    // Used for the percentage in each category
    private String ScoreCategory;
    private String ScorePercent;
    private String ScoreWeight;

    public GradeDetail() {
        this.detailName = "";
        this.category = "";
        this.dueDate = "";
        this.pointsEarned = 0.0d;
        this.totalPoints = 0.0d;
        this.comment = "";
        this.submissions = "";
        this.lastUpdateDate = "";
        // Used for the percentage in each category

        this.ScoreCategory = "";
        this.ScorePercent = "";
        this.ScoreWeight = "";
    }

    public String getDetailName() {
        return detailName;
    }

    public void setDetailName(String detailName) {
        this.detailName = detailName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public double getPointsEarned() {
        return pointsEarned;
    }

    public void setPointsEarned(double pointsEarned) {
        this.pointsEarned = pointsEarned;
    }

    public double getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(double totalPoints) {
        this.totalPoints = totalPoints;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getSubmissions() {
        return submissions;
    }

    public void setSubmissions(String submissions) {
        this.submissions = submissions;
    }

    public String getDisplayScore() {
        return displayScore;
    }

    public void setDisplayScore(String displayScore) {
        this.displayScore = displayScore;
    }

    public String getDisplayPercent() {
        return displayPercent;
    }

    public void setDisplayPercent(String displayPercent) {
        this.displayPercent = displayPercent;
    }

    public String getLastPublishDate() {
        return lastUpdateDate;
    }

    public void setLastPublishDate(String lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }


    // Used for the percentage in each category

    public void setScoreCategory(String ScoreCategory) {
        this.ScoreCategory = ScoreCategory;
    }

    public String getScoreCategory() {
        return ScoreCategory;
    }

    public void setScorePercent(String ScorePercent) {
        this.ScorePercent = ScorePercent;
    }

    public String getScorePercent() {
        return ScorePercent;
    }

    public void setScoreWeight(String ScoreWeight) {
        this.ScoreWeight = ScoreWeight;
    }

    public String getScoreWeight() {
        return ScoreWeight;
    }
}
