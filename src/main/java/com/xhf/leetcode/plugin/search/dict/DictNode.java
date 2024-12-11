package com.xhf.leetcode.plugin.search.dict;

import java.util.HashMap;
import java.util.Map;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class DictNode {
    private char c;
    private Map<Character, DictNode> children;
    private boolean isEnd;

    public DictNode() {
        this.isEnd = false;
        this.children = new HashMap<>(16);
    }

    public DictNode(char c) {
        this.c = c;
        this.isEnd = false;
        this.children = new HashMap<>(16);
    }

    public DictNode(char c, boolean isEnd) {
        this.c = c;
        this.isEnd = isEnd;
    }

    public char getC() {
        return c;
    }

    public boolean contains(Character c) {
        return children.containsKey(c);
    }

    public DictNode getChild(Character c) {
        return children.get(c);
    }

    public void add(Character c) {
        children.put(c, new DictNode());
    }

    public void setEnd(boolean isEnd) {
        this.isEnd = isEnd;
    }

    public boolean isEnd() {
        return isEnd;
    }
}
