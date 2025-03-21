package com.xhf.leetcode.plugin.review.backend.test;

import com.xhf.leetcode.plugin.review.backend.algorithm.AlgorithmApp;
import com.xhf.leetcode.plugin.review.backend.card.QuestionCard;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author 文艺倾年
 */
public class APIDemo {
    static void testRating(int index) {
        // 内存队列操作，无需更新
        if(index != -1) {
            AlgorithmApp.getInstance().getCardScheduler().onRating(index);
        }
    }

    static void testDelete(Integer id) {
        QuestionCard card = QuestionCard.getById(id);
        card.delete();
        testUpdate();
    }

    static void testUpdate() {
        AlgorithmApp.getInstance().getCardScheduler().queueDueCards();
    }

    static void testCardCreate(Integer id, String front, String back) {
        QuestionCard.create(id, front, back);
        System.out.println("[Cards] 成功本地创建卡片 " + id + front + " " + back);
        testUpdate();
    }

    static List<QuestionCard> getCards() {
        Map<Integer, QuestionCard> cards = AlgorithmApp.getInstance().getCards();
        return new ArrayList<>(cards.values());
    }



    public static void main(String[] args) {
        // 1.实例化
        new AlgorithmApp();
        // 2.测试Card创建，TODO 创建时，数据库本身存在该数据，进行的做法。
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
        QuestionCard topCard = AlgorithmApp.getInstance().getCardScheduler().getTopCard();
        System.out.println("打分前：" + topCard.getId() + "-" + topCard.getFront());
        // 5.对卡片评分
        testRating(1);
        // 6.抽取顶部卡牌
        topCard = AlgorithmApp.getInstance().getCardScheduler().getTopCard();
        System.out.println("打分后：" + topCard.getId() + "-" + topCard.getFront());

        // 7.测试删除
//        Integer id = 1;
//        testDelete(id);
        topCard.delete(); // 这里删除的是数据库中的数据
        // 8.查看顶部卡牌
        topCard = AlgorithmApp.getInstance().getCardScheduler().getTopCard();
        System.out.println("删除后，未更新顶部卡牌：" + topCard.getId() + "-" + topCard.getFront());
        // 9.更新卡牌组，删除后一定要更新队列，然后更新GUI
        testUpdate(); // 这里是同步内存数据和数据库数据
        topCard = AlgorithmApp.getInstance().getCardScheduler().getTopCard();
        System.out.println("删除后，更新后顶部卡牌：" + topCard.getId() + "-" + topCard.getFront());
    }

}
