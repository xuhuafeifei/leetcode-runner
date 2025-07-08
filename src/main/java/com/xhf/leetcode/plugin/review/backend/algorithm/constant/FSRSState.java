package com.xhf.leetcode.plugin.review.backend.algorithm.constant;

/**
 * @author 文艺倾年
 */
public enum FSRSState {
    /**
     * 从未学习过
     */
    NEW(0),
    /**
     * 刚刚第一次学习
     */
    LEARNING(1),
    /**
     * 完成LEARNING状态
     */
    REVIEW(2),
    /**
     * 在REVIEW状态时忘记
     */
    RELEARNING(3);

    private final Integer id;

    /**
     * 构造器
     *
     * @param id 状态的ID (0-3)
     */
    FSRSState(Integer id) {
        this.id = id;
    }

    /**
     * 获取状态的整数值
     *
     * @return 状态的ID
     */
    public Integer toInt() {
        return this.id;
    }
}
