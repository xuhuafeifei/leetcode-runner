package com.xhf.leetcode.plugin.review.backend.card;

import com.xhf.leetcode.plugin.review.backend.test.TestAlgorithmApp;

import java.util.UUID;

/**
 * @author 文艺倾年
 */
public class QuestionCard {

    private UUID uuid; // 唯一标识符
    private String front, back; // 问题内容
    private Long created; // 创建时间

    /**
     * 构造函数，用于初始化卡片的所有传入参数
     * @param uuid 卡片的UUID，用于标识
     * @param front 卡片的正面文本
     * @param back 卡片的背面文本
     * @param created 卡片的创建时间
     */
    public QuestionCard(UUID uuid, String front, String back, Long created) {
        this.uuid = uuid;
        this.front = front;
        this.back = back;
        this.created = created;
        // 每次实例化后，自动插入到Map中
        TestAlgorithmApp.getInstance().getCards().put(uuid, this);
    }


    /**
     * 获取唯一标识符
     * @return 唯一标识符
     */
    public UUID getUUID() {
        return uuid;
    }

    /**
     * 设置唯一标识符
     * @param uuid 唯一标识符
     */
    public void setUUID(UUID uuid) {
        this.uuid = uuid;
    }

    /**
     * 获取问题内容
     * @return 问题内容
     */
    public String getFront() {
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
     * Erhalten einer Karteikarte über ihre UUID
     * @param uuid die UUID der Karteikarte
     * @return geladenes Card-Objekt
     */
    public static QuestionCard getByUUID(UUID uuid) {
        // Erhalte die Karteikarte (Card) aus der HashMap per UUID
        return TestAlgorithmApp.getInstance().getCards().get(uuid);
    }

    /**
     * 使用给定的数据创建一张卡片，并生成一个随机的UUID
     * @param front 卡片的正面文本
     * @param back 卡片的背面文本
     */
    public static void create(String front, String back) {
        UUID uuid = UUID.randomUUID(); // 生成随机的UUID
        Long created = System.currentTimeMillis(); // 当前时间作为创建时间

        // 插入数据库
        //
        new QuestionCard(uuid, front, back, created);
        System.out.println("[Cards] 成功本地创建卡片 " + uuid);
    }

    /**
     * 通过从数据库和TestAlgorithmApp中的HashMap "cards" 删除来删除卡片
     */
    public void delete() {
        TestAlgorithmApp.getInstance().getCards().remove(this.uuid, this);
        // 数据库操作
        System.out.println("[Cards] 成功删除卡片 " + this.uuid);
    }
}
