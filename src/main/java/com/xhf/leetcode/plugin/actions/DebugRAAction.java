package com.xhf.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.debug.DebugManager;
import com.xhf.leetcode.plugin.debug.command.operation.Operation;
import com.xhf.leetcode.plugin.debug.instruction.Instruction;
import com.xhf.leetcode.plugin.debug.reader.InstSource;
import com.xhf.leetcode.plugin.debug.reader.ReadType;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;
import com.xhf.leetcode.plugin.utils.BundleUtils;
import com.xhf.leetcode.plugin.utils.LoginPass;
import com.xhf.leetcode.plugin.utils.RatePass;

/**
 * remove all break points
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
// @DebugCheck 允许在不处于debug状态下, 移除文件的所有断点
@LoginPass
@RatePass
public class DebugRAAction extends AbstractAction {
    @Override
    public void doActionPerformed(Project project, AnActionEvent e) {
        // 如果是处于debug状态, 写入指令
        if (DebugManager.getInstance(project).isDebug()) {
            // 写指令到阻塞队列中
            boolean flag = InstSource.uiInstInput(Instruction.success(ReadType.UI_IN, Operation.RBA, ""));
            if (! flag) {
                ConsoleUtils.getInstance(project).showError(BundleUtils.i18n("action.leetcode.actions.debug.command.inputerr"), true);
            }
        } else {
            DebugUtils.removeCurrentVFileAllBreakpoint(project);
        }
    }
}
