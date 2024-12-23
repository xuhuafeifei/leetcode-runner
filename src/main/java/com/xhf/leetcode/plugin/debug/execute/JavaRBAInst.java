package com.xhf.leetcode.plugin.debug.execute;

import com.sun.jdi.request.BreakpointRequest;
import com.xhf.leetcode.plugin.debug.instruction.Instruction;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class JavaRBAInst implements InstExecutor{
    @Override
    public ExecuteResult execute(Instruction inst, Context context) {
        for (BreakpointRequest breakpointRequest : context.getBreakpointRequests()) {
            breakpointRequest.disable();
        }
        return ExecuteResult.success(inst.getOperation(), "All breakpoint removed !");
    }
}
