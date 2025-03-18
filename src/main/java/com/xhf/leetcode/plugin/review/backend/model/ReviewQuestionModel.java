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

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String getStatus() {
        return this.status;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getTitle() {
        return this.title;
    }

    public void setUserRate(String userRate) {
        this.userRate = userRate;
    }

    @Override
    public String getUserRate() {
        return this.userRate;
    }

    public void setLastModify(String lastModify) {
        this.lastModify = lastModify;
    }

    @Override
    public String getLastModify() {
        return this.lastModify;
    }

    public void setNextReview(String nextReview) {
        this.nextReview = nextReview;
    }

    @Override
    public String getNextReview() {
        return this.nextReview;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    @Override
    public String getDifficulty() {
        return this.difficulty;
    }
}
