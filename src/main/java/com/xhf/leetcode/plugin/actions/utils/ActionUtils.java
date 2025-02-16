package com.xhf.leetcode.plugin.actions.utils;

import com.google.common.util.concurrent.RateLimiter;

import java.util.concurrent.TimeUnit;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class ActionUtils {
    private static RateLimiter rateLimiter = RateLimiter.create(1, 500, TimeUnit.MILLISECONDS);

    public synchronized static boolean get() {
        return rateLimiter.tryAcquire();
    }
}
