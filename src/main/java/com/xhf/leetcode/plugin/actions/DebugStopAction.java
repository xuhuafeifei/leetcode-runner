package com.xhf.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.debug.DebugManager;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;
import com.xhf.leetcode.plugin.utils.DebugCheck;
import com.xhf.leetcode.plugin.utils.LoginPass;
import com.xhf.leetcode.plugin.utils.RatePass;

/**
 * next
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
@DebugCheck(DebugCheck.CheckType.STATUS)
@LoginPass
@RatePass
public class DebugStopAction extends AbstractAction {
    @Override
    public void doActionPerformed(Project project, AnActionEvent e) {
        DebugManager.getInstance(project).stopDebugger();
        ConsoleUtils.getInstance(project).simpleShowConsole("结束debug!\n");
    }
}
