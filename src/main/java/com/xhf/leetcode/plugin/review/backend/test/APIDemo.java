package com.xhf.leetcode.plugin.review.backend.test;

import com.xhf.leetcode.plugin.review.backend.card.QuestionCardScheduler;
import com.xhf.leetcode.plugin.review.backend.util.AlgorithmType;

/**
 * @author 文艺倾年
 */
public class APIDemo {
    void testRating(int index) {
        if(index != -1) {
            TestAlgorithmApp.getInstance().getCardScheduler().onRating(index);
        }
    }

    void testDelete() {
        // 更新GUI界面
        //
        TestAlgorithmApp.getInstance().getCardScheduler().queueDueCards();
    }

    void testUpdate() {
        // 更新GUI界面
        //
        TestAlgorithmApp.getInstance().getCardScheduler().queueDueCards();
    }

    void testSelectCardScheduler() {
        AlgorithmType type = AlgorithmType.valueOf("");
        if (type != TestAlgorithmApp.getInstance().getCardScheduler().getType()) {
            TestAlgorithmApp.getInstance().setCardScheduler(new QuestionCardScheduler(type));
        }
    }
}
