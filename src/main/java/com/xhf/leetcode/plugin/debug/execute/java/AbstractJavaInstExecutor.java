package com.xhf.leetcode.plugin.debug.execute.java;

import com.xhf.leetcode.plugin.debug.execute.ExecuteContext;
import com.xhf.leetcode.plugin.debug.execute.ExecuteResult;
import com.xhf.leetcode.plugin.debug.execute.InstExecutor;
import com.xhf.leetcode.plugin.debug.execute.java.Context;
import com.xhf.leetcode.plugin.debug.instruction.Instruction;
import com.xhf.leetcode.plugin.exception.DebugError;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public abstract class AbstractJavaInstExecutor implements InstExecutor {
    @Override
    public ExecuteResult execute(Instruction inst, ExecuteContext context) {
        if (! (context instanceof Context) ) {
            throw new DebugError("context must be instance of com.xhf.leetcode.plugin.debug.execute.java.Context in java executor");
        }
        return doExecute(inst, (Context) context);
    }

    protected abstract ExecuteResult doExecute(Instruction inst, Context context);
}
