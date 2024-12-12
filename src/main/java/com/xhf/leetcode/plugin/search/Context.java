package com.xhf.leetcode.plugin.search;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

/**
 * 上下文数据, 保留文本处理过程中的上下文信息
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class Context {
    private final SourceManager sm;
    // 当前处理的字符
    private char c;
    // 迭代器
    private Iterator iterator;
    // tokens
    private final Queue<Token> tokens = new LinkedList<>();



    public Context(Character c, Iterator itr, SourceManager sm) {
        this(itr, sm);
        this.c = c;
    }

    public Context(Iterator itr, SourceManager sm) {
        this.iterator = itr;
        this.sm = sm;
    }

    public SourceManager getSm() {
        return sm;
    }

    public char getC() {
        return c;
    }

    public void setC(char c) {
        this.c = c;
    }

    public boolean hasToken() {
        return ! tokens.isEmpty();
    }
    public Token getToken() {
        return tokens.poll();
    }

    public void addToken(String token, int len) {
        tokens.add(new Token(token, len));
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
