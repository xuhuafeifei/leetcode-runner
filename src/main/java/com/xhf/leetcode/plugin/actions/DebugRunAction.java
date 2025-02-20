package com.xhf.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.debug.command.operation.Operation;
import com.xhf.leetcode.plugin.debug.instruction.Instruction;
import com.xhf.leetcode.plugin.debug.reader.InstSource;
import com.xhf.leetcode.plugin.debug.reader.ReadType;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;
import com.xhf.leetcode.plugin.utils.DebugCheck;
import com.xhf.leetcode.plugin.utils.LoginPass;
import com.xhf.leetcode.plugin.utils.RatePass;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
@DebugCheck(DebugCheck.CheckType.STATUS)
@LoginPass
@RatePass
public class DebugRunAction extends AbstractAction {
    @Override
    public void doActionPerformed(Project project, AnActionEvent e) {
        // дָ�����������
        boolean flag = InstSource.uiInstInput(Instruction.success(ReadType.UI_IN, Operation.R, ""));
        if (! flag) {
            ConsoleUtils.getInstance(project).showError("ָ������ʧ�� ", true);
        }
    }
}
