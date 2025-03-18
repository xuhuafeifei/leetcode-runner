package com.xhf.leetcode.plugin.review.backend.algorithm;

import com.xhf.leetcode.plugin.review.backend.algorithm.constant.FSRSRating;
import com.xhf.leetcode.plugin.review.backend.algorithm.constant.FSRSState;
import com.xhf.leetcode.plugin.review.backend.algorithm.result.FSRSAlgorithmResult;

/**
 * @author 文艺倾年
 */
public class AlgorithmAPI {

    private static AlgorithmAPI instance;

    /**
     * 使用实现的自由间隔重复调度算法的示例，结合Builder模式
     * @param rating 学习内容的评分
     * @param stability 学习内容的稳定性
     * @param difficulty 学习内容的难度
     * @param elapsedDays 自上次复习到下次复习的间隔天数
     * @param scheduledDays 计算出的下次复习天数
     * @param repetitions 到目前为止的复习次数
     * @param state 学习内容的状态
     * @param lastReview 上次复习的时间戳
     * @return 自由间隔重复调度算法的返回结果
     */
    public FSRSAlgorithmResult basicFSRS(FSRSRating rating, float stability, float difficulty, int elapsedDays, int scheduledDays, int repetitions, FSRSState state, long lastReview) {
        FSRSAlgorithm fsrs = FSRSAlgorithm.builder()
                .rating(rating)
                .stability(stability)
                .difficulty(difficulty)
                .elapsedDays(elapsedDays)
                .scheduledDays(scheduledDays)
                .repetitions(repetitions)
                .state(state)
                .lastReview(lastReview)
                .build();

        return fsrs.calc();
    }

    /**
     * 获取类的实例
     * @return 该类的实例
     */
    public static AlgorithmAPI getInstance() {
        return instance;
    }
}
