package com.xhf.leetcode.plugin.debug.execute;

import com.google.common.eventbus.Subscribe;
import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.bus.DebugEndEvent;
import com.xhf.leetcode.plugin.bus.DebugStartEvent;
import com.xhf.leetcode.plugin.bus.WatchPoolRemoveEvent;

/**
 * 执行上下文
 * 因为一开始考虑不周全, 导致Java的上下文命名是Context, python的是PyContext, 感觉有点ambiguous
 * 但Java 的Context已经比较常用了, 所以还是沿用这个命名吧
 */
public interface ExecuteContext {
    Project getProject();

    void addToWatchPool(String s);

    String[] getWatchPool();

    /**
     * watch pool 移除监听
     * @param event
     */
    @Subscribe
    void removeWatchPoolListener(WatchPoolRemoveEvent event);

    @Subscribe
    void debugStartListener(DebugStartEvent event);

    @Subscribe
    void debugStopListener(DebugEndEvent event);
}
