package com.xhf.leetcode.plugin.review.front;

import com.intellij.util.Consumer;

import java.util.List;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class ReviewEnv {
    
    private final List<Consumer<String>> listeners;

    public ReviewEnv() {
        listeners = new java.util.ArrayList<>();
    }
    
    public void post(String content) {
        for (Consumer<String> listener : listeners) {
            listener.consume(content);
        }
    }

    public void registerListener(Consumer<String> listener) {
        listeners.add(listener);
    }
}
