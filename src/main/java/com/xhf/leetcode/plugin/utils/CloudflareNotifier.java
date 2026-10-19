package com.xhf.leetcode.plugin.utils;

import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;

/**
 * Cloudflare 质询拦截通知
 * <p>
 * leetcode.cn 风控会拦截插件请求并返回 "Just a moment..." 质询页(HTML),
 * 该情况需要用户在浏览器完成人机验证才能恢复, 必须显式告知用户, 不允许静默失败.
 * <p>
 * 60 秒内去重: 被拦截期间几乎每个请求都会命中质询, 避免通知刷屏.
 */
public class CloudflareNotifier {

    private static volatile long lastNotifyTime = 0L;
    private static final long DEDUP_INTERVAL_MS = 60_000L;

    private static final String MESSAGE =
        "请求被 leetcode.cn 风控拦截 (Cloudflare 质询)。\n\n"
            + "自救方法:\n"
            + "1. 用浏览器打开 leetcode.cn, 完成一次人机验证(勾选\"确认您是真人\")\n"
            + "2. 等待几分钟后重试, 通常会自动恢复\n"
            + "3. 若频繁出现, 请降低短时间内的运行/提交频率, 或检查代理/VPN";

    private CloudflareNotifier() {
    }

    /**
     * 弹出 Cloudflare 拦截警告通知 (60秒去重)
     */
    public static void notifyChallenge() {
        long now = System.currentTimeMillis();
        if (now - lastNotifyTime < DEDUP_INTERVAL_MS) {
            return;
        }
        lastNotifyTime = now;
        try {
            Project[] projects = ProjectManager.getInstance().getOpenProjects();
            if (projects.length == 0) {
                return;
            }
            NotificationGroupManager.getInstance()
                .getNotificationGroup("leetcode-runner.notifications")
                .createNotification("Leetcode-Runner: 请求被 Cloudflare 拦截", MESSAGE, NotificationType.WARNING)
                .notify(projects[0]);
        } catch (Exception e) {
            // 通知失败不影响主流程, 日志兜底
            LogUtils.error("failed to show cloudflare notification: " + e.getMessage());
        }
    }
}
