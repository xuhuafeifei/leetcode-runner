package com.xhf.leetcode.plugin.review.backend.test;

import com.xhf.leetcode.plugin.review.backend.card.QuestionCard;
import com.xhf.leetcode.plugin.review.backend.card.QuestionCardScheduler;
import com.xhf.leetcode.plugin.review.backend.util.AlgorithmType;

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
        // 默认的 CardScheduler 使用 FSRS 算法，该算法默认在 LearnView 的 ComboBox 中被选中
        this.cardScheduler = new QuestionCardScheduler(AlgorithmType.FREE_SPACED_REPETITION_SCHEDULER);
    }

    /**
     * 从数据库表 "cards" 加载卡片。
     * 卡片将存储在类的 HashMap "cards" 中
     */
    public void loadCards() {
        // TODO 本地加载题目卡片信息
        // new QuestionCard();
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

    /**
     * 设置管理待复习卡片的 CardScheduler（例如，当算法更改时，Scheduler 需要相应地调整以适应新算法）
     * @param cardScheduler 新的 CardScheduler
     */
    public void setCardScheduler(QuestionCardScheduler cardScheduler) {
        this.cardScheduler = cardScheduler;
    }
}
