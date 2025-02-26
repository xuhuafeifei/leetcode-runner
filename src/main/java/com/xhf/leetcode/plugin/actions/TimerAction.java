package com.xhf.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

import com.xhf.leetcode.plugin.utils.LoginPass;
import com.xhf.leetcode.plugin.utils.RatePass;
import com.xhf.leetcode.plugin.utils.SettingPass;
import com.xhf.leetcode.plugin.window.TimerWindow;

import java.awt.*;

@SettingPass // 跳过设置检查
@LoginPass   // 跳过登录检查
@RatePass    // 跳过频率限制
public class TimerAction extends AbstractAction {

    private static TimerWindow windowInstance;
    @Override
    protected void doActionPerformed(Project project, AnActionEvent e) {
        assert project != null;

        if (windowInstance == null || !windowInstance.isVisible()) {
            windowInstance = new TimerWindow(project);
            windowInstance.setVisible(true);
        } else {
            windowInstance.dispose();
            windowInstance = null;
        }

    }
}
