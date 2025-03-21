package com.xhf.leetcode.plugin.review.backend.algorithm;

import com.xhf.leetcode.plugin.review.backend.algorithm.constant.FSRSRating;
import com.xhf.leetcode.plugin.review.backend.algorithm.constant.FSRSState;
import com.xhf.leetcode.plugin.review.backend.algorithm.constant.SchedulingCard;
import com.xhf.leetcode.plugin.review.backend.algorithm.result.FSRSAlgorithmResult;

import java.util.concurrent.TimeUnit;

/**
 * @author 文艺倾年
 */
public class FSRSAlgorithm{
    // 算法的标准值
    private final float REQUEST_RETENTION = 0.9F;
    private final int MAXIMUM_INTERVAL = 36500;
    private final float EASY_BONUS = 1.3F;
    private final float HARD_FACTOR = 1.2F;
    private final float[] WEIGHTS = new float[]{1F, 1F, 5F, -1F, -1F, 0.1F, 1.5F, -0.2F, 0.8F, 2, -0.2F, 0.2F, 1F};

    private FSRSRating rating;
    private SchedulingCard card;
    private long lastReview;
    private float stability, difficulty;
    private int elapsedDays, scheduledDays, repetitions;
    private FSRSState state;

    /**
     * 使用构建器模式创建FSRSAlgorithm对象
     * @return FSRSAlgorithmBuilder对象，用于构建FSRSAlgorithm实例
     */
    public static FSRSAlgorithmBuilder builder() {
        return new FSRSAlgorithmBuilder();
    }

    /**
     * 使用构建器模式创建FSRSAlgorithm对象
     * @param builder 包含所有必要参数的FSRSAlgorithmBuilder对象
     */
    public FSRSAlgorithm(FSRSAlgorithmBuilder builder) {
        this.rating = builder.rating;
        this.lastReview = builder.lastReview;
        this.stability = builder.stability;
        this.difficulty = builder.difficulty;
        this.elapsedDays = builder.elapsedDays;
        this.scheduledDays = builder.scheduledDays;
        this.repetitions = builder.repetitions;
        this.state = builder.state;
    }

    /**
     * 计算下一个复习时间、稳定性、难度等参数
     * @return 包含计算结果的FSRSAlgorithmResult对象
     */
    public FSRSAlgorithmResult calc() {
        this.card = new SchedulingCard(System.currentTimeMillis(), this.stability, this.difficulty, this.elapsedDays, this.scheduledDays, this.repetitions, this.state, this.lastReview);
        // 为每个评分枚举创建一张卡片
        for (FSRSRating rating : FSRSRating.values()) {
            this.card.getRatingToCard().put(rating, new SchedulingCard(System.currentTimeMillis(), this.stability, this.difficulty, this.elapsedDays, this.scheduledDays, this.repetitions, FSRSState.NEW, this.lastReview));
        }

        if (this.card.getState() == FSRSState.NEW) {
            this.card.setElapsedDays(0);
        } else {
            this.card.setElapsedDays((int) (TimeUnit.MILLISECONDS.toDays(this.card.getDueTime() - this.card.getLastReview())));
        }

        this.card.updateState();

        int easyInterval, hardInterval, goodInterval;

        // 处理新卡片
        if (this.card.getState() == FSRSState.NEW) {
            this.init();

            this.card.getRatingToCard().get(FSRSRating.AGAIN).setDueTime(this.card.getDueTime() + TimeUnit.MINUTES.toMillis(1));
            this.card.getRatingToCard().get(FSRSRating.HARD).setDueTime(this.card.getDueTime() + TimeUnit.MINUTES.toMillis(15));
            this.card.getRatingToCard().get(FSRSRating.GOOD).setDueTime(this.card.getDueTime() + TimeUnit.MINUTES.toMillis(10));

            easyInterval = this.nextInterval(this.card.getRatingToCard().get(FSRSRating.EASY).getStability() * EASY_BONUS);
            this.card.getRatingToCard().get(FSRSRating.EASY).setScheduledDays(easyInterval);
            this.card.getRatingToCard().get(FSRSRating.EASY).setDueTime(this.card.getDueTime() + TimeUnit.DAYS.toMillis(easyInterval));

            // 处理正在学习或重新学习的卡片
        } else if (this.card.getState() == FSRSState.LEARNING || this.card.getState() == FSRSState.RELEARNING) {
            hardInterval = this.nextInterval(this.card.getRatingToCard().get(FSRSRating.HARD).getStability());
            goodInterval = Math.max(this.nextInterval(this.card.getRatingToCard().get(FSRSRating.GOOD).getStability()), hardInterval + 1);
            easyInterval = Math.max(this.nextInterval(this.card.getRatingToCard().get(FSRSRating.EASY).getStability() * EASY_BONUS), goodInterval + 1);
            this.card.schedule(hardInterval, goodInterval, easyInterval);

            // 处理需要复习的卡片
        } else if (this.card.getState() == FSRSState.REVIEW) {
            int interval = this.card.getElapsedDays();
            float lastDifficulty = this.card.getDifficulty();
            float lastStability = this.card.getStability();

            float retrievability = (float) Math.exp(Math.log(0.9) * interval / lastStability);
            this.next(lastDifficulty, lastStability, retrievability);

            hardInterval = this.nextInterval(lastStability * HARD_FACTOR);
            goodInterval = nextInterval(this.card.getRatingToCard().get(FSRSRating.GOOD).getStability());
            hardInterval = Math.min(hardInterval, goodInterval);
            goodInterval = Math.max(goodInterval, hardInterval + 1);
            easyInterval = Math.max(this.nextInterval(this.card.getRatingToCard().get(FSRSRating.EASY).getStability() * HARD_FACTOR), goodInterval + 1);
            this.card.schedule(hardInterval, goodInterval, easyInterval);
        }

        SchedulingCard newCard = this.card.getRatingToCard().get(this.rating);
        newCard.setLastReview(System.currentTimeMillis());
        newCard.setRepetitions(this.card.getRepetitions() + 1);
        return new FSRSAlgorithmResult(newCard);
    }

