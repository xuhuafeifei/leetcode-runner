package com.xhf.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.xhf.leetcode.plugin.actions.utils.ActionUtils;
import com.xhf.leetcode.plugin.debug.DebugManager;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;
import com.xhf.leetcode.plugin.service.LoginService;
import com.xhf.leetcode.plugin.setting.AppSettings;
import com.xhf.leetcode.plugin.utils.*;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public abstract class AbstractAction extends AnAction {
    public AbstractAction(String s, String s1, Icon icon) {
        super(s, s1, icon);
    }

    public AbstractAction() {
        super();
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        assert project != null;
        RatePass ratePass = this.getClass().getAnnotation(RatePass.class);
        if (ratePass == null) {
            if (!ActionUtils.get()) {
                ConsoleUtils.getInstance(e.getProject()).showInfo(BundleUtils.i18n("action.leetcode.actions.frequency.info"), false, true);
                return;
            }
        }
        // settings check
        SettingPass settingPass = this.getClass().getAnnotation(SettingPass.class);
        if (settingPass == null) {
            AppSettings appSettings = AppSettings.getInstance();
            if (!appSettings.initOrNot()) {
                Messages.showInfoMessage(BundleUtils.i18n("action.leetcode.actions.setting.info"), "INFO");
                ShowSettingsUtil.getInstance().showSettingsDialog(project, "Leetcode Runner Setting");
                return;
            }
        }
        // login check
        LoginPass annotation = this.getClass().getAnnotation(LoginPass.class);
        if (annotation == null) {
            boolean login = LoginService.getInstance(project).isLogin();
            if (! login) {
                ConsoleUtils.getInstance(e.getProject()).showWaring(BundleUtils.i18n("action.leetcode.actions.login.info"), false);
                // LoginService.getInstance(project).doLogin();
                Messages.showOkCancelDialog(
                        project,
                        BundleUtils.i18n("action.leetcode.actions.login.info"),
                        "INFO",
                        Messages.getOkButton(),
                        Messages.getCancelButton(),
                        Messages.getQuestionIcon()
                );
                return;
            }
        }
        // debug check
        DebugCheck debugCheck = this.getClass().getAnnotation(DebugCheck.class);
        if (debugCheck != null) {
            // 必须检测设置
            AppSettings appSettings = AppSettings.getInstance();
            // reader 检测
            if (StringUtils.isBlank(appSettings.getReadTypeName())) {
                Messages.showInfoMessage(BundleUtils.i18n("action.leetcode.actions.debug.reader"), "INFO");
                ShowSettingsUtil.getInstance().showSettingsDialog(project, "Leetcode Runner Setting");
                return;
            }
            // output 检测
            if (StringUtils.isBlank(appSettings.getOutputTypeName())) {
                Messages.showInfoMessage(BundleUtils.i18n("action.leetcode.actions.debug.output"), "INFO");
                ShowSettingsUtil.getInstance().showSettingsDialog(project, "Leetcode Runner Setting");
                return;
            }
            // 状态检测
            DebugCheck.CheckType value = debugCheck.value();
            if (value == DebugCheck.CheckType.STATUS) {
                if (!DebugManager.getInstance(project).isDebug()) {
                    ConsoleUtils.getInstance(project).showWaring(BundleUtils.i18n("action.leetcode.actions.debug.nodebug"), false, true);
                    return;
                }
            }
        }
        doActionPerformed(project, e);
    }

    protected abstract void doActionPerformed(Project project, AnActionEvent e);
}
