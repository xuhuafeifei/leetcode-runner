package com.xhf.leetcode.plugin.model;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class CalendarSubmitRecord {
    /**
     * 连续提交天数
     */
    private int dailySubmitStreakCount;
    /**
     * 每日一题连胜
     */
    private int dailyQuestionStreakCount;
    /**
     * 本月解决问题
     */
    private int monthlyAcCount;

    public static CalendarSubmitRecord getDefault() {
        CalendarSubmitRecord calendarSubmitRecord = new CalendarSubmitRecord();
        calendarSubmitRecord.setDailySubmitStreakCount(0);
        calendarSubmitRecord.setDailyQuestionStreakCount(0);
        calendarSubmitRecord.setMonthlyAcCount(0);
        return calendarSubmitRecord;
    }

    public int getDailySubmitStreakCount() {
        return dailySubmitStreakCount;
    }

    public void setDailySubmitStreakCount(int dailySubmitStreakCount) {
        this.dailySubmitStreakCount = dailySubmitStreakCount;
    }

    public int getDailyQuestionStreakCount() {
        return dailyQuestionStreakCount;
    }

    public void setDailyQuestionStreakCount(int dailyQuestionStreakCount) {
        this.dailyQuestionStreakCount = dailyQuestionStreakCount;
    }

    public int getMonthlyAcCount() {
        return monthlyAcCount;
    }

    public void setMonthlyAcCount(int monthlyAcCount) {
        this.monthlyAcCount = monthlyAcCount;
    }
}
