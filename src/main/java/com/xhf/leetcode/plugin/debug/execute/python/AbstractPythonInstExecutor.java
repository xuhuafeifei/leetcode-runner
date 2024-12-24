package com.xhf.leetcode.plugin.debug.execute.python;

import com.xhf.leetcode.plugin.debug.execute.ExecuteContext;
import com.xhf.leetcode.plugin.debug.execute.ExecuteResult;
import com.xhf.leetcode.plugin.debug.execute.InstExecutor;
import com.xhf.leetcode.plugin.debug.instruction.Instruction;
import com.xhf.leetcode.plugin.exception.DebugError;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public abstract class AbstractPythonInstExecutor implements InstExecutor {

    @Override
    public ExecuteResult execute(Instruction inst, ExecuteContext context) {
        if (! (context instanceof PyContext) ) {
            throw new DebugError("context must be instance of com.xhf.leetcode.plugin.debug.execute.python.PyContext in python executor");
        }
        return doExecute(inst, (PyContext) context);
    }

    protected abstract ExecuteResult doExecute(Instruction inst, PyContext pCtx);
}
