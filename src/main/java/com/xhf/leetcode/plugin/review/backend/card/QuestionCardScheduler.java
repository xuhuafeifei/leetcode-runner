package com.xhf.leetcode.plugin.review.backend.card;

import com.xhf.leetcode.plugin.review.backend.algorithm.FSRSAlgorithm;
import com.xhf.leetcode.plugin.review.backend.algorithm.constant.FSRSRating;
import com.xhf.leetcode.plugin.review.backend.algorithm.constant.FSRSState;
import com.xhf.leetcode.plugin.review.backend.algorithm.result.FSRSAlgorithmResult;
import com.xhf.leetcode.plugin.review.backend.util.Queue;

import java.util.UUID;

/**
 * @author 文艺倾年
 */
public class QuestionCardScheduler {

    private Queue<QuestionCard> queue;

    /**
     * 构造函数，用于初始化卡片调度器
     */
    public QuestionCardScheduler() {
        this.queue = new Queue<>();
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
        // 数据库中查询
        String card_uuid = "3d7157cc-6375-4432-923f-37cc14c9efa0";
        QuestionCard dueCard = QuestionCard.getByUUID(UUID.fromString(card_uuid));
        System.out.println("[CardScheduler] Due time for card " + dueCard.getUUID().toString());
        this.queue.enqueue(dueCard);
    }

    /**
     * 在复习过程中对卡片评分时，更新队列和卡片数据
     *
     * @param rating 卡片复习的质量评分（评分范围根据算法而定）
     */
    public void onRating(int rating) {
        QuestionCard ratedCard = this.queue.front();

        if (ratedCard != null) {
            try {
                // 1.根据UUID数据库查询卡片
                UUID uuid = ratedCard.getUUID();

                float stability = 0;
                float difficulty = 0;
                int repetitions = 0;
                int elapsed_days = 0;
                long lastReview = 0;
                int state = 1;
                // 2.使用算法计算
                FSRSAlgorithm algorithm = FSRSAlgorithm.builder()
                        .rating(FSRSRating.values()[rating])
                        .stability(stability)
                        .difficulty(difficulty)
                        .elapsedDays(elapsed_days)
                        .repetitions(repetitions)
                        .state(FSRSState.values()[state])
                        .lastReview(lastReview)
                        .build();
                FSRSAlgorithmResult result = algorithm.calc();
                System.out.println("[CardScheduler] 卡片 " + " 评分为 " + rating);
                // 3.数据库更新卡片数据
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.queue.dequeue();
        if (this.queue.isEmpty()) {
            this.queueDueCards();
        }
    }
}
