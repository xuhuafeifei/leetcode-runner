package com.xhf.leetcode.plugin.actions;

import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.debug.DebugManager;
import com.xhf.leetcode.plugin.debug.debugger.Debugger;
import com.xhf.leetcode.plugin.debug.debugger.JavaDebugConfig;
import com.xhf.leetcode.plugin.debug.debugger.JavaDebugger;
import com.xhf.leetcode.plugin.debug.debugger.PythonDebugger;
import com.xhf.leetcode.plugin.debug.env.AbstractDebugEnv;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;
import com.xhf.leetcode.plugin.io.console.utils.ConsoleDialog;
import com.xhf.leetcode.plugin.setting.AppSettings;
import com.xhf.leetcode.plugin.utils.LangType;
import com.xhf.leetcode.plugin.utils.LogUtils;

/**
 * 启动debug调试功能
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class DebugAction extends AbstractAction {
    @Override
    void doActionPerformed(Project project, AnActionEvent e) {
        if (DebugManager.getInstance(project).isDebug()) {
            ConsoleUtils.getInstance(project).showInfo("当前处于调试状态, 请先退出调试状态", false, true);
            return;
        }
        LangType langType = LangType.getType(AppSettings.getInstance().getLangType());
        if (langType == null) {
            LogUtils.error("异常, LangType == null " + AppSettings.getInstance().getLangType());
            return;
        }
        switch (langType) {
            case JAVA:
                doJavaDebug(project);
                break;
            case PYTHON3:
                doPythonDebug(project);
                break;
            default:
                ConsoleUtils.getInstance(project).showWaring("当前" + langType.getLangType() + "语言类型不支持调试", false, true);
        }
    }

    private void doPythonDebug(Project project) {
        Debugger debugger = DebugManager.getInstance(project).createDebugger(PythonDebugger.class);
        try {
            debugger.start();
        } catch (Exception e) {
            DebugUtils.simpleDebug("Debug Failed! " + e, project, ConsoleViewContentType.ERROR_OUTPUT, true);
        }
    }

    private void doJavaDebug(Project project) {
        Debugger debugger = DebugManager.getInstance(project).createDebugger(JavaDebugger.class);
        try {
            debugger.start();
        } catch (Exception e) {
            DebugUtils.simpleDebug("Debug Failed! " + e, project, ConsoleViewContentType.ERROR_OUTPUT, true);
        }
    }
}
