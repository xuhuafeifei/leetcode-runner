package com.xhf.leetcode.plugin.review.backend.algorithm;

import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import com.xhf.leetcode.plugin.review.backend.algorithm.constant.FSRSRating;
import com.xhf.leetcode.plugin.review.backend.algorithm.constant.FSRSState;
import com.xhf.leetcode.plugin.review.backend.algorithm.result.FSRSAlgorithmResult;
import com.xhf.leetcode.plugin.review.backend.card.QuestionCard;
import com.xhf.leetcode.plugin.review.backend.card.QuestionCardReq;
import com.xhf.leetcode.plugin.review.backend.card.QuestionFront;
import com.xhf.leetcode.plugin.review.backend.database.DatabaseAdapter;
import com.xhf.leetcode.plugin.utils.GsonUtils;

import com.xhf.leetcode.plugin.utils.LogUtils;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * 每次实例化App后, 必须调用init()方法
 * @author 文艺倾年
 */
public class AlgorithmApp {

    private static volatile AlgorithmApp instance;
    private Map<Integer, QuestionCard> cards;
    private DatabaseAdapter databaseAdapter;

    /**
     * 实例化 AlgorithmApp。在这里执行启动应用程序所需的重要步骤
     */
    private AlgorithmApp(Project project) {
        // 实例化 HashMap，用于存储从数据库加载的卡片
        this.cards = new HashMap<>();
        this.databaseAdapter = DatabaseAdapter.getInstance(project);
    }

    /**
     * 每次实例化完成后, 必须调用init()方法
     */
    public void init() {
        loadCards();
    }

    /**
     * 从数据库表 "cards" 加载卡片。
     * 卡片将存储在类的 HashMap "cards" 中
     */
    public void loadCards() {
        String query = "SELECT * FROM cards";
        LogUtils.simpleDebug("[cards] 查询所有数据 SQL: " + query);

        this.databaseAdapter.getSqlite().query(query, resultSet -> {
            try {
                while (resultSet.next()) {
                    String strFront = resultSet.getString("front");
                    QuestionFront questionFront = GsonUtils.fromJson(strFront, QuestionFront.class);
                    QuestionCard card = new QuestionCard(
                            resultSet.getInt("card_id"),
                            questionFront,
                            resultSet.getString("back"),
                            resultSet.getLong("created"),
                            resultSet.getLong("next_repetition")
                    );
                    card.setNextReview(resultSet.getLong("next_repetition"));
                    this.cards.put(card.getId(), card);

                    LogUtils.simpleDebug("[Cards] Sucessfully loaded card " + card.getId());
                }
            } catch (SQLException e) {
                LogUtils.warn(DebugUtils.getStackTraceAsString(e));
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
                    instance.loadCards();
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
     * 根据卡片ID获取卡片对象
     * @param id 卡片的ID
     * @return 加载的卡片对象
     */
    public QuestionCard getById(Integer id) {
        // 根据ID从HashMap中获取卡片对象
        return this.cards.get(id);
    }

    /**
     * 使用给定的数据创建一张卡片
     */
    public void create(QuestionCardReq questionCardReq) {
        Integer id = questionCardReq.getId();
        QuestionFront front = questionCardReq.getFront();
        String back = questionCardReq.getBack();
        String strFront = GsonUtils.toJsonStr(front);
        // 根据打分计算参数
        Integer rating = questionCardReq.getFsrsRating().toInt();
        FSRSAlgorithm algorithm = FSRSAlgorithm.builder()
                .rating(FSRSRating.values()[rating])
                .stability(0)
                .difficulty(0)
                .elapsedDays(0)
                .repetitions(0)
                .state(FSRSState.values()[0])
                .lastReview(0)
                .build();
        FSRSAlgorithmResult result = algorithm.calc();
        Long created = System.currentTimeMillis(); // 当前时间作为创建时间

        // 插入数据库
        String insert = "INSERT OR REPLACE INTO cards (card_id, front, back, created, repetitions, difficulty, stability, elapsed_days, state, day_interval, next_repetition, last_review) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        LogUtils.simpleDebug("[cards] 插入数据 SQL: " + insert);

        try (
            PreparedStatement ps = this.databaseAdapter.getSqlite().prepare(insert))
        {
            ps.setInt(1, id);
            ps.setString(2, strFront);
            ps.setString(3, back);
            ps.setString(4, created.toString());
            ps.setLong(5, result.getRepetitions());
            ps.setFloat(6, result.getDifficulty());
            ps.setFloat(7, result.getStability());
            ps.setInt(8, result.getElapsedDays());
            ps.setInt(9, result.getState().toInt());
            ps.setInt(10, result.getInterval());
            ps.setLong(11, result.getNextRepetitionTime());
            ps.setLong(12, result.getLastReview());
            ps.executeUpdate();
            LogUtils.info("[Cards] Sucessfully inserted card " + id + " into database");
        } catch (SQLException e) {
            LogUtils.info("[Cards] Failed inserting the card " + id + " into database: " + e);
        }
        QuestionCard card = new QuestionCard(id, front, back, created, result.getNextRepetitionTime());
        this.cards.put(id, card);
        LogUtils.info("[Cards] 成功本地创建卡片 " + id);
    }


    /**
     * 通过从数据库和AlgorithmApp中的HashMap "cards" 删除来删除卡片
     */
    public void delete(QuestionCard card) {
        String delete = "DELETE FROM cards WHERE card_id = '" + card.getId() + "'";
        LogUtils.simpleDebug("[cards] 删除数据 SQL: " + delete);

        this.databaseAdapter.getSqlite().update(delete);
        this.cards.remove(card.getId(), card);
        // 数据库操作
        LogUtils.info("[Cards] 成功删除卡片 " + card.getId());
    }

    public void update(QuestionCard card, String back) {
        String updateBack = "UPDATE cards SET back = '" + back + "' WHERE card_id = '" + card.getId() + "'";
        LogUtils.simpleDebug("[cards] 更新back数据 SQL: " + updateBack);

        this.databaseAdapter.getSqlite().update(updateBack);
        // 数据库操作
        LogUtils.info("[Cards] 成功删除卡片 " + card.getId());
    }
}
