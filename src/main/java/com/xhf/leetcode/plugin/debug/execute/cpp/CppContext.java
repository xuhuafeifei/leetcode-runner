package com.xhf.leetcode.plugin.debug.execute.cpp;

import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.bus.WatchPoolRemoveEvent;
import com.xhf.leetcode.plugin.debug.execute.AbstractExecuteContext;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class CppContext extends AbstractExecuteContext {
    public CppContext(Project project) {
        super(project);
    }

    @Override
    public void removeWatchPoolListener(WatchPoolRemoveEvent event) {

    }
}
