package com.xhf.leetcode.plugin.bus;

import com.intellij.openapi.project.Project;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class CodeSubmitEvent extends LCEvent {
    public CodeSubmitEvent(Project project) {
        super(CodeSubmitEvent.class, project);
    }
}
