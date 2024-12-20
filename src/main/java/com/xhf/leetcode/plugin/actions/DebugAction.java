package com.xhf.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.debug.debugger.JavaDebugConfig;
import com.xhf.leetcode.plugin.debug.debugger.JavaDebugger;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;
import com.xhf.leetcode.plugin.io.console.utils.ConsoleDialog;
import com.xhf.leetcode.plugin.setting.AppSettings;
import com.xhf.leetcode.plugin.utils.LangType;
import com.xhf.leetcode.plugin.utils.LogUtils;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class DebugAction extends AbstractAction {
    @Override
    void doActionPerformed(Project project, AnActionEvent e) {
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
                // 暂不支持
            default:
                ConsoleUtils.getInstance(project).showWaring("当前" + langType.getLangType() + "语言类型不支持调试", false, true);
        }
    }

    private void doJavaDebug(Project project) {
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
