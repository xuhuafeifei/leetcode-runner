package com.xhf.leetcode.plugin.debug.execute;

import com.xhf.leetcode.plugin.debug.instruction.Instruction;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class JavaRInst implements InstExecutor{

    @Override
    public ExecuteResult execute(Instruction inst, Context context) {
        context.removeStepRequest();
        return ExecuteResult.success(inst.getOperation());
    }
}
