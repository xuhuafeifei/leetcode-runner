package com.xhf.leetcode.plugin.review.backend.test;

import com.xhf.leetcode.plugin.review.backend.card.QuestionCard;

/**
 * @author 文艺倾年
 */
public class APIDemo {
    static void testRating(int index) {
        if(index != -1) {
            TestAlgorithmApp.getInstance().getCardScheduler().onRating(index);
        }
    }

    static void testDelete() {
        // 更新GUI界面
        //
        TestAlgorithmApp.getInstance().getCardScheduler().queueDueCards();
    }

    static void testUpdate() {
        // 更新GUI界面
        //
        TestAlgorithmApp.getInstance().getCardScheduler().queueDueCards();
    }

    static void testCardCreate(String front, String back) {
        QuestionCard.create(front, back);
        System.out.println("[Cards] 成功本地创建卡片 " + front + " " + back);
        TestAlgorithmApp.getInstance().getCardScheduler().queueDueCards();
    }


    public static void main(String[] args) {
        // 1.实例化
        new TestAlgorithmApp();
        // 2.测试Card创建
        String front = "1";
        String back = "2";
        testCardCreate(front, back);
        // 3.更新卡牌组
        testUpdate();
        // 4.对卡片评分
        testRating(0);
    }
}
