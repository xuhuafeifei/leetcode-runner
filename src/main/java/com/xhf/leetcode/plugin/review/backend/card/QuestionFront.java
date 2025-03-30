package com.xhf.leetcode.plugin.review.backend.card;

/**
 * @author 文艺倾年
 */
public class QuestionFront {
    private String title; // 题目名称
    private String difficulty; // 困难度【题目本身的困难度, 中等，简单，困难】
    private String status; // 状态
    private Double acRate; // 通过率
    /*
     用户评分, 具体可以参考{@link FSRSRating}
     */
    private Integer userRate;

    public QuestionFront() {
    }

    public QuestionFront(String title, String difficulty, String status, Double acRate, Integer userRate) {
        this.title = title;
        this.difficulty = difficulty;
        this.status = status;
        this.acRate = acRate;
        this.userRate = userRate;
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

    public Integer getUserRate() {
        return userRate;
    }

    public void setUserRate(Integer userRate) {
        this.userRate = userRate;
    }
}
