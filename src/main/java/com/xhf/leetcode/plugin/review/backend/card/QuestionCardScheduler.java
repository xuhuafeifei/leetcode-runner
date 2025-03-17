package com.xhf.leetcode.plugin.review.backend.card;

import com.xhf.leetcode.plugin.review.backend.algorithm.FSRSAlgorithm;
import com.xhf.leetcode.plugin.review.backend.algorithm.constant.FSRSRating;
import com.xhf.leetcode.plugin.review.backend.algorithm.constant.FSRSState;
import com.xhf.leetcode.plugin.review.backend.algorithm.result.FSRSAlgorithmResult;
import com.xhf.leetcode.plugin.review.backend.util.AlgorithmType;
import com.xhf.leetcode.plugin.review.backend.util.Queue;

/**
 * @author 文艺倾年
 */
public class QuestionCardScheduler {

    private Queue<QuestionCard> queue;
    private AlgorithmType type;

    /**
     * 构造函数，用于初始化卡片调度器
     * @param type 调度器使用的算法类型
     */
    public QuestionCardScheduler(AlgorithmType type) {
        this.queue = new Queue<>();
        this.type = type;

        this.queueDueCards();
    }

    /**
     * 将过期的卡片放入队列中
     */
    public void queueDueCards() {
        // 清空队列
        while (!this.queue.isEmpty()) {
            this.queue.dequeue();
        }
    }

    /**
     * 在复习过程中对卡片评分时，更新队列和卡片数据
     * @param rating 卡片复习的质量评分（评分范围根据算法而定）
     */
    public void onRating(int rating) {
        QuestionCard ratedCard = this.queue.front();

        if (ratedCard != null) {
            try {
                if (this.type == AlgorithmType.FREE_SPACED_REPETITION_SCHEDULER) {
                    float stability = 0;
                    float difficulty = 0;
                    int repetitions = 0;
                    int elapsed_days = 0;
                    long lastReview = 0;
                    String state = "";
                    FSRSAlgorithm algorithm = FSRSAlgorithm.builder()
                            .rating(FSRSRating.values()[rating])
                            .stability(stability)
                            .difficulty(difficulty)
                            .elapsedDays(elapsed_days)
                            .repetitions(repetitions)
                            .state(FSRSState.valueOf(state))
                            .lastReview(lastReview)
                            .build();

                    FSRSAlgorithmResult result = algorithm.calc();

                }
                System.out.println("[CardScheduler] 卡片 " + " 评分为 " + rating + " (算法: " + this.type + ")");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        this.queue.dequeue();
        if (this.queue.isEmpty()) {
            this.queueDueCards();
        }
    }

    /**
     * 获取此调度器的类型
     * @return 调度器的类型
     */
    public AlgorithmType getType() {
        return this.type;
    }
}
