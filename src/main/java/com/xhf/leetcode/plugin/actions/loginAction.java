package com.xhf.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.service.LoginService;
import com.xhf.leetcode.plugin.utils.LoginPass;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
@LoginPass
public class loginAction extends AbstractAction {
    @Override
    void doActionPerformed(Project project, AnActionEvent e) {
        LoginService.getInstance(project).doLogin();
    }
}
