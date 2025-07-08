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
import com.xhf.leetcode.plugin.utils.BundleUtils;
import com.xhf.leetcode.plugin.utils.DebugCheck;
import com.xhf.leetcode.plugin.utils.LangType;
import com.xhf.leetcode.plugin.utils.LogUtils;
import com.xhf.leetcode.plugin.utils.LoginPass;
import com.xhf.leetcode.plugin.utils.OSHandler;
import com.xhf.leetcode.plugin.utils.RatePass;

/**
 * 启动debug调试功能
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
@DebugCheck
@LoginPass
@RatePass
public class DebugAction extends AbstractAction {

    public DebugAction() {
        super(BundleUtils.i18n("action.leetcode.plugin.debug.Debug"));
    }

    @Override
    public void doActionPerformed(Project project, AnActionEvent e) {
        if (DebugManager.getInstance(project).isDebug()) {
            ConsoleUtils.getInstance(project)
                .showInfo(BundleUtils.i18n("action.leetcode.actions.debug.quit"), false, true);
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
                // 别把空格删除了, 否则不好看
                ConsoleUtils.getInstance(project).showWaring(
                    langType.getLangType() + " " + BundleUtils.i18n("action.leetcode.actions.debug.notsupport"), false,
                    true);
        }
    }


    private void doCPPDebug(Project project, LangType langType) {
        if (!doCheck(project, langType)) {
            return;
        }
        // mac操作系统不支持cpp的调试, 因为他无法兼容gdb（操蛋的mac对gdb有诸多限制, 且lldb又找不到输出结构化数据的方式, 放弃了，毁灭吧）
        if (OSHandler.isMac()) {
            ConsoleUtils.getInstance(project).showWaring(
                BundleUtils.i18nHelper(
                    "暂不支持mac操作系统的cpp调试, 因为他对gdb调试存在诸多限制且lldb又找不到输出结构化数据的方式, 放弃了, 毁灭吧",
                    "not support mac os cpp debug, because it has many limitations of gdb and lldb can't find a way to output structured data, give up, destroy it"),
                false, true
            );
            return;
        }
        Debugger debugger = DebugManager.getInstance(project).createDebugger(CPPDebugger.class);
        try {
            debugger.start();
        } catch (Exception e) {
            DebugUtils.simpleDebug(BundleUtils.i18n("action.leetcode.actions.debug.failed") + " " + e, project,
                ConsoleViewContentType.ERROR_OUTPUT, true);
        }
    }

    private void doPythonDebug(Project project, LangType langType) {
        if (!doCheck(project, langType)) {
            return;
        }
        Debugger debugger = DebugManager.getInstance(project).createDebugger(PythonDebugger.class);
        try {
            debugger.start();
        } catch (Exception e) {
            DebugUtils.simpleDebug(BundleUtils.i18n("action.leetcode.actions.debug.failed") + " " + e, project,
                ConsoleViewContentType.ERROR_OUTPUT, true);
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
            DebugUtils.simpleDebug(BundleUtils.i18n("action.leetcode.actions.debug.failed") + " " + e, project,
                ConsoleViewContentType.ERROR_OUTPUT, true);
        }
    }

    private boolean doCheck(Project project, LangType langType) {
        // 通过文件名获取语言类型
        String langFromFile = CodeService.getInstance(project).parseLangTypeFromCVFile(project);
        if (LangType.getType(langFromFile) != langType) {
            LogUtils.warn(
                BundleUtils.i18n("action.leetcode.plugin.error") + ", LangType != langFromFile " + langType + " != "
                    + langFromFile);
            ConsoleUtils.getInstance(project).showWaring(
                BundleUtils.i18n("action.leetcode.actions.debug.langtype.not.equal") + "\n"
                    + BundleUtils.i18n("action.leetcode.actions.debug.langtype.file.type") + " = " + langFromFile + "\n"
                    + BundleUtils.i18n("action.leetcode.actions.debug.langtype.setting.type") + " = "
                    + langType.getLangType()
                , false, true
            );
            return false;
        }
        return true;
    }
}
