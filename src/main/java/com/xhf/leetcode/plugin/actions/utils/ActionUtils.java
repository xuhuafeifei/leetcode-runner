package com.xhf.leetcode.plugin.actions.utils;

import com.google.common.util.concurrent.RateLimiter;
import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.personal.PersonalWindow;
import com.xhf.leetcode.plugin.review.front.ReviewWindow;
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

    private static ReviewWindow reviewWindow;

    public static void createReviewWindow(Project project) {
        if (reviewWindow == null) {
            reviewWindow = new ReviewWindow(project);
            reviewWindow.setVisible(true);
        } else {
            disposeReviewWindow();
        }
    }

    public static void disposeReviewWindow() {
        if (reviewWindow != null) {
            reviewWindow.dispose();
            reviewWindow = null;
        }
    }

    private static PersonalWindow personalWindow;

    public static void createPersonalWindow(Project project) {
        if (personalWindow == null) {
            personalWindow = new PersonalWindow(project);
            personalWindow.setVisible(true);
        } else {
            disposePersonalWindow();
        }
    }

    public static void disposePersonalWindow() {
        if (personalWindow != null) {
            personalWindow.dispose();
            personalWindow = null;
        }
    }
}
