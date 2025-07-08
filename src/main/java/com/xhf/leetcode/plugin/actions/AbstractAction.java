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
import com.xhf.leetcode.plugin.utils.BundleUtils;
import com.xhf.leetcode.plugin.utils.DebugCheck;
import com.xhf.leetcode.plugin.utils.LogUtils;
import com.xhf.leetcode.plugin.utils.LoginPass;
import com.xhf.leetcode.plugin.utils.RatePass;
import com.xhf.leetcode.plugin.utils.SettingPass;
import javax.swing.Icon;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

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

    public AbstractAction(String s) {
        super(s);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        assert project != null;
        RatePass ratePass = this.getClass().getAnnotation(RatePass.class);
        if (ratePass == null) {
            if (!ActionUtils.get()) {
                ConsoleUtils.getInstance(e.getProject())
                    .showInfo(BundleUtils.i18n("action.leetcode.actions.frequency.info"), false, true);
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
            if (!login) {
                ConsoleUtils.getInstance(e.getProject())
                    .showWaring(BundleUtils.i18n("action.leetcode.actions.login.info"), false);
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
                Messages.showInfoMessage(
                    BundleUtils.i18nHelper(
                        "Debug设置模块 '读取类型' 没有设置, 请前往设置界面",
                        "Debug setting 'read type' is not set, please go to the setting interface"
                    ),
                    "INFO"
                );
                ShowSettingsUtil.getInstance().showSettingsDialog(project, "Leetcode Runner Setting");
                return;
            }
            // output 检测
            if (StringUtils.isBlank(appSettings.getOutputTypeName())) {
                Messages.showInfoMessage(
                    BundleUtils.i18nHelper(
                        "Debug设置模块 '输出类型' 没有设置, 请前往设置界面",
                        "Debug setting 'output type' is not set, please go to the setting interface"
                    ),
                    "INFO"
                );
                ShowSettingsUtil.getInstance().showSettingsDialog(project, "Leetcode Runner Setting");
                return;
            }
            // 状态检测
            DebugCheck.CheckType value = debugCheck.value();
            if (value == DebugCheck.CheckType.STATUS) {
                if (!DebugManager.getInstance(project).isDebug()) {
                    ConsoleUtils.getInstance(project)
                        .showWaring(BundleUtils.i18n("action.leetcode.actions.debug.nodebug"), false, true);
                    return;
                }
            }
        }
        try {
            doActionPerformed(project, e);
        } catch (Exception ex) {
            LogUtils.error(ex);
            ConsoleUtils.getInstance(project)
                .showError(BundleUtils.i18n("action.leetcode.unknown.error") + "\n" + ex.getMessage(), false, true);
        }
    }

    protected abstract void doActionPerformed(Project project, AnActionEvent e);
}
