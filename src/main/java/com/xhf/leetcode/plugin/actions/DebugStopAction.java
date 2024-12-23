package com.xhf.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.debug.DebugManager;
import com.xhf.leetcode.plugin.debug.command.operation.Operation;
import com.xhf.leetcode.plugin.debug.env.AbstractDebugEnv;
import com.xhf.leetcode.plugin.debug.instruction.Instruction;
import com.xhf.leetcode.plugin.debug.reader.InstSource;
import com.xhf.leetcode.plugin.debug.reader.ReadType;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;
import com.xhf.leetcode.plugin.utils.DebugCheck;

/**
 * next
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
@DebugCheck
public class DebugStopAction extends AbstractAction {
    @Override
    void doActionPerformed(Project project, AnActionEvent e) {
        DebugManager.getInstance(project).getAnyDebugger().stop();
        ConsoleUtils.getInstance(project).showInfo("结束debug!", false, true);
    }
}
