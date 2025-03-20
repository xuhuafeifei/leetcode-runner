package com.xhf.leetcode.plugin.review.backend.test;

import com.xhf.leetcode.plugin.review.backend.card.QuestionCard;
import com.xhf.leetcode.plugin.review.backend.card.QuestionCardScheduler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author 文艺倾年
 */
public class TestAlgorithmApp {

    private static TestAlgorithmApp instance;
    private Map<UUID, QuestionCard> cards;
    private QuestionCardScheduler cardScheduler;

    /**
     * 实例化 SpacedRepetitionApp。在这里执行启动应用程序所需的重要步骤
     */
    protected TestAlgorithmApp() {
        instance = this;
        // 实例化 HashMap，用于存储从数据库加载的卡片
        this.cards = new HashMap<>();
        this.loadCards();
        this.cardScheduler = new QuestionCardScheduler();
    }

    /**
     * 从数据库表 "cards" 加载卡片。
     * 卡片将存储在类的 HashMap "cards" 中
     */
    public void loadCards() {
        // TODO 本地加载题目卡片信息
        for(int i = 0; i < 10; i++) {
            String card_uuid = UUID.randomUUID().toString();
            String front = "1" + i;
            String back = "1" + i;
            Long created = 1L;
            new QuestionCard(UUID.fromString(card_uuid), front, back, created);
            System.out.println("[Cards] Sucessfully loaded card " + card_uuid);
        }
        String card_uuid = "3d7157cc-6375-4432-923f-37cc14c9efa0";
        String front = "1";
        String back = "1";
        Long created = 1L;
        new QuestionCard(UUID.fromString(card_uuid), front, back, created);
    }

    /**
     * 获取类的实例
     * @return 该类的实例
     */
    public static TestAlgorithmApp getInstance() {
        return instance;
    }

    /**
     * 获取存储从数据库加载的卡片的 HashMap
     * @return 存储卡片的 HashMap，可以通过 UUID 获取卡片
     */
    public Map<UUID, QuestionCard> getCards() {
        return this.cards;
    }

    /**
     * 获取管理待复习卡片的 CardScheduler
     * @return CardScheduler
     */
    public QuestionCardScheduler getCardScheduler() {
        return this.cardScheduler;
    }
}
