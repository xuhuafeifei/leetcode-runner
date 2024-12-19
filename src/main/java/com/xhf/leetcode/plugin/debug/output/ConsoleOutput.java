package com.xhf.leetcode.plugin.debug.output;

import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;

/**
 * 写入consoleView
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class ConsoleOutput implements Output{

    public final Project project;

    private final ConsoleUtils console;

    public ConsoleOutput(Project project) {
        this.project = project;
        console = ConsoleUtils.getInstance(project);
    }

    @Override
    public void output(String output) {
        // 检测是否末尾是换行
        if (!output.endsWith("\n")) {
            output += "\n";
        }
        console.simpleShowConsole(output);
    }
}
