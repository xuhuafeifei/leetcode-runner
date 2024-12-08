package com.xhf.leetcode.plugin.bus;

/**
 * 该注解没什么实际作用, 主要是提醒开发者, 当前类是订阅者, 以及他订阅了哪些事件
 */
public @interface LCSubscriber {
    Class[] events();
}
