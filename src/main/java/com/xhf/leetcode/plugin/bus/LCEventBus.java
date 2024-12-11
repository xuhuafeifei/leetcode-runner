package com.xhf.leetcode.plugin.bus;

import com.google.common.eventbus.EventBus;

/**
 * 使用guava的EventBus, LCEventBus对其api做出封装, 适配当前项目
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class LCEventBus {
    private final EventBus eventBus;

    private LCEventBus() {
        eventBus = new EventBus();
    }

    private static class SingletonHolder {
        private static final LCEventBus INSTANCE = new LCEventBus();
    }

    public static LCEventBus getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void post(Object event) {
        eventBus.post(event);
    }

    public void register(Object listener) {
        eventBus.register(listener);
    }
}
