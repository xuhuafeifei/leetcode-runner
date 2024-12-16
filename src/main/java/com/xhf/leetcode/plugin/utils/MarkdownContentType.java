package com.xhf.leetcode.plugin.utils;

public enum MarkdownContentType {
    /**
     * 题解
     */
    SOLUTION("SOLUTION"),
    /**
     * 题目
     */
    QUESTION("QUESTION");

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
