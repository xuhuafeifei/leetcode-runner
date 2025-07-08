package com.xhf.leetcode.plugin.model;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class TodayRecord {

    private String date;
    private String userStatus;
    private Question question;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(String userStatus) {
        this.userStatus = userStatus;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }
}