    /**
     * 初始化所有卡片的难度和稳定性
     */
    private void init() {
        for (FSRSRating rating : FSRSRating.values()) {
            this.card.getRatingToCard().get(rating).setDifficulty(this.initDifficulty(rating.toInt()));
            this.card.getRatingToCard().get(rating).setStability(this.initStability(rating.toInt()));
        }
    }

    /**
     * 初始化难度
     * @param retrievability 可检索性
     * @return 难度
     */
    private float initDifficulty(int retrievability) {
        return Math.min(Math.max(WEIGHTS[2] + WEIGHTS[3] * (retrievability - 2), 1), 10);
    }

    /**
     * 初始化稳定性
     * @param retrievability 可检索性
     * @return 稳定性
     */
    private float initStability(int retrievability) {
        return Math.max(WEIGHTS[0] + WEIGHTS[1] * retrievability, 0.1F);
    }

    /**
     * 计算下一次复习的难度和稳定性
     * @param lastDifficulty 上一次的难度
     * @param lastStability 上一次的稳定性
     * @param retrievability 可检索性
     */
    private void next(float lastDifficulty, float lastStability, float retrievability) {
        for (FSRSRating rating : FSRSRating.values()) {
            this.card.getRatingToCard().get(rating).setDifficulty(this.nextDifficulty(lastDifficulty, rating.toInt()));
            if (rating == FSRSRating.AGAIN) {
                this.card.getRatingToCard().get(rating).setStability(this.nextForgetStability(this.card.getRatingToCard().get(rating).getDifficulty(), lastStability, retrievability));
            } else {
                this.card.getRatingToCard().get(rating).setStability(this.nextRecallStability(this.card.getRatingToCard().get(rating).getDifficulty(), lastStability, retrievability));
            }
        }
    }

    /**
     * 计算下一次复习的时间间隔
     * @param stability 稳定性
     * @return 时间间隔（天）
     */
    private int nextInterval(float stability) {
        double interval = stability * Math.log(REQUEST_RETENTION) / Math.log(0.9);
        return (int) Math.min(Math.max(Math.round(interval), 1), MAXIMUM_INTERVAL);
    }

