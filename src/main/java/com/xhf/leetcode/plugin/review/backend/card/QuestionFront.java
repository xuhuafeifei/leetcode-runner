package com.xhf.leetcode.plugin.review.backend.card;

/**
 * @author 文艺倾年
 */
public class QuestionFront {
    private String title; // 题目名称
    private String difficulty; // 困难度
    private String status; // 状态
    private Double acRate; // 通过率

    public QuestionFront() {
    }

    public QuestionFront(String title, String difficulty, String status, Double acRate) {
        this.title = title;
        this.difficulty = difficulty;
        this.status = status;
        this.acRate = acRate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getAcRate() {
        return acRate;
    }

    public void setAcRate(Double acRate) {
        this.acRate = acRate;
    }
}
