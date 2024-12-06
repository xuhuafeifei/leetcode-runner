package com.xhf.leetcode.plugin.search;

/**
 * 上下文数据, 保留文本处理过程中的上下文信息
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class Context {
    // 当前处理的字符
    private char c;
    // 当前文本处理得到的token
    private String token;
    // 当前处理的字符长度(等于处理的字符次数)
    private int len;
    // 迭代器
    private Iterator iterator;

    public Context(Character c, Iterator itr) {
        this.c = c;
        this.iterator = itr;
    }

    public char getC() {
        return c;
    }

    public void setC(char c) {
        this.c = c;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getLen() {
        return len;
    }

    public void setLen(int len) {
        this.len = len;
    }

    public Iterator getIterator() {
        return iterator;
    }

    public void setIterator(Iterator iterator) {
        this.iterator = iterator;
    }

    public boolean hasNext() {
        return getIterator().hasNext();
    }

    public char nextC() {
        char next = getIterator().next();
        setC(next);
        return next;
    }
}
