package com.xhf.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.service.CodeService;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class RePositionAction extends AbstractAction {
    @Override
    void doActionPerformed(Project project, AnActionEvent e) {
        CodeService.getInstance(project).rePosition();
    }
}
