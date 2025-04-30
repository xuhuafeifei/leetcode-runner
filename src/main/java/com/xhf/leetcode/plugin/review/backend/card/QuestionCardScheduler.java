package com.xhf.leetcode.plugin.review.backend.card;

import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.review.backend.algorithm.FSRSAlgorithm;
import com.xhf.leetcode.plugin.review.backend.algorithm.constant.FSRSRating;
import com.xhf.leetcode.plugin.review.backend.algorithm.constant.FSRSState;
import com.xhf.leetcode.plugin.review.backend.algorithm.result.FSRSAlgorithmResult;
import com.xhf.leetcode.plugin.review.backend.algorithm.AlgorithmApp;
import com.xhf.leetcode.plugin.review.backend.database.DatabaseAdapter;
import org.jetbrains.annotations.Nullable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author 文艺倾年
 */
public class QuestionCardScheduler {

    private final Project project;
    private final DatabaseAdapter databaseAdapter;
    private Queue<QuestionCard> queue;

    /**
     * 构造函数，用于初始化卡片调度器
     */
    public QuestionCardScheduler(Project project, DatabaseAdapter databaseAdapter) {
        this.queue = new Queue<>();
        this.project = project;
        this.databaseAdapter = databaseAdapter;
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
        this.databaseAdapter.getSqlite().syncQuery("SELECT * FROM cards" + " WHERE next_repetition <= " + System.currentTimeMillis(), resultSet -> {
            try {
                while (resultSet.next()) {
                    QuestionCard dueCard = AlgorithmApp.getInstance(project).getCards().get(resultSet.getInt("card_id"));
                    System.out.println("[CardScheduler] Due time for card " + dueCard.getId());
                    this.queue.enqueue(dueCard);
                }
            } catch (SQLException e) {
                e.printStackTrace();
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
     */
    public void onRating(int rating) {
        QuestionCard ratedCard = this.queue.front();

        if (ratedCard != null) {
            try {
                // 1.查询数据库
                PreparedStatement ps = this.databaseAdapter.getSqlite().prepare("SELECT * FROM cards" + " WHERE card_id = ?");
                ps.setInt(1, ratedCard.getId());
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
                PreparedStatement db = this.databaseAdapter.getSqlite().prepare("UPDATE cards SET repetitions = ?, difficulty = ?, stability = ?, elapsed_days = ?, state = ?, day_interval = ?, next_repetition = ?, last_review = ? WHERE card_id = ?");
                db.setLong(1, result.getRepetitions());
                db.setFloat(2, result.getDifficulty());
                db.setFloat(3, result.getStability());
                db.setInt(4, result.getElapsedDays());
                db.setInt(5, result.getState().toInt());
                db.setInt(6, result.getInterval());
                db.setLong(7, result.getNextRepetitionTime());
                db.setLong(8, result.getLastReview());
                db.setInt(9, ratedCard.getId());
                db.execute();

                System.out.println("[CardScheduler] 卡片 " + " 评分为 " + rating);
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
