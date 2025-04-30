package com.xhf.leetcode.plugin.review.backend.algorithm;

import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import com.xhf.leetcode.plugin.review.backend.card.QuestionCard;
import com.xhf.leetcode.plugin.review.backend.card.QuestionCardScheduler;
import com.xhf.leetcode.plugin.review.backend.card.QuestionFront;
import com.xhf.leetcode.plugin.review.backend.database.DatabaseAdapter;
import com.xhf.leetcode.plugin.utils.GsonUtils;

import com.xhf.leetcode.plugin.utils.LogUtils;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 文艺倾年
 */
public class AlgorithmApp {

    private static volatile AlgorithmApp instance;
    private final Map<Integer, QuestionCard> cards;
    private final QuestionCardScheduler cardScheduler;
    private final DatabaseAdapter databaseAdapter;
    private final Project project;

    /**
     * 实例化 AlgorithmApp。在这里执行启动应用程序所需的重要步骤
     */
    public AlgorithmApp(Project project) {
        // 实例化数据库
        this.project = project;
        this.databaseAdapter = new DatabaseAdapter(project);
        // 实例化 HashMap，用于存储从数据库加载的卡片
        this.cards = new HashMap<>();
        this.loadCards();
        this.cardScheduler = new QuestionCardScheduler(project, this.databaseAdapter);
    }

    /**
     * 从数据库表 "cards" 加载卡片。
     * 卡片将存储在类的 HashMap "cards" 中
     */
    public void loadCards() {
        this.databaseAdapter.getSqlite().query("SELECT * FROM cards", resultSet -> {
            try {
                while (resultSet.next()) {
                    String strFront = resultSet.getString("front");
                    QuestionFront questionFront = GsonUtils.fromJson(strFront, QuestionFront.class);
                    new QuestionCard(
                            resultSet.getInt("card_id"),
                            questionFront,
                            resultSet.getString("back"),
                            resultSet.getLong("created"),
                            project
                    )
                        .setNextReview(resultSet.getLong("next_repetition"));
                    ;
                    System.out.println("[Cards] Sucessfully loaded card " + resultSet.getInt("card_id"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 获取类的实例
     * @return 该类的实例
     */
    public static AlgorithmApp getInstance(Project project) {
        if (instance == null) {
            synchronized (AlgorithmApp.class) {
                if (instance == null) {
                    instance = new AlgorithmApp(project);
                }
            }
        }
        return instance;
    }

    /**
     * 获取存储从数据库加载的卡片的 HashMap
     * @return 存储卡片的 HashMap，可以通过 ID 获取卡片
     */
    public Map<Integer, QuestionCard> getCards() {
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
     * @return DatabaseAdapter
     */
    public DatabaseAdapter getDatabaseAdapter() {
        return this.databaseAdapter;
    }
}
