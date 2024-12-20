package com.xhf.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.xhf.leetcode.plugin.debug.env.AbstractDebugEnv;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;
import com.xhf.leetcode.plugin.io.console.utils.ConsoleDialog;
import com.xhf.leetcode.plugin.service.LoginService;
import com.xhf.leetcode.plugin.setting.AppSettings;
import com.xhf.leetcode.plugin.utils.DebugCheck;
import com.xhf.leetcode.plugin.utils.LoginPass;
import com.xhf.leetcode.plugin.utils.SettingPass;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public abstract class AbstractAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        assert project != null;
        // settings check
        SettingPass settingPass = this.getClass().getAnnotation(SettingPass.class);
        if (settingPass == null) {
            AppSettings appSettings = AppSettings.getInstance();
            if (!appSettings.initOrNot()) {
                ConsoleUtils.getInstance(project).showWaring("please init plugin setting", false, false, "Please init Setting", null, ConsoleDialog.WARNING);
                ShowSettingsUtil.getInstance().showSettingsDialog(project, "Leetcode Setting");
                return;
            }
        }
        // login check
        LoginPass annotation = this.getClass().getAnnotation(LoginPass.class);
        if (annotation == null) {
            boolean login = LoginService.getInstance(project).isLogin();
            if (! login) {
                ConsoleUtils.getInstance(e.getProject()).showWaring("not login!", false);
                // LoginService.getInstance(project).doLogin();
                Messages.showOkCancelDialog(
                        project,
                        "Please login first...",
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
            if (!AbstractDebugEnv.isDebug()) {
                ConsoleUtils.getInstance(project).showWaring("no debug happen", false, true);
                return;
            }
            AppSettings appSettings = AppSettings.getInstance();
            // reader 检测
            if (StringUtils.isBlank(appSettings.getReadTypeName())) {
                ConsoleUtils.getInstance(project).showWaring("debug reader没有设置, 请前往设置界面", false, true);
                ShowSettingsUtil.getInstance().showSettingsDialog(project, "Leetcode Setting");
                return;
            }
            // output 检测
            if (StringUtils.isBlank(appSettings.getOutputTypeName())) {
                ConsoleUtils.getInstance(project).showWaring("debug output没有设置, 请前往设置界面", false, true);
                ShowSettingsUtil.getInstance().showSettingsDialog(project, "Leetcode Setting");
                return;
            }
        }
        doActionPerformed(project, e);
    }

    abstract void doActionPerformed(Project project, AnActionEvent e);
}
