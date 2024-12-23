package com.xhf.leetcode.plugin.debug.execute;

import com.sun.jdi.request.BreakpointRequest;
import com.xhf.leetcode.plugin.debug.instruction.Instruction;

import java.util.List;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class JavaRBInst implements InstExecutor {
    @Override
    public ExecuteResult execute(Instruction inst, Context context) {
        int lineNumber = Integer.parseInt(inst.getParam());
        List<BreakpointRequest> breakpointRequests = context.getBreakpointRequests();
        for (BreakpointRequest breakpointRequest : breakpointRequests) {
            if (breakpointRequest.location().lineNumber() == lineNumber) {
                breakpointRequest.disable();
                ExecuteResult success = ExecuteResult.success(inst.getOperation(), "break point remove at line " + lineNumber);
                success.setAddLine(lineNumber);
                return success;
            }
        }
        return ExecuteResult.fail(inst.getOperation(), "break point at " + lineNumber + " haven't been set!");
    }
}
