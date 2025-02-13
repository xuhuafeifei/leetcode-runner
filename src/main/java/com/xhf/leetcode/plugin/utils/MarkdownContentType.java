package com.xhf.leetcode.plugin.utils;

public enum MarkdownContentType {
    /**
     * 题解
     */
    SOLUTION("SOLUTION"),
    /**
     * 题目
     */
    QUESTION("QUESTION"),
    /**
     * 0x3f, 灵神题单
     */
    _0x3f("_0x3f");

    MarkdownContentType(String type) {
        this.type = type;
    }
    private final String type;

    @Override
    public String toString() {
        return "MarkdownContentType{" +
                "type='" + type + '\'' +
                '}';
    }
}
