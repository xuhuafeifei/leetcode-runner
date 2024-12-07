package com.xhf.leetcode.plugin.search.dict;

import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class DictTree {
    private DictNode head;

    private DictTree() {
        head = new DictNode();
        loadMainData("/dict/main.dic");
        loadExtData("/dict/ext.dic");
    }

    private void loadData(String path) {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        DictTree.class.getResourceAsStream(path)
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

    private void loadExtData(String path) {
        loadData(path);
    }

    private void loadMainData(String path) {
        loadData(path);
    }

    // 给我一个懒汉单例
    private static DictTree INSTANCE = new DictTree();

    public static DictTree getInstance() {
        return INSTANCE;
    }

    public static void init() {
        INSTANCE.init();
    }

    public void addWords(String words) {
        if (StringUtils.isBlank(words)) {
            return;
        }
        this.addWords(words.trim().toCharArray());
    }

    public void addWords(char[] wordArray) {
        DictNode head = this.head;
        for (int i = 0; i < wordArray.length; i++) {
            char c = wordArray[i];
            if (! head.contains(c)) {
                head.add(c);
            }
            head = head.getChild(c);
        }
        head.setEnd(true);
    }

    public Hit match(char c) {
        DictNode head = this.head;
        if (! head.contains(c)) {
            return Hit.notHit(c);
        }
        DictNode child = head.getChild(c);
        return child.isEnd() ? Hit.hitInEnd(c, child) : Hit.hitNotInEnd(c, child);
    }

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
