package com.xhf.leetcode.plugin.search;

public interface Iterator {
    /**
     * 判断是否还有下一个元素
     * @return
     */
    boolean hasNext();
    char next();
    char peekNext();
    char current();
    void reset();

    Iterator deepcopy();
}
