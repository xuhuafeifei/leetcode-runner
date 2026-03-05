package com.xhf.leetcode.plugin.debug.execute;

import com.xhf.leetcode.plugin.debug.instruction.Instruction;

/**
 * 指令执行器
 */
public interface InstExecutor {

    /**
     * 执行指令
     *
     * @param inst 指令
     * @param context 指令执行时候的上下文
     * @return 执行结果
     */
    ExecuteResult execute(Instruction inst, ExecuteContext context);
}
