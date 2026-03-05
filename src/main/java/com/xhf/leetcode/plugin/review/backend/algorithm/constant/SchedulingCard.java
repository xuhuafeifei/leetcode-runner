package com.xhf.leetcode.plugin.review.backend.algorithm.constant;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author 文艺倾年
 */
public class SchedulingCard {

    // 核心时间参数（毫秒级）
    private long dueTime;       // 下次复习截止时间
    private long lastReview;    // 上次复习时间戳

    // 动态参数（算法核心指标）
    private float stability;     // 稳定性（0.1-∞）
    private float difficulty;    // 难度（1-10）

    // 时间跟踪参数
    private int elapsedDays;    // 已过天数（上次复习至今）
    private int scheduledDays;   // 计划间隔天数
    private int repetitions;     // 复习总次数

    // 状态管理
    private FSRSState state;      // 当前卡片状态
    private final HashMap<FSRSRating, SchedulingCard> ratingToCard;  // 评分等级映射表

    /**
     * 构造函数
     *
     * @param dueTime 下一次复习的时间点
     * @param stability 内容的稳定性
     * @param difficulty 内容的难度
     * @param elapsedDays 自上次复习以来经过的天数
     * @param scheduledDays 计算的下一次复习间隔天数
     * @param repetitions 复习次数
     * @param state 内容的状态
     * @param lastReview 上次复习的时间点
     */
    public SchedulingCard(long dueTime, float stability, float difficulty, int elapsedDays, int scheduledDays,
        int repetitions, FSRSState state, long lastReview) {
        this.dueTime = dueTime;
        this.stability = stability;
        this.difficulty = difficulty;
        this.elapsedDays = elapsedDays;
        this.scheduledDays = scheduledDays;
        this.repetitions = repetitions;
        this.state = state;
        this.lastReview = lastReview;

        this.ratingToCard = new HashMap<>();
    }

    /**
     * 根据评分更新卡片的状态
     */
    public void updateState() {
        if (this.state == FSRSState.NEW) {
            this.ratingToCard.get(FSRSRating.AGAIN).setState(FSRSState.LEARNING);
            this.ratingToCard.get(FSRSRating.HARD).setState(FSRSState.LEARNING);
            this.ratingToCard.get(FSRSRating.GOOD).setState(FSRSState.LEARNING);
            this.ratingToCard.get(FSRSRating.EASY).setState(FSRSState.REVIEW);

        } else if (this.state == FSRSState.LEARNING || this.state == FSRSState.RELEARNING) {
            this.ratingToCard.get(FSRSRating.AGAIN).setState(this.state);
            this.ratingToCard.get(FSRSRating.HARD).setState(FSRSState.REVIEW);
            this.ratingToCard.get(FSRSRating.GOOD).setState(FSRSState.REVIEW);
            this.ratingToCard.get(FSRSRating.EASY).setState(FSRSState.REVIEW);

        } else if (this.state == FSRSState.REVIEW) {
            this.ratingToCard.get(FSRSRating.AGAIN).setState(FSRSState.RELEARNING);
            this.ratingToCard.get(FSRSRating.HARD).setState(FSRSState.REVIEW);
            this.ratingToCard.get(FSRSRating.GOOD).setState(FSRSState.REVIEW);
            this.ratingToCard.get(FSRSRating.EASY).setState(FSRSState.REVIEW);
        }
    }

    /**
     * 计算下一次复习的时间
     *
     * @param hardInterval 硬度评分对应的间隔
     * @param goodInterval 好评评分对应的间隔
     * @param easyInterval 容易评分对应的间隔
     */
    public void schedule(float hardInterval, float goodInterval, float easyInterval) {
        this.scheduledDays = 0;

        this.ratingToCard.get(FSRSRating.HARD).setScheduledDays(Math.round(hardInterval));
        this.ratingToCard.get(FSRSRating.GOOD).setScheduledDays(Math.round(goodInterval));
        this.ratingToCard.get(FSRSRating.EASY).setScheduledDays(Math.round(easyInterval));

        this.ratingToCard.get(FSRSRating.AGAIN).setDueTime(this.dueTime + TimeUnit.MINUTES.toMillis(5));
        this.ratingToCard.get(FSRSRating.HARD).setDueTime(this.dueTime + TimeUnit.DAYS.toMillis((long) hardInterval));
        this.ratingToCard.get(FSRSRating.GOOD).setDueTime(this.dueTime + TimeUnit.DAYS.toMillis((long) goodInterval));
        this.ratingToCard.get(FSRSRating.EASY).setDueTime(this.dueTime + TimeUnit.DAYS.toMillis((long) easyInterval));
    }

    /**
     * 获取下一次复习的时间点
     *
     * @return 复习时间点（毫秒）
     */
    public long getDueTime() {
        return this.dueTime;
    }

    /**
     * 设置下一次复习的时间点
     *
     * @param dueTime 复习时间点（毫秒）
     */
    public void setDueTime(long dueTime) {
        this.dueTime = dueTime;
    }

    /**
     * 获取稳定性
     *
     * @return 内容的稳定性
     */
    public float getStability() {
        return this.stability;
    }

    /**
     * 设置稳定性
     *
     * @param stability 内容的稳定性
     */
    public void setStability(float stability) {
        this.stability = stability;
    }

    /**
     * 获取难度
     *
     * @return 内容的难度
     */
    public float getDifficulty() {
        return this.difficulty;
    }

    /**
     * 设置难度
     *
     * @param difficulty 内容的难度
     */
    public void setDifficulty(float difficulty) {
        this.difficulty = difficulty;
    }

    /**
     * 获取自上次复习以来经过的天数
     *
     * @return 经过的天数
     */
    public int getElapsedDays() {
        return this.elapsedDays;
    }

    /**
     * 设置自上次复习以来经过的天数
     *
     * @param elapsedDays 经过的天数
     */
    public void setElapsedDays(int elapsedDays) {
        this.elapsedDays = elapsedDays;
    }

    /**
     * 获取计算的下一次复习间隔天数
     *
     * @return 间隔天数
     */
    public int getScheduledDays() {
        return this.scheduledDays;
    }

    /**
     * 设置计算的下一次复习间隔天数
     *
     * @param scheduledDays 间隔天数
     */
    public void setScheduledDays(int scheduledDays) {
        this.scheduledDays = scheduledDays;
    }

    /**
     * 获取复习次数
     *
     * @return 复习次数
     */
    public int getRepetitions() {
        return this.repetitions;
    }

    /**
     * 设置复习次数
     *
     * @param repetitions 复习次数
     */
    public void setRepetitions(int repetitions) {
        this.repetitions = repetitions;
    }

    /**
     * 获取上次复习的时间点
     *
     * @return 上次复习时间点（毫秒）
     */
    public long getLastReview() {
        return this.lastReview;
    }

    /**
     * 设置上次复习的时间点
     *
     * @param lastReview 上次复习时间点（毫秒）
     */
    public void setLastReview(long lastReview) {
        this.lastReview = lastReview;
    }

    /**
     * 获取内容的状态
     *
     * @return 状态
     */
    public FSRSState getState() {
        return this.state;
    }

    /**
     * 设置内容的状态
     *
     * @param state 状态
     */
    public void setState(FSRSState state) {
        this.state = state;
    }

    /**
     * 获取评分对应的卡片映射
     *
     * @return 评分对应的卡片映射
     */
    public HashMap<FSRSRating, SchedulingCard> getRatingToCard() {
        return this.ratingToCard;
    }
}
