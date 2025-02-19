package com.xhf.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.xhf.leetcode.plugin.window.LCConsoleWindowFactory;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * @author 文艺倾年
 * 老板键
 * Windows/Linux: Ctrl+Shift+.，Mac: Cmd+Shift+.
 */
public class BossKeyAction extends AbstractAction {
    private static boolean isHidden = false;

    public BossKeyAction() {
        // 注册快捷键
        registerCustomShortcutSet(
                new CustomShortcutSet(KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD,
                        InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK)),
                null
        );
    }

    @Override
    protected void doActionPerformed(Project project, AnActionEvent e) {
        if (project == null) return;

        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        ToolWindow toolWindow = toolWindowManager.getToolWindow(LCConsoleWindowFactory.LEETCODE_CONSOLE_PLUGIN_ID);

        if (toolWindow != null) {
            if (isHidden) {
                toolWindow.show();
            } else {
                toolWindow.hide();
            }
            isHidden = !isHidden;
        }
    }
}
