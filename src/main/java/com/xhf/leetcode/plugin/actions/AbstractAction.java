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
import com.xhf.leetcode.plugin.utils.DebugCheck;
import com.xhf.leetcode.plugin.utils.LoginPass;
import com.xhf.leetcode.plugin.utils.RatePass;
import com.xhf.leetcode.plugin.utils.SettingPass;
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
                ConsoleUtils.getInstance(e.getProject()).showInfo("����ǰ��������Ƶ��!", false, true);
                return;
            }
        }
        // settings check
        SettingPass settingPass = this.getClass().getAnnotation(SettingPass.class);
        if (settingPass == null) {
            AppSettings appSettings = AppSettings.getInstance();
            if (!appSettings.initOrNot()) {
                Messages.showInfoMessage("����ǰ�����ý������ò��...", "INFO");
                ShowSettingsUtil.getInstance().showSettingsDialog(project, "Leetcode Runner Setting");
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
                        "���ȵ�¼...",
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
            // ����������
            AppSettings appSettings = AppSettings.getInstance();
            // reader ���
            if (StringUtils.isBlank(appSettings.getReadTypeName())) {
                Messages.showInfoMessage("debug readerû������, ��ǰ�����ý���", "INFO");
                ShowSettingsUtil.getInstance().showSettingsDialog(project, "Leetcode Runner Setting");
                return;
            }
            // output ���
            if (StringUtils.isBlank(appSettings.getOutputTypeName())) {
                Messages.showInfoMessage("debug outputû������, ��ǰ�����ý���", "INFO");
                ShowSettingsUtil.getInstance().showSettingsDialog(project, "Leetcode Runner Setting");
                return;
            }
            // ״̬���
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

    protected abstract void doActionPerformed(Project project, AnActionEvent e);
}
