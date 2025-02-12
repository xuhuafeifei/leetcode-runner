package com.xhf.leetcode.plugin.bus;

import com.intellij.openapi.project.Project;

/**
 * code submit事件, 当代码提交完成后, 并更新缓存. 则会发出此事件
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class CodeSubmitEvent {
    public CodeSubmitEvent(Project project) {
    }
}
