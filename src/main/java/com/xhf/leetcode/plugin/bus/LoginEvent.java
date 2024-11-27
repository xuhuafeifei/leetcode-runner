package com.xhf.leetcode.plugin.bus;

import com.intellij.openapi.project.Project;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class LoginEvent extends LCEvent {
    public LoginEvent(Project project) {
        super(LoginEvent.class, project);
    }
}
