package com.xhf.leetcode.plugin.debug.reader;

import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.debug.params.InstParserImpl;
import com.xhf.leetcode.plugin.debug.params.Instrument;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;

/**
 * 命令读取器, 读取命令并解析为指令
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public abstract class CommandReader extends AbstractInstReader{
    public CommandReader(Project project, ReadType readType) {
        super(project, readType);
    }

    @Override
    public final Instrument readInst() {
        String res = readCommand();
        DebugUtils.simpleDebug("command = " + res, project, ConsoleViewContentType.USER_INPUT);
        if (res == null || res.equals("bk") || res.equals("exit")) {
            return Instrument.quit(this.readType);
        }
        // 解析指令, 并执行
        Instrument parse = new InstParserImpl().parse(res, this.readType);
        if (parse == null) {
            return Instrument.error(this.readType);
        }
        return parse;
    }

    protected abstract String readCommand();
}
