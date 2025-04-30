package com.xhf.leetcode.plugin.review.backend.card;

import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.review.backend.algorithm.AlgorithmApp;
import com.xhf.leetcode.plugin.review.backend.algorithm.FSRSAlgorithm;
import com.xhf.leetcode.plugin.review.backend.algorithm.constant.FSRSRating;
import com.xhf.leetcode.plugin.review.backend.algorithm.constant.FSRSState;
import com.xhf.leetcode.plugin.review.backend.algorithm.constant.ReviewStatus;
import com.xhf.leetcode.plugin.review.backend.algorithm.result.FSRSAlgorithmResult;
import com.xhf.leetcode.plugin.review.backend.model.ReviewQuestion;
import com.xhf.leetcode.plugin.review.backend.model.ReviewQuestionModel;
import com.xhf.leetcode.plugin.utils.GsonUtils;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author 文艺倾年
 */
public class QuestionCard {

    private final AlgorithmApp app;
    private final Project project;
    /*
        id是Question在QuestionList中的index下标
     */

    private Integer id; // 卡片ID

    private final QuestionFront front; // 卡片前面题目
    private final String back; // 背部答案
    private Long nextReview;
    private Long created; // 创建时间

    /**
     * 构造函数，用于初始化卡片的所有传入参数
     * @param id 卡片的id，用于标识
     * @param front 卡片的正面文本
     * @param back 卡片的背面文本
     * @param created 卡片的创建时间
     */
    public QuestionCard(Integer id, QuestionFront front, String back, Long created, Project project) {
        this.id = id;
        this.front = front;
        this.back = back;
        this.created = created;
        this.project = project;
        // 每次实例化后，自动插入到Map中
        this.app = AlgorithmApp.getInstance(project);
        app.getCards().put(id, this);
    }


    /**
     * 获取唯一标识符
     * @return 唯一标识符
     */
    public Integer getId() {
        return id;
    }

    /**
     * 设置唯一标识符
     * @param id 唯一标识符
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 获取问题内容
     * @return 问题内容
     */
    public QuestionFront getFront() {
        return this.front;
    }

    public String getBack() {
        return this.back;
    }

    /**
     * 获取创建时间
     * @return 创建时间
     */
    public Long getCreated() {
        return created;
    }

    /**
     * 设置创建时间
     * @param created 创建时间
     */
    public void setCreated(Long created) {
        this.created = created;
    }

    /**
     * 根据卡片ID获取卡片对象
     * @param id 卡片的ID
     * @return 加载的卡片对象
     */
    public QuestionCard getById(Integer id) {
        // 根据ID从HashMap中获取卡片对象
        return app.getCards().get(id);
    }

    /**
     * 使用给定的数据创建一张卡片
     */
    public static void create(QuestionCardReq questionCardReq) {
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
        // TODO 增加条件判断，若数据已经存在，则执行更新。
        Long created = System.currentTimeMillis(); // 当前时间作为创建时间
        // 插入数据库
        try {
            PreparedStatement ps =
                AlgorithmApp.getInstance(questionCardReq.getProject()).getDatabaseAdapter().getSqlite().prepare("INSERT INTO cards (card_id, front, back, created, repetitions, difficulty, stability, elapsed_days, state, day_interval, next_repetition, last_review) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
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
            System.out.println("[Cards] Sucessfully inserted card " + id + " into database");
        } catch (SQLException e) {
            System.out.println("[Cards] Failed inserting the card " + id + " into database: " + e);
        }
        // 避免每次创建后，手动去再去同步到内存中
        new QuestionCard(id, front, back, created, questionCardReq.getProject());
        System.out.println("[Cards] 成功本地创建卡片 " + id);
    }

    public void setNextReview(Long nextReview) {
        this.nextReview = nextReview;
    }

    public Long getNextReview() {
        return this.nextReview;
    }

    /**
     * 通过从数据库和AlgorithmApp中的HashMap "cards" 删除来删除卡片
     */
    public void delete() {
        app.getDatabaseAdapter().getSqlite().update("DELETE FROM cards WHERE card_id = '" + this.id + "'");
        app.getCards().remove(this.id, this);
        // 数据库操作
        System.out.println("[Cards] 成功删除卡片 " + this.id);
    }

    public ReviewQuestion toReviewQuestion() {
        ReviewQuestionModel model = new ReviewQuestionModel();
        model.setId(this.getId());
        model.setUserSolution(this.getBack());
        model.setNextReview(this.handleNextReview());
        model.setLastModify(this.handleLastModify());
        model.setUserRate(FSRSRating.toName(this.getFront().getUserRate()));
        model.setStatus(this.handleStatus());
        model.setTitle(this.getFront().getTitle());
        model.setDifficulty(this.getFront().getDifficulty());
        return model;
    }

    private String handleStatus() {
        // 判断当前题目状态, 是否逾期
        Long a = this.getNextReview();
        Long b = System.currentTimeMillis();
        // 判断时间差是否大于24h
        if (a != null && b - a > 24 * 60 * 60 * 1000) {
            return ReviewStatus.OVER_TIME.getName();
        }
        return ReviewStatus.NOT_START.getName();
    }

    private String handleLastModify() {
        Long time = this.getCreated();
        if (time != null) {
            return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(time));
        }
        return "";
    }

    private String handleNextReview() {
        Long time = this.getNextReview();
        // 把timestamp转换成日期格式, 处理成YYYY-MM-DD格式的字符串
        if (time != null) {
            return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(time));
        }
        return "";
    }

    public void update(String back) {
        app.getDatabaseAdapter().getSqlite().update("UPDATE cards SET back = " + back + " WHERE card_id = '" + this.id + "'");
        app.getCards().remove(this.id, this);
        // 数据库操作
        System.out.println("[Cards] 成功删除卡片 " + this.id);
    }

}
