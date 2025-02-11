package com.xhf.leetcode.plugin.actions;

import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.debug.DebugManager;
import com.xhf.leetcode.plugin.debug.debugger.CPPDebugger;
import com.xhf.leetcode.plugin.debug.debugger.Debugger;
import com.xhf.leetcode.plugin.debug.debugger.JavaDebugger;
import com.xhf.leetcode.plugin.debug.debugger.PythonDebugger;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;
import com.xhf.leetcode.plugin.service.CodeService;
import com.xhf.leetcode.plugin.setting.AppSettings;
import com.xhf.leetcode.plugin.utils.DebugCheck;
import com.xhf.leetcode.plugin.utils.LangType;
import com.xhf.leetcode.plugin.utils.LogUtils;
import com.xhf.leetcode.plugin.utils.LoginPass;

import javax.swing.*;

/**
 * 启动debug调试功能
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
@DebugCheck
@LoginPass
public class DebugAction extends AbstractAction {
    @Override
    public void doActionPerformed(Project project, AnActionEvent e) {
        if (DebugManager.getInstance(project).isDebug()) {
            ConsoleUtils.getInstance(project).showInfo("当前处于调试状态, 请先退出调试状态", false, true);
            return;
        }
        LangType langType = LangType.getType(AppSettings.getInstance().getLangType());
        if (langType == null) {
            LogUtils.error("异常, LangType == null " + AppSettings.getInstance().toString());
            return;
        }
        switch (langType) {
            case JAVA:
                doJavaDebug(project, langType);
                break;
            case PYTHON3:
                doPythonDebug(project, langType);
                break;
            case CPP:
                doCPPDebug(project, langType);
                break;
            default:
                ConsoleUtils.getInstance(project).showWaring("当前" + langType.getLangType() + "语言类型不支持调试", false, true);
        }
    }

    private void doCPPDebug(Project project, LangType langType) {
        if (! doCheck(project, langType)) {
            return;
        }
        Debugger debugger = DebugManager.getInstance(project).createDebugger(CPPDebugger.class);
        try {
            debugger.start();
        } catch (Exception e) {
            DebugUtils.simpleDebug("Debug Failed! " + e, project, ConsoleViewContentType.ERROR_OUTPUT, true);
        }
    }

    private void doPythonDebug(Project project, LangType langType) {
        if (! doCheck(project, langType)) {
            return;
        }
        Debugger debugger = DebugManager.getInstance(project).createDebugger(PythonDebugger.class);
        try {
            debugger.start();
        } catch (Exception e) {
            DebugUtils.simpleDebug("Debug Failed! " + e, project, ConsoleViewContentType.ERROR_OUTPUT, true);
        }
    }

    private void doJavaDebug(Project project, LangType langType) {
        if (!doCheck(project, langType)) {
            return;
        }
        Debugger debugger = DebugManager.getInstance(project).createDebugger(JavaDebugger.class);
        try {
            debugger.start();
        } catch (Exception e) {
            DebugUtils.simpleDebug("Debug Failed! " + e, project, ConsoleViewContentType.ERROR_OUTPUT, true);
        }
    }

    private boolean doCheck(Project project, LangType langType) {
        // 通过文件名获取语言类型
        String langFromFile = CodeService.getInstance(project).parseLangTypeFromCVFile(project);
        if (LangType.getType(langFromFile) != langType) {
            LogUtils.warn("异常, LangType != langFromFile " + langType + " != " + langFromFile);
            JOptionPane.showMessageDialog(null, "当前文件代表语言类型与设置的语言类型类型不一致, 请重新选择代码文件\n"
                    + "当前文件语言类型 = " + langFromFile + "\n"
                    + "设置语言类型 = " + langType.getLangType()
            );
            return false;
        }
        return true;
    }
}
