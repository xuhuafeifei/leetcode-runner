package com.xhf.leetcode.plugin.review.backend.algorithm.result;

/**
 * @author 文艺倾年
 */
public class AlgorithmResult {
    private int interval;

    /**
     * 构造函数用于创建 AlgorithmResult 类的实例。
     * 每个实现的算法必须向构造函数传递一个新的间隔。
     * @param interval 新的复习间隔（以天为单位）
     */
    public AlgorithmResult(int interval) {
        this.interval = interval;
    }

    /**
     * 计算下一次复习的时间点（以毫秒为单位）
     * @return 下一次复习的时间点（毫秒）
     */
    public long getNextRepetitionTime() {
        // 一天的毫秒数
        long dayMillis = 60 * 60 * 24 * 1000;
        // 当前时间的毫秒数
        long currentMillis = System.currentTimeMillis();

        return currentMillis + (dayMillis * interval);
    }

    /**
     * 获取计算出的新复习间隔（以天为单位）
     * @return 新的复习间隔（天）
     */
    public int getInterval() {
        return this.interval;
    }
}