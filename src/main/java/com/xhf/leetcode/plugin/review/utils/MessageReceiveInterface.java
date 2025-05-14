package com.xhf.leetcode.plugin.review.utils;

/**
 * 消息接受接口, 用于通过{@link com.xhf.leetcode.plugin.review.front.ReviewEnv}内置的EventBus获取消息
 * @feigebuge
 */
public interface MessageReceiveInterface {

    void onMessageReceived(String msg);

}
