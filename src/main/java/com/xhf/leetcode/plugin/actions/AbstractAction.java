package com.xhf.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.xhf.leetcode.plugin.debug.DebugManager;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;
import com.xhf.leetcode.plugin.service.LoginService;
import com.xhf.leetcode.plugin.setting.AppSettings;
import com.xhf.leetcode.plugin.utils.DebugCheck;
import com.xhf.leetcode.plugin.utils.LoginPass;
import com.xhf.leetcode.plugin.utils.SettingPass;
import org.apache.commons.lang3.StringUtils;
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
                Messages.showInfoMessage("请先前往设置界面设置插件...", "INFO");
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
            // 必须检测设置
            AppSettings appSettings = AppSettings.getInstance();
            // reader 检测
            if (StringUtils.isBlank(appSettings.getReadTypeName())) {
                Messages.showInfoMessage("debug reader没有设置, 请前往设置界面", "INFO");
                ShowSettingsUtil.getInstance().showSettingsDialog(project, "Leetcode Setting");
                return;
            }
            // output 检测
            if (StringUtils.isBlank(appSettings.getOutputTypeName())) {
                Messages.showInfoMessage("debug output没有设置, 请前往设置界面", "INFO");
                ShowSettingsUtil.getInstance().showSettingsDialog(project, "Leetcode Setting");
                return;
            }
            // 状态检测
            DebugCheck.CheckType value = debugCheck.value();
            if (value == DebugCheck.CheckType.STATUS) {
                if (!DebugManager.getInstance(project).isDebug()) {
                    ConsoleUtils.getInstance(project).showWaring("no debug happen", false, true);
                    return;
                }
            }
        }
        doActionPerformed(project, e);
    }

    abstract void doActionPerformed(Project project, AnActionEvent e);
}
