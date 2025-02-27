package com.xhf.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.actions.utils.ActionUtils;
import com.xhf.leetcode.plugin.utils.LoginPass;
import com.xhf.leetcode.plugin.utils.RatePass;
import com.xhf.leetcode.plugin.utils.SettingPass;

@SettingPass // 跳过设置检查
@LoginPass   // 跳过登录检查
@RatePass    // 跳过频率限制
public class TimerAction extends AbstractAction {

    @Override
    protected void doActionPerformed(Project project, AnActionEvent e) {
        assert project != null;

        ActionUtils.createTimerWindow().setVisible(true);
    }
}