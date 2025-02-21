package com.xhf.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.xhf.leetcode.plugin.utils.LogUtils;
import com.xhf.leetcode.plugin.utils.LoginPass;
import com.xhf.leetcode.plugin.utils.RatePass;
import com.xhf.leetcode.plugin.utils.SettingPass;
import com.xhf.leetcode.plugin.window.LCConsoleWindowFactory;
import com.xhf.leetcode.plugin.window.LCToolWindowFactory;

/**
 * @author 文艺倾年
 * 老板键
 * Windows/Linux: Ctrl+Shift+.，Mac: Cmd+Shift+.
 */
@SettingPass // 跳过设置检查
@LoginPass   // 跳过登录检查
@RatePass    // 跳过频率限制
public class BossKeyAction extends AbstractAction {

    @Override
    protected void doActionPerformed(Project project, AnActionEvent e) {
        assert project != null;
        ToolWindowManager manager = ToolWindowManager.getInstance(project);
        toggleToolWindow(manager, LCConsoleWindowFactory.LEETCODE_CONSOLE_PLUGIN_ID);
        toggleToolWindow(manager, LCToolWindowFactory.LEETCODE_RUNNER_ID);
    }

    private void toggleToolWindow(ToolWindowManager manager, String windowId) {
        try {
            ToolWindow window = manager.getToolWindow(windowId);
            if (window == null) {
                return;
            }
            if (window.isVisible()) {
                window.hide();
            } else {
                window.show(() -> window.activate(null));
            }
        } catch (Exception ex) {
            LogUtils.error("Toggle operation failed for: " + windowId, ex);
        }
    }
}
