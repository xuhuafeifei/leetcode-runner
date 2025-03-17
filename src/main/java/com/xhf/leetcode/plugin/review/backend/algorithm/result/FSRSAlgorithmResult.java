package com.xhf.leetcode.plugin.review.backend.algorithm.result;

import com.xhf.leetcode.plugin.review.backend.algorithm.constant.FSRSState;
import com.xhf.leetcode.plugin.review.backend.algorithm.constant.SchedulingCard;

/**
 * @author 文艺倾年
 */
public class FSRSAlgorithmResult extends AlgorithmResult {

    private long dueTime, lastReview;
    private float stability, difficulty;
    private int elapsedDays, repetitions;
    private FSRSState state;

    /**
     * 构造函数，用于初始化 FSRS 算法的结果
     * @param card 包含新计算属性的卡片
     */
    public FSRSAlgorithmResult(SchedulingCard card) {
        super(card.getScheduledDays());
        this.dueTime = card.getDueTime();
        this.lastReview = card.getLastReview();
        this.stability = card.getStability();
        this.difficulty = card.getDifficulty();
        this.elapsedDays = card.getElapsedDays();
        this.repetitions = card.getRepetitions();
        this.state = card.getState();
    }

    /**
     * 计算下一次复习的时间点（以毫秒为单位）
     * @return 下一次复习的时间点（毫秒）
     */
    public long getNextRepetitionTime() {
        return this.dueTime;
    }

    /**
     * 获取稳定性
     * @return 内容的稳定性
     */
    public float getStability() {
        return this.stability;
    }

    /**
     * 获取难度
     * @return 内容的难度
     */
    public float getDifficulty() {
        return this.difficulty;
    }

    /**
     * 获取自上次复习以来经过的天数
     * @return 经过的天数
     */
    public int getElapsedDays() {
        return this.elapsedDays;
    }

    /**
     * 获取复习次数
     * @return 复习次数
     */
    public int getRepetitions() {
        return this.repetitions;
    }

    /**
     * 获取上次复习的时间点（以毫秒为单位）
     * @return 上次复习的时间点（毫秒）
     */
    public long getLastReview() {
        return this.lastReview;
    }

    /**
     * 获取内容的状态
     * @return 内容的状态
     */
    public FSRSState getState() {
        return this.state;
    }
}