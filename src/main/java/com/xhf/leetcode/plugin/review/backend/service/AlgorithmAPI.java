package com.xhf.leetcode.plugin.review.backend.service;

import com.xhf.leetcode.plugin.review.backend.algorithm.AlgorithmApp;
import com.xhf.leetcode.plugin.review.backend.card.QuestionCard;
import com.xhf.leetcode.plugin.review.backend.card.QuestionCardReq;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author 文艺倾年
 */
public class AlgorithmAPI {
    // 单例
    private static AlgorithmApp instance = null;
    private AlgorithmAPI() {
    }

    public static AlgorithmApp getInstance() {
        if (instance == null) {
            new AlgorithmApp();
            instance = AlgorithmApp.getInstance();
        }
        return instance;
    }

    /**
     * 更新队列
     */
    public void updateQueue() {
        instance.getCardScheduler().queueDueCards();
    }

    /**
     * 创建卡片
     */
    public void createCard(QuestionCardReq questionCardReq) {
        QuestionCard.create(questionCardReq);
        updateQueue();
    }

    /**
     * 获取所有卡片
     */
    public List<QuestionCard> getAllCards() {
        Map<Integer, QuestionCard> cards = instance.getCards();
        return new ArrayList<>(cards.values());
    }

    /**
     * 删除卡片通过ID
     */
    public void deleteCardById(Integer id) {
        QuestionCard card = QuestionCard.getById(id);
        card.delete();
        updateQueue();
    }

    /**
     * 评分
     */
    public void rateCard(Integer index) {
        // 内存队列操作，无需更新
        if(index != -1) {
            instance.getCardScheduler().onRating(index);
        }
    }

    /**
     * 获取顶部卡片
     */
    public QuestionCard getTopCard() {
        QuestionCard topCard = instance.getCardScheduler().getTopCard();
        return topCard;
    }

}
