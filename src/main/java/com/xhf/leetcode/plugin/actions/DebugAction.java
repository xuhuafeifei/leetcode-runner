package com.xhf.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.debug.debugger.JavaDebugConfig;
import com.xhf.leetcode.plugin.debug.debugger.JavaDebugger;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;
import com.xhf.leetcode.plugin.io.console.utils.ConsoleDialog;
import com.xhf.leetcode.plugin.utils.LogUtils;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class DebugAction extends AbstractAction {
    @Override
    void doActionPerformed(Project project, AnActionEvent e) {
        JavaDebugConfig config = null;
        try {
            config = new JavaDebugConfig.Builder(project).autoBuild().build();
        } catch (Exception ex) {
            ConsoleUtils.getInstance(project).showError(ex.toString(), true, true, ex.toString(), "Java环境配置创建异常!", ConsoleDialog.ERROR);
            LogUtils.error(ex);
            return;
        }
        JavaDebugger javaDebugger = new JavaDebugger(project, config);
        javaDebugger.start();
    }
}
