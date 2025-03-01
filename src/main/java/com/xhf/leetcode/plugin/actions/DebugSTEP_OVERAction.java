package com.xhf.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.debug.command.operation.Operation;
import com.xhf.leetcode.plugin.debug.instruction.Instruction;
import com.xhf.leetcode.plugin.debug.reader.InstSource;
import com.xhf.leetcode.plugin.debug.reader.ReadType;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;
import com.xhf.leetcode.plugin.utils.BundleUtils;
import com.xhf.leetcode.plugin.utils.DebugCheck;
import com.xhf.leetcode.plugin.utils.LoginPass;
import com.xhf.leetcode.plugin.utils.RatePass;

/**
 * step over
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
@DebugCheck(DebugCheck.CheckType.STATUS)
@LoginPass
@RatePass
public class DebugSTEP_OVERAction extends AbstractAction {
    @Override
    public void doActionPerformed(Project project, AnActionEvent e) {
        // 写指令到阻塞队列中
        boolean flag = InstSource.uiInstInput(Instruction.success(ReadType.UI_IN, Operation.STEP, "over"));
        if (! flag) {
            ConsoleUtils.getInstance(project).showError(BundleUtils.i18n("action.leetcode.actions.debug.command.inputerr"), true);
        }
        flag = InstSource.uiInstInput(Instruction.success(ReadType.UI_IN, Operation.P, ""));
        if (! flag) {
            ConsoleUtils.getInstance(project).showError(BundleUtils.i18n("action.leetcode.actions.debug.command.inputerr"), true);
        }
    }
}
