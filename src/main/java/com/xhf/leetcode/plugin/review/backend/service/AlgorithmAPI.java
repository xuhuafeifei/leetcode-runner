package com.xhf.leetcode.plugin.review.backend.service;

import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.review.backend.algorithm.AlgorithmApp;
import com.xhf.leetcode.plugin.review.backend.card.QuestionCard;
import com.xhf.leetcode.plugin.review.backend.card.QuestionCardReq;
import com.xhf.leetcode.plugin.review.backend.card.QuestionCardScheduler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author 文艺倾年
 */
public class AlgorithmAPI {
    private AlgorithmApp app;
    private QuestionCardScheduler cardScheduler;

    /**
     * 一定是先初始化app, 再初始化cardScheduler
     * @param project project
     */
    public AlgorithmAPI(Project project) {
        app = AlgorithmApp.getInstance(project);
        cardScheduler = QuestionCardScheduler.getInstance(project);
        init();
    }

    private void init() {
        app.init();
        cardScheduler.init();
    }

    /**
     * 更新队列
     */
    public void updateQueue() {
        this.cardScheduler.queueDueCards();
        app.loadCards();
    }

    /**
     * 创建卡片
     */
    public void createCard(QuestionCardReq questionCardReq) {
        app.create(questionCardReq);
        updateQueue();
    }

    /**
     * 获取所有卡片
     */
    public List<QuestionCard> getAllCards() {
        Map<Integer, QuestionCard> cards = app.getCards();
        return new ArrayList<>(cards.values());
    }

    private QuestionCard getById(Integer id) {
        return app.getCards().get(id);
    }

    /**
     * 删除卡片通过ID
     */
    public void deleteCardById(Integer id) {
        QuestionCard card = getById(id);
        app.delete(card);
        updateQueue();
    }

    public void updateCardBack(Integer id, String back) {
        QuestionCard card = getById(id);
        app.update(card, back);
        updateQueue();
    }

    /**
     * 评分
     */
    public void rateCard(Integer index, String back) {
        // 内存队列操作，无需更新
        if(index != -1) {
            this.cardScheduler.onRating(index, back);
            updateQueue();
        }
    }

    /**
     * 获取顶部卡片
     */
    public @Nullable QuestionCard getTopCard() {
        return this.cardScheduler.getTopCard();
    }

}
