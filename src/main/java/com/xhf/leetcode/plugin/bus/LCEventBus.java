package com.xhf.leetcode.plugin.bus;

import com.google.common.eventbus.EventBus;
import com.xhf.leetcode.plugin.utils.LogUtils;

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

    public void postSync(Object event) {
        eventBus.post(event);
    }

    public void register(Object listener) {
        LogUtils.debug(listener.getClass().getName() + " has been register to event bus...");
        eventBus.register(listener);
    }

    public void remove(Object listener) {
        eventBus.unregister(listener);
    }
}
