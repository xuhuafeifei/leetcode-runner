package com.xhf.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.debug.debugger.JavaDebugConfig;
import com.xhf.leetcode.plugin.debug.debugger.JavaDebugger;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class DebugAction extends AbstractAction {
    @Override
    void doActionPerformed(Project project, AnActionEvent e) {
        JavaDebugConfig config = new JavaDebugConfig.Builder(project).autoBuild().build();
        JavaDebugger javaDebugger = new JavaDebugger(project, config);
        javaDebugger.start();
    }
}
