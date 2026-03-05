package com.xhf.leetcode.plugin.review.backend.model;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class ReviewQuestionModel implements ReviewQuestion {

    private String status;
    private String title;
    private String userRate;
    private String lastModify;
    private String nextReview;
    private String difficulty;
    private String userSolution;
    // index
    private Integer id;

    @Override
    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getUserRate() {
        return this.userRate;
    }

    public void setUserRate(String userRate) {
        this.userRate = userRate;
    }

    @Override
    public String getLastModify() {
        return this.lastModify;
    }

    public void setLastModify(String lastModify) {
        this.lastModify = lastModify;
    }

    @Override
    public String getNextReview() {
        return this.nextReview;
    }

    public void setNextReview(String nextReview) {
        this.nextReview = nextReview;
    }

    @Override
    public String getDifficulty() {
        return this.difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public void setUserSolution(String userSolution) {
        this.userSolution = userSolution;
    }

    @Override
    public String getUserNoteText() {
        return this.userSolution;
    }

    @Override
    public void setBack(String newSolution) {
        this.userSolution = newSolution;
    }

    @Override
    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "ReviewQuestionModel{" +
            "status='" + status + '\'' +
            ", title='" + title + '\'' +
            ", userRate='" + userRate + '\'' +
            ", lastModify='" + lastModify + '\'' +
            ", nextReview='" + nextReview + '\'' +
            ", difficulty='" + difficulty + '\'' +
            ", userSolution='" + userSolution + '\'' +
            ", id=" + id +
            '}';
    }
}
