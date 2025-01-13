package com.xhf.leetcode.plugin.debug.execute.java;

import com.xhf.leetcode.plugin.debug.command.operation.Operation;
import com.xhf.leetcode.plugin.debug.execute.ExecuteContext;
import com.xhf.leetcode.plugin.debug.execute.ExecuteResult;
import com.xhf.leetcode.plugin.debug.execute.InstExecutor;
import com.xhf.leetcode.plugin.debug.instruction.Instruction;
import com.xhf.leetcode.plugin.exception.DebugError;
import com.xhf.leetcode.plugin.utils.LogUtils;

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
        Context ctx = (Context) context;
        if (ctx.getProject() == null) {
            LogUtils.simpleDebug("测试模式, 不检查核心数据");
            return doExecute(inst, ctx);
        }
        // 如果context核心变量为null, 则恢复vm
        if (ctx.getLocation() == null || ctx.getThread() == null || ctx.getSolutionLocation() == null) {
            ctx.getVm().resume();
            return ExecuteResult.success(Operation.NULL, "ctx核心数据为Location或Thread为NULL, resume targetVM." +
                    "\nLocation = " + ctx.getLocation() + " Thread = " + ctx.getThread());
        }
        return doExecute(inst, ctx);
    }

    protected abstract ExecuteResult doExecute(Instruction inst, Context context);
}
