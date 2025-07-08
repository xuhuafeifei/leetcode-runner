package com.xhf.leetcode.plugin.review.front;

import com.xhf.leetcode.plugin.review.utils.MessageReceiveInterface;
import java.util.List;

/**
 * 监听观察者模式, 低配版EventBus
 * 当前类不存在内存泄露问题, 如果想要注册到env中, 需要实现{@link com.xhf.leetcode.plugin.review.utils.MessageReceiveInterface}
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class ReviewEnv {

    private final List<MessageReceiveInterface> listeners;

    public ReviewEnv() {
        listeners = new java.util.ArrayList<>();
    }

    public void post(String content) {
        for (var listener : listeners) {
            listener.onMessageReceived(content);
        }
    }

    public void registerListener(MessageReceiveInterface listener) {
        listeners.add(listener);
    }
}
