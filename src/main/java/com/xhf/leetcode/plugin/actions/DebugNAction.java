package com.xhf.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.debug.params.Instrument;
import com.xhf.leetcode.plugin.debug.params.Operation;
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
public class DebugNAction extends AbstractAction {
    @Override
    void doActionPerformed(Project project, AnActionEvent e) {
        // 写指令到阻塞队列中
        boolean flag = InstSource.uiInstInput(Instrument.success(ReadType.UI_IN, Operation.N, "1"));
        if (! flag) {
            ConsoleUtils.getInstance(project).showError("指令输入失败 ", true);
        }
        flag = InstSource.uiInstInput(Instrument.success(ReadType.UI_IN, Operation.P, ""));
        if (! flag) {
            ConsoleUtils.getInstance(project).showError("指令输入失败 ", true);
        }
    }
}
