package com.xhf.leetcode.plugin.debug.reader;

import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.debug.command.parser.InstParserImpl;
import com.xhf.leetcode.plugin.debug.instruction.Instruction;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import org.apache.commons.lang.StringUtils;

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
    public final Instruction readInst() throws InterruptedException {
        String res = readCommand();
        // 阻塞队列被打断时, 返回null
        if (res == null) {
            throw new InterruptedException("阻塞队列消费被打断");
        }
        DebugUtils.simpleDebug("command = " + res, project, ConsoleViewContentType.USER_INPUT);
        if (res == null || res.equals("stop") || res.equals("exit")) {
            return Instruction.quit(this.readType);
        }
        // 解析指令, 并执行
        Instruction parse = new InstParserImpl().parse(res, this.readType);
        if (parse == null) {
            return Instruction.error(this.readType);
        }
        return parse;
    }

    protected abstract String readCommand();
}
