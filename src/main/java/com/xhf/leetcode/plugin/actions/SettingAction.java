package com.xhf.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.utils.BundleUtils;
import com.xhf.leetcode.plugin.utils.LoginPass;
import com.xhf.leetcode.plugin.utils.SettingPass;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
@SettingPass
@LoginPass
public class SettingAction extends AbstractAction {

    public SettingAction() {
        super(BundleUtils.i18n("action.leetcode.plugin.Settings"));
    }

    @Override
    public void doActionPerformed(Project project, AnActionEvent e) {
        ShowSettingsUtil.getInstance().showSettingsDialog(project, "Leetcode Runner Setting");
    }

}
