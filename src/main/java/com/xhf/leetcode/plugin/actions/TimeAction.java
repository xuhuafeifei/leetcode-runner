package com.xhf.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.editors.TimeWidget;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;

public class TimeAction extends AbstractAction {
    @Override
    protected void doActionPerformed(Project project, AnActionEvent e) {
        if (true) {
            ConsoleUtils.getInstance(project).showInfo("功能开发中, 敬请期待...", true, true);
            return;
        }
        new TimeWidget(project).show();
    }
}
