package com.xhf.leetcode.plugin.bus;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class LCEventBus {
    Map<Class<? extends LCEvent>, Set<LCSubscriber>> subscriberMap;

    private LCEventBus() {
        subscriberMap = new HashMap<>();
    }

    private static class SingletonHolder {
        private static final LCEventBus INSTANCE = new LCEventBus();
    }

    public static LCEventBus getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void post(LCEvent event) {
        Set<LCSubscriber> subscribers = subscriberMap.get(event.getTopic());
        if (subscribers != null) {
            for (LCSubscriber subscriber : subscribers) {
                subscriber.onEvent(event);
            }
        }
    }

    public void register(Class<? extends LCEvent> topic, LCSubscriber subscriber) {
        if (subscriberMap.containsKey(topic)) {
            subscriberMap.get(topic).add(subscriber);
        }else {
            Set<LCSubscriber> lcSubscribers = new HashSet<>();
            lcSubscribers.add(subscriber);
            subscriberMap.put(topic, lcSubscribers);
        }
    }
}
