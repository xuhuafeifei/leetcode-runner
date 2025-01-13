package com.xhf.leetcode.plugin.debug.output;

import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;

/**
 * 写入consoleView
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class ConsoleOutput extends IOOutput{

    private final ConsoleUtils console;

    public ConsoleOutput(Project project) {
        super(project);
        console = ConsoleUtils.getInstance(project);
    }

    @Override
    protected void outputTo(String output) {
        console.simpleShowConsole(output);
    }
}
