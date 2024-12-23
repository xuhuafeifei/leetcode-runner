package com.xhf.leetcode.plugin.debug.reader;

import com.intellij.openapi.project.Project;

/**
 * 从command命令行读取数据, 并解析为指令
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class CommandLineReader extends CommandReader{

    public CommandLineReader(Project project) {
        super(project, ReadType.COMMAND_IN);
    }

    @Override
    protected String readCommand() {
        return InstSource.consumeCmd();
    }
}
