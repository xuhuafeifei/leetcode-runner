package com.xhf.leetcode.plugin.review.backend.card;

import java.util.UUID;

/**
 * @author 文艺倾年
 */
public class QuestionCard {

    private UUID uuid; // 唯一标识符
    private String question; // 问题内容
    private Long created; // 创建时间

    /**
     * 获取唯一标识符
     * @return 唯一标识符
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * 设置唯一标识符
     * @param uuid 唯一标识符
     */
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    /**
     * 获取问题内容
     * @return 问题内容
     */
    public String getQuestion() {
        return question;
    }

    /**
     * 设置问题内容
     * @param question 问题内容
     */
    public void setQuestion(String question) {
        this.question = question;
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
}
