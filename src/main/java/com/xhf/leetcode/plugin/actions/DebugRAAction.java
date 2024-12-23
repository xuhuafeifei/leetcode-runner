package com.xhf.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.debug.env.AbstractDebugEnv;
import com.xhf.leetcode.plugin.debug.instruction.Instruction;
import com.xhf.leetcode.plugin.debug.command.operation.Operation;
import com.xhf.leetcode.plugin.debug.reader.InstSource;
import com.xhf.leetcode.plugin.debug.reader.ReadType;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;

/**
 * remove all break points
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
// @DebugCheck 允许在不处于debug状态下, 移除文件的所有断点
public class DebugRAAction extends AbstractAction {
    @Override
    void doActionPerformed(Project project, AnActionEvent e) {
        // 如果是处于debug状态, 写入指令
        if (AbstractDebugEnv.isDebug()) {
            // 写指令到阻塞队列中
            boolean flag = InstSource.uiInstInput(Instruction.success(ReadType.UI_IN, Operation.RBA, ""));
            if (! flag) {
                ConsoleUtils.getInstance(project).showError("指令输入失败 ", true);
            }
        } else {
            DebugUtils.removeCurrentVFileAllBreakpoint(project);
        }
    }
}
