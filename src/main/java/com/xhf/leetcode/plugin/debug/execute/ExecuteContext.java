package com.xhf.leetcode.plugin.debug.execute;

import com.intellij.openapi.project.Project;

import java.util.Deque;

/**
 * 执行上下文
 * 因为一开始考虑不周全, 导致Java的上下文命名是Context, python的是PyContext, 感觉有点ambiguous
 * 但Java 的Context已经比较常用了, 所以还是沿用这个命名吧
 */
public interface ExecuteContext {
    Project getProject();

    Deque<String> getWatchPool();
}
