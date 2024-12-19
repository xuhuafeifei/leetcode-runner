package com.xhf.leetcode.plugin.debug.reader;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;
import com.xhf.leetcode.plugin.utils.DataKeys;
import com.xhf.leetcode.plugin.window.LCConsoleWindowFactory;

/**
 * 从consoleView读取输入
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class ConsoleReader implements InstReader{
    private final Project project;
    private final ConsoleUtils console;

    public ConsoleReader(Project project) {
        this.project = project;
        this.console = ConsoleUtils.getInstance(project);
    }

    @Override
    public String readInst() {
        // 从阻塞队列中获取指令输入
        String cmd = console.consumeCmd();
        return cmd;
    }
}
