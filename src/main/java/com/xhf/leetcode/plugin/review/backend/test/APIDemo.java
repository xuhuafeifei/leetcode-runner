package com.xhf.leetcode.plugin.review.backend.test;

import com.xhf.leetcode.plugin.review.backend.card.QuestionCard;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        TestAlgorithmApp.getInstance().getCardScheduler().queueDueCards();
    }

    static void testUpdate() {
        TestAlgorithmApp.getInstance().getCardScheduler().queueDueCards();
    }

    static void testCardCreate(Integer id, String front, String back) {
        QuestionCard.create(id, front, back);
        System.out.println("[Cards] 成功本地创建卡片 " + id + front + " " + back);
        TestAlgorithmApp.getInstance().getCardScheduler().queueDueCards();
    }

    static List<QuestionCard> getCards() {
        Map<Integer, QuestionCard> cards = TestAlgorithmApp.getInstance().getCards();
        return new ArrayList<>(cards.values());
    }



    public static void main(String[] args) {
        // 1.实例化
        new TestAlgorithmApp();
        // 2.测试Card创建
        for(int i = 0; i < 10; ++ i) {
            String front = "1" + i;
            String back = "2" + i;
            testCardCreate(i, front, back);
        }
        // 3.查询全部卡牌
        List<QuestionCard> cards = getCards();
        for (QuestionCard card : cards) {
            System.out.println("card:" + card.getId() + "-" + card.getFront() + "-" + card.getBack() + "-" + card.getCreated());
        }
        // 4.抽取顶部卡牌
        QuestionCard topCard = TestAlgorithmApp.getInstance().getCardScheduler().getTopCard();
        System.out.println("打分前：" + topCard.getId());
//        // 3.更新卡牌组
//        testUpdate();
//        // 4.对卡片评分
        testRating(0);
        // 4.抽取顶部卡牌
        topCard = TestAlgorithmApp.getInstance().getCardScheduler().getTopCard();
        System.out.println("打分后：" + topCard.getId());
    }

}
