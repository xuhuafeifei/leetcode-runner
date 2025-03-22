package com.xhf.leetcode.plugin.review.front;

import com.xhf.leetcode.plugin.utils.BundleUtils;

/**
 * 题目复习状态枚举类
 * @author feigebuge
 */
public enum ReviewStatus {

    NOT_START(0, "未开始", "not start"),
    OVER_TIME(1, "逾期", "over time"),
    DONE(2, "已完成", "done");

    private final int id;          // 唯一标识
    private final String cnName;   // 中文描述
    private final String enName;   // 英文描述

    // 构造函数
    ReviewStatus(int id, String cnName, String enName) {
        this.id = id;
        this.cnName = cnName;
        this.enName = enName;
    }

    /**
     * 根据 id 获取对应的复习状态
     *
     * @param id 状态标识
     * @return 对应的 ReviewStatus
     * @throws IllegalArgumentException 如果 id 无效
     */
    public static ReviewStatus fromId(int id) {
        for (ReviewStatus status : values()) {
            if (status.getId() == id) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的复习状态 id: " + id);
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

    public String getName() {
        return BundleUtils.i18nHelper(cnName, enName);
    }

    @Override
    public String toString() {
        return String.format("%s (%s) [id=%d]", cnName, enName, id);
    }
} 
