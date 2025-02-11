package com.xhf.leetcode.plugin.model;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class DeepCodingInfo {
    // 模式, 表示是deep coding的哪种方式进入编码
    private final String pattern;
    private final int idx;
    // 获取当前deep coding模式下, 题目总量
    private final int totalLength;

    public DeepCodingInfo(String pattern, int totalLen, int idx) {
        this.pattern = pattern;
        this.totalLength = totalLen;
        this.idx = idx;
    }

    public String getPattern() {
        return pattern;
    }

    public int getTotalLength() {
        return totalLength;
    }

    public int getIdx() {
        return idx;
    }
}
