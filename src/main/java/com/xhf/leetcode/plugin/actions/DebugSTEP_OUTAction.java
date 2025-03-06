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

/**
 * step out
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
@DebugCheck(DebugCheck.CheckType.STATUS)
@LoginPass
//@RatePass
public class DebugSTEP_OUTAction extends AbstractAction {

    public DebugSTEP_OUTAction() {
        super(BundleUtils.i18n("action.leetcode.plugin.console.DebugSTEP_OUTAction"));
    }

    @Override
    public void doActionPerformed(Project project, AnActionEvent e) {
        // 写指令到阻塞队列中
        boolean flag = InstSource.uiInstInput(Instruction.success(ReadType.UI_IN, Operation.STEP, "out"));
        if (!flag) {
            ConsoleUtils.getInstance(project).showError(BundleUtils.i18n("action.leetcode.actions.debug.command.inputerr"), true);
        }
        flag = InstSource.uiInstInput(Instruction.success(ReadType.UI_IN, Operation.P, ""));
        if (!flag) {
            ConsoleUtils.getInstance(project).showError(BundleUtils.i18n("action.leetcode.actions.debug.command.inputerr"), true);
        }
    }

}