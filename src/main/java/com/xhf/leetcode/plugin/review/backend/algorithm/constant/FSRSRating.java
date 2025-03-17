package com.xhf.leetcode.plugin.review.backend.algorithm.constant;

/**
 * @author 文艺倾年
 */
public enum FSRSRating {
    /**
     * 忘记；错误答案
     */
    AGAIN(0),
    /**
     * 回忆起来；经过一定困难才答出的正确答案
     */
    HARD(1),
    /**
     * 经过延迟答出的正确答案
     */
    GOOD(2),
    /**
     * 完美答案
     */
    EASY(3);

    /**
     * 构造函数
     * @param id 评分的ID（0-3）
     */
    FSRSRating(int id) {
        this.id = id;
    }

    private int id;

    /**
     * 获取评分的整数值
     * @return 评分的ID
     */
    public int toInt() {
        return this.id;
    }
}