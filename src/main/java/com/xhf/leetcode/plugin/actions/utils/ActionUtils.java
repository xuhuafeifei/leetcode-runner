package com.xhf.leetcode.plugin.actions.utils;

import com.google.common.util.concurrent.RateLimiter;
import com.xhf.leetcode.plugin.window.TimerWindow;

import java.util.concurrent.TimeUnit;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class ActionUtils {
    private static RateLimiter rateLimiter = RateLimiter.create(1, 500, TimeUnit.MILLISECONDS);

    private static TimerWindow windowInstance;

    public synchronized static boolean get() {
        return rateLimiter.tryAcquire();
    }

    public static TimerWindow getTimerWindow() {
        return windowInstance;
    }

    public static TimerWindow createTimerWindow() {
        disposeTimer();
        windowInstance = new TimerWindow();
        return windowInstance;
    }

    public static void disposeTimer() {
        if (windowInstance != null) {
            windowInstance.dispose();
            windowInstance = null;
        }
    }
}
