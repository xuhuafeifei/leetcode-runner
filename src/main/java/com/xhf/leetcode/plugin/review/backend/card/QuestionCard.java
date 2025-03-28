package com.xhf.leetcode.plugin.review.backend.card;

import com.xhf.leetcode.plugin.review.backend.algorithm.AlgorithmApp;
import com.xhf.leetcode.plugin.utils.GsonUtils;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author 文艺倾年
 */
public class QuestionCard {

    private Integer id; // 卡片ID

    private QuestionFront front; // 卡片前面题目
    private String back; // 背部答案

    private Long created; // 创建时间

    /**
     * 构造函数，用于初始化卡片的所有传入参数
     * @param id 卡片的id，用于标识
     * @param front 卡片的正面文本
     * @param back 卡片的背面文本
     * @param created 卡片的创建时间
     */
    public QuestionCard(Integer id, QuestionFront front, String back, Long created) {
        this.id = id;
        this.front = front;
        this.back = back;
        this.created = created;
        // 每次实例化后，自动插入到Map中
        AlgorithmApp.getInstance().getCards().put(id, this);
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
    public static QuestionCard getById(Integer id) {
        // 根据ID从HashMap中获取卡片对象
        return AlgorithmApp.getInstance().getCards().get(id);
    }

    /**
     * 使用给定的数据创建一张卡片
     */
    public static void create(QuestionCardReq questionCardReq) {
        Integer id = questionCardReq.getId();
        QuestionFront front = questionCardReq.getFront();
        String back = questionCardReq.getBack();
        String strFront = GsonUtils.toJsonStr(front);
        // TODO 增加条件判断，若数据已经存在，则执行更新。
        Long created = System.currentTimeMillis(); // 当前时间作为创建时间
        // 插入数据库
        try {
            PreparedStatement ps = AlgorithmApp.getInstance().getDatabaseAdapter().getSqlite().prepare("INSERT INTO cards (card_id, front, back, created) VALUES (?, ?, ?, ?)");
            ps.setInt(1, id);
            ps.setString(2, strFront);
            ps.setString(3, back);
            ps.setString(4, created.toString());
            ps.executeUpdate();
            System.out.println("[Cards] Sucessfully inserted card " + id + " into database");
        } catch (SQLException e) {
            System.out.println("[Cards] Failed inserting the card " + id + " into database: " + e);
        }
        new QuestionCard(id, front, back, created);
        System.out.println("[Cards] 成功本地创建卡片 " + id);
    }

    /**
     * 通过从数据库和AlgorithmApp中的HashMap "cards" 删除来删除卡片
     */
    public void delete() {
        AlgorithmApp.getInstance().getDatabaseAdapter().getSqlite().update("DELETE FROM cards WHERE card_id = '" + this.id + "'");
        AlgorithmApp.getInstance().getCards().remove(this.id, this);
        // 数据库操作
        System.out.println("[Cards] 成功删除卡片 " + this.id);
    }
}
