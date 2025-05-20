package com.xhf.leetcode.plugin.review.backend.card;

import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import com.xhf.leetcode.plugin.review.backend.algorithm.AlgorithmApp;
import com.xhf.leetcode.plugin.review.backend.algorithm.FSRSAlgorithm;
import com.xhf.leetcode.plugin.review.backend.algorithm.constant.FSRSRating;
import com.xhf.leetcode.plugin.review.backend.algorithm.constant.FSRSState;
import com.xhf.leetcode.plugin.review.backend.algorithm.result.FSRSAlgorithmResult;
import com.xhf.leetcode.plugin.review.backend.database.DatabaseAdapter;
import com.xhf.leetcode.plugin.utils.LogUtils;
import org.jetbrains.annotations.Nullable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 初始化完成后必须调用init
 * @author 文艺倾年
 */
public class QuestionCardScheduler {

    private final Project project;
    private final DatabaseAdapter databaseAdapter;
    private Queue<QuestionCard> queue;

    /**
     * 构造函数，用于初始化卡片调度器
     */
    private QuestionCardScheduler(Project project) {
        this.queue = new Queue<>();
        this.project = project;
        this.databaseAdapter = DatabaseAdapter.getInstance(project);
    }

    public void init() {
        queueDueCards();
    }

    // 单例
    private static volatile QuestionCardScheduler instance = null;

    public static QuestionCardScheduler getInstance(Project project) {
        if (instance == null) {
            synchronized (QuestionCardScheduler.class) {
                if (instance == null) {
                    instance = new QuestionCardScheduler(project);
                }
            }
        }
        return instance;
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
        this.databaseAdapter.getSqlite().syncQuery("SELECT * FROM cards" + " WHERE next_repetition <= " + System.currentTimeMillis(), resultSet -> {
            try {
                while (resultSet.next()) {
                    QuestionCard dueCard = AlgorithmApp.getInstance(project).getCards().get(resultSet.getInt("card_id"));
                    LogUtils.simpleDebug("[CardScheduler] Due time for card " + dueCard.getId());
                    this.queue.enqueue(dueCard);
                }
            } catch (SQLException e) {
                LogUtils.warn(DebugUtils.getStackTraceAsString(e));
            }
        });
    }

    public @Nullable QuestionCard getTopCard() {
        return this.queue.front();
    }

    /**
     * 在复习过程中对卡片评分时，更新队列和卡片数据
     * <p>
     * 每次评分都会作用到队列top部位的卡牌
     * <p>
     * rating 为卡片复习的质量评分（评分范围根据算法而定, 其内容详见FSRSRating枚举）
     *
     * @param rating 卡片复习的质量评分（评分范围根据算法而定）
     * @param back   卡片复习的反馈信息, 如果为null, 则不更新数据库
     */
    public void onRating(int rating, @Nullable String back) {
        QuestionCard ratedCard = this.queue.front();
        onRatingById(ratedCard.getId(), rating, back);
    }

    public void onRatingById(Integer cardId, Integer rating, String back) {
        // 1.查询数据库
        String query = "SELECT * FROM cards" + " WHERE card_id = ?";
        LogUtils.simpleDebug("[CardScheduler] 查询卡片 SQL: " + cardId);

        try (PreparedStatement ps = this.databaseAdapter.getSqlite().prepare(query)) {
            ps.setInt(1, cardId);
            ResultSet rs = ps.executeQuery();
            rs.next();

            // 2.使用算法计算
            FSRSAlgorithm algorithm = FSRSAlgorithm.builder()
                    .rating(FSRSRating.values()[rating])
                    .stability(rs.getFloat("stability"))
                    .difficulty(rs.getFloat("difficulty"))
                    .elapsedDays(rs.getInt("elapsed_days"))
                    .repetitions(rs.getInt("repetitions"))
                    .state(FSRSState.values()[(rs.getInt("state"))])
                    .lastReview(rs.getLong("last_review"))
                    .build();
            FSRSAlgorithmResult result = algorithm.calc();

            // 3.更新数据库
            String update = String.format(
                    "UPDATE cards SET " +
                            "repetitions = %d, " +
                            "difficulty = %.2f, " +
                            "stability = %.2f, " +
                            "elapsed_days = %d, " +
                            "state = %d, " +
                            "day_interval = %d, " +
                            "next_repetition = %d, " +
                            "last_review = %d, " +
                            "back = '%s' " +
                            "WHERE card_id = %d",
                    result.getRepetitions(),
                    result.getDifficulty(),
                    result.getStability(),
                    result.getElapsedDays(),
                    result.getState().toInt(),
                    result.getInterval(),
                    result.getNextRepetitionTime(),
                    result.getLastReview(),
                    back == null ? rs.getString("back") : back.replace("'", "''"), // 防止 SQL 注入问题
                    cardId
            );

            LogUtils.simpleDebug("[CardScheduler] 更新卡片 SQL: " + update);
            this.databaseAdapter.getSqlite().update(update);

            LogUtils.simpleDebug("[CardScheduler] 卡片 " + " 评分为 " + rating);
        } catch (Exception e) {
            LogUtils.warn(DebugUtils.getStackTraceAsString(e));
        }
        this.queue.dequeue();
        if (this.queue.isEmpty()) {
            this.queueDueCards();
        }
    }
}
