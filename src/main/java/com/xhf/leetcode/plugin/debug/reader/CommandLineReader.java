package com.xhf.leetcode.plugin.debug.reader;

import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.debug.params.InstParserImpl;
import com.xhf.leetcode.plugin.debug.params.Instrument;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;

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
