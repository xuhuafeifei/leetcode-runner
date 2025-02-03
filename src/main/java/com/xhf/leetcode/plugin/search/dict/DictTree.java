package com.xhf.leetcode.plugin.search.dict;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

/**
 * 字典树, 将词组以树的形式存储
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class DictTree {
    private final DictNode head;

    private DictTree() {
        head = new DictNode();
        loadMainData();
        loadExtData();
    }

    /**
     * 加载词典数据
     * @param path
     */
    private void loadData(String path) {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        Objects.requireNonNull(DictTree.class.getResourceAsStream(path))
                ))
        ) {
            String line;
            while ((line = br.readLine()) != null) {
                addWords(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 加载扩展词典数据
     */
    private void loadExtData() {
        loadData("/dict/ext.dic");
    }

    private void loadMainData() {
        loadData("/dict/main.dic");
    }

    // 给我一个懒汉单例
    private static final DictTree INSTANCE = new DictTree();

    public static DictTree getInstance() {
        return INSTANCE;
    }

    /**
     * 添加词组
     * @param words 词组
     */
    public void addWords(String words) {
        if (StringUtils.isBlank(words)) {
            return;
        }
        this.addWords(words.trim().toCharArray());
    }

    public void addWords(char[] wordArray) {
        DictNode head = this.head;
        // 迭代添加
        for (char c : wordArray) {
            // 头节点不包含字符c
            if (!head.contains(c)) {
                // 创建并添加c
                head.add(c);
            }
            // 迭代
            head = head.getChild(c);
        }
        // 标记词组结尾
        head.setEnd(true);
    }

    /**
     * 以字符c为开始, 匹配字典树的节点, 并返回Hit
     * @param c 匹配的字符c
     * @return Hit命中对象, 缓存当前匹配的信息
     */
    public Hit match(char c) {
        DictNode head = this.head;
        if (! head.contains(c)) {
            return Hit.notHit(c);
        }
        DictNode child = head.getChild(c);
        return child.isEnd() ? Hit.hitInEnd(c, child) : Hit.hitNotInEnd(c, child);
    }

    /**
     * 以字符searchC为匹配对象, 以hit中缓存的匹配信息开始, 继续匹配字典树
     *
     * @param searchC 匹配对象
     * @param hit 匹配信息
     * @return Hit命中对象, 缓存searchC匹配的信息
     */
    public Hit match(char searchC, Hit hit) {
        DictNode head = hit.getNode();
        if (head == null) {
            return Hit.notHit(searchC);
        }
        if (! head.contains(searchC)) {
            return Hit.notHit(searchC);
        }
        DictNode child = head.getChild(searchC);
        return child.isEnd() ? Hit.hitInEnd(searchC, child) : Hit.hitNotInEnd(searchC, child);
    }
}
