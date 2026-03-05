package com.xhf.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.debug.DebugManager;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;
import com.xhf.leetcode.plugin.utils.BundleUtils;
import com.xhf.leetcode.plugin.utils.DebugCheck;
import com.xhf.leetcode.plugin.utils.LoginPass;

/**
 * next
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
@DebugCheck(DebugCheck.CheckType.STATUS)
@LoginPass
//@RatePass
public class DebugStopAction extends AbstractAction {

    public DebugStopAction() {
        super(BundleUtils.i18n("action.leetcode.plugin.console.DebugStopAction"));
    }

    @Override
    public void doActionPerformed(Project project, AnActionEvent e) {
        DebugManager.getInstance(project).stopDebugger();
        ConsoleUtils.getInstance(project)
            .simpleShowConsole(BundleUtils.i18n("action.leetcode.actions.debug.stop") + "\n");
    }

}