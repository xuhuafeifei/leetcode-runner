package com.xhf.leetcode.plugin.debug.command.parser;

import com.xhf.leetcode.plugin.debug.instruction.Instruction;
import com.xhf.leetcode.plugin.debug.reader.ReadType;

/**
 * 指令解析器, 读取命令并解析为指令
 */
public interface InstParser {

    /**
     * 解析指令
     *
     * @param command 命令
     * @param readType 命令来源
     * @return 解析对象
     */
    Instruction parse(String command, ReadType readType) throws IllegalArgumentException;
}
