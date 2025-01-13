package com.xhf.leetcode.plugin.debug.execute;

import com.xhf.leetcode.plugin.debug.instruction.Instruction;

/**
 * 指令执行工厂, 创建执行不同指令的执行器
 */
public interface InstructionFactory {
    InstExecutor create(Instruction inst);
}
