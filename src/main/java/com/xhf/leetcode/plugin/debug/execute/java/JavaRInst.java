package com.xhf.leetcode.plugin.debug.execute.java;

import com.xhf.leetcode.plugin.debug.command.operation.Operation;
import com.xhf.leetcode.plugin.debug.execute.ExecuteResult;
import com.xhf.leetcode.plugin.debug.instruction.Instruction;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class JavaRInst extends AbstractJavaInstExecutor {

    @Override
    public ExecuteResult doExecute(Instruction inst, Context context) {
        context.removeStepRequest();
        context.getVm().resume();
        super.doMoreInst(
            new Operation[]{Operation.W, Operation.P},
            new String[]{"w", "p"},
            inst.getReadType()
        );
        return ExecuteResult.success(inst.getOperation());
    }
}
