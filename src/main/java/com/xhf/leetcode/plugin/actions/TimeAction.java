package com.xhf.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.editors.TimeWidget;

public class TimeAction extends AbstractAction {
    @Override
    protected void doActionPerformed(Project project, AnActionEvent e) {
        new TimeWidget(project).show();
    }
}
