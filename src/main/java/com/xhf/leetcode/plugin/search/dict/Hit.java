package com.xhf.leetcode.plugin.search.dict;

import com.xhf.leetcode.plugin.search.utils.CharacterHelper;

/**
 * 存储字符在字典树中的命中信息
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class Hit {
    /**
     * 用于查询得到hit的字符c
     */
    private char c;
    /**
     * node指向字典树中的某一个节点, 表示搜索开始的位置
     */
    private DictNode node;
    /**
     * c是否再字典树中命中
     */
    private boolean isHit;
    /**
     * c是否是词组的结束
     */
    private boolean isEnd;

    public Hit() {

    }

    public Hit(char c, boolean isEnd, boolean isHit) {
        this.c = c;
        this.isHit = isHit;
        this.isEnd = isEnd;
    }

    public static Hit notHit(char c) {
        Hit hit = new Hit(CharacterHelper.NULL, true, false);
        hit.c = c;
        return hit;
    }

    public static Hit hitNotInEnd(char c, DictNode child) {
        Hit hit = new Hit();
        hit.c = c;
        hit.node = child;
        hit.isHit = true;
        hit.isEnd = false;
        return hit;
    }
    public static Hit hitInEnd(char c, DictNode child) {
        Hit hit = new Hit();
        hit.c = c;
        hit.node = child;
        hit.isHit = true;
        hit.isEnd = true;
        return hit;
    }

    public char getC() {
        return c;
    }

    public void setC(char c) {
        this.c = c;
    }

    public DictNode getNode() {
        return node;
    }

    public void setNode(DictNode node) {
        this.node = node;
    }

    public boolean isHit() {
        return isHit;
    }

    public void setHit(boolean hit) {
        isHit = hit;
    }

    public boolean isEnd() {
        return isEnd;
    }

    public void setEnd(boolean end) {
        isEnd = end;
    }
}
