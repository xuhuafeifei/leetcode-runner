package com.xhf.leetcode.plugin.review.front;

/**
 * 用户评价枚举
 * @author feigebuge
 * @date 2025/3/22
 */
public enum UserRate {

    VERY_HARD(0, "很难", "very hard"),
    AVERAGE(1, "一般般", "average"),
    VERY_EASY(2, "很轻松", "very easy");

    private final int id;          // 唯一标识
    private final String cnName;   // 中文描述
    private final String enName;   // 英文描述

    UserRate(int id, String cnName, String enName) {
        this.id = id;
        this.cnName = cnName;
        this.enName = enName;
    }

    /**
     * 根据 id 获取对应的用户评价
     *
     * @param id 用户评价标识
     * @return 对应的 UserRate
     * @throws IllegalArgumentException 如果 id 无效
     */
    public static UserRate fromId(int id) {
        for (UserRate rate : values()) {
            if (rate.getId() == id) {
                return rate;
            }
        }
        throw new IllegalArgumentException("未知的用户评价 id: " + id);
    }

    /**
     * 根据用户评价名称获取对应的枚举
     *
     * @param name 用户评价名称（中/英文）
     * @return 对应的 UserRate
     * @throws IllegalArgumentException 如果名称无效
     */
    public static UserRate fromName(String name) {
        for (UserRate rate : values()) {
            if (rate.getCnName().equalsIgnoreCase(name) || rate.getEnName().equalsIgnoreCase(name)) {
                return rate;
            }
        }
        throw new IllegalArgumentException("未知的用户评价名称: " + name);
    }

    public int getId() {
        return id;
    }

    public String getCnName() {
        return cnName;
    }

    public String getEnName() {
        return enName;
    }

    @Override
    public String toString() {
        return String.format("%s (%s) [id=%d]", cnName, enName, id);
    }
}