    /**
     * 计算下一次复习的难度
     * @param difficulty 上一次的难度
     * @param retrievability 可检索性
     * @return 下一次的难度
     */
    private float nextDifficulty(float difficulty, int retrievability) {
        float next = difficulty + WEIGHTS[4] * (retrievability - 2);
        return Math.min(Math.max(this.meanReversion(WEIGHTS[2], next), 1), 100);
    }

    /**
     * 计算下一次复习的稳定性（回忆情况）
     * @param difficulty 难度
     * @param stability 上一次的稳定性
     * @param retrievability 可检索性
     * @return 下一次的稳定性
     */
    private float nextRecallStability(float difficulty, float stability, float retrievability) {
        return (float) (stability * (1 + Math.exp(WEIGHTS[6]) *
                (11 - difficulty) *
                Math.pow(stability, WEIGHTS[7]) *
                (Math.exp((1 - retrievability) * WEIGHTS[8]) - 1)));
    }

    /**
     * 计算下一次复习的稳定性（遗忘情况）
     * @param difficulty 难度
     * @param stability 上一次的稳定性
     * @param retrievability 可检索性
     * @return 下一次的稳定性
     */
    private float nextForgetStability(float difficulty, float stability, float retrievability) {
        return (float) (WEIGHTS[9] * Math.pow(difficulty, WEIGHTS[10]) * Math.pow(stability, WEIGHTS[11]) * Math.exp((1 - retrievability) * WEIGHTS[12]));
    }

    private float meanReversion(float init, float current) {
        return WEIGHTS[5] * init + (1 - WEIGHTS[5]) * current;
    }

    /**
     * 使用构建器模式创建FSRSAlgorithm对象
     */
    public static class FSRSAlgorithmBuilder {

        private FSRSRating rating = FSRSRating.AGAIN;
        private long lastReview = 0;
        private float stability = 0, difficulty = 0;
        private int elapsedDays = 0, scheduledDays = 0, repetitions = 0;
        private FSRSState state = FSRSState.NEW;

        /**
         * 设置卡片的评分
         * @param rating 评分
         * @return FSRSAlgorithmBuilder对象
         */
        public FSRSAlgorithmBuilder rating(FSRSRating rating) {
            this.rating = rating;
            return this;
        }

        /**
         * 设置上次复习的时间
         * @param lastReview 上次复习的时间（毫秒）
         * @return FSRSAlgorithmBuilder对象
         */
        public FSRSAlgorithmBuilder lastReview(long lastReview) {
            this.lastReview = lastReview;
            return this;
        }

        /**
         * 设置稳定性
         * @param stability 稳定性
         * @return FSRSAlgorithmBuilder对象
         */
        public FSRSAlgorithmBuilder stability(float stability) {
            this.stability = stability;
            return this;
        }

        /**
         * 设置难度
         * @param difficulty 难度
         * @return FSRSAlgorithmBuilder对象
         */
        public FSRSAlgorithmBuilder difficulty(float difficulty) {
            this.difficulty = difficulty;
            return this;
        }

        /**
         * 设置上次复习与下次复习之间经过的天数
         * @param elapsedDays 经过的天数
         * @return FSRSAlgorithmBuilder对象
         */
        public FSRSAlgorithmBuilder elapsedDays(int elapsedDays) {
            this.elapsedDays = elapsedDays;
            return this;
        }

        /**
         * 设置下次复习的计划天数
         * @param scheduledDays 计划的天数
         * @return FSRSAlgorithmBuilder对象
         */
        public FSRSAlgorithmBuilder scheduledDays(int scheduledDays) {
            this.scheduledDays = scheduledDays;
            return this;
        }

        /**
         * 设置复习次数
         * @param repetitions 复习次数
         * @return FSRSAlgorithmBuilder对象
         */
        public FSRSAlgorithmBuilder repetitions(int repetitions) {
            this.repetitions = repetitions;
            return this;
        }

        /**
         * 设置卡片的状态
         * @param state 卡片状态
         * @return FSRSAlgorithmBuilder对象
         */
        public FSRSAlgorithmBuilder state(FSRSState state) {
            this.state = state;
            return this;
        }

        /**
         * 构建FSRSAlgorithm对象
         * @return FSRSAlgorithm对象
         */
        public FSRSAlgorithm build() {
            return new FSRSAlgorithm(this);
        }
    }
}
