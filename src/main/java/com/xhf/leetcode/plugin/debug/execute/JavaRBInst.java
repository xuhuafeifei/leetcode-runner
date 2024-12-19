package com.xhf.leetcode.plugin.debug.execute;

import com.sun.jdi.request.BreakpointRequest;
import com.xhf.leetcode.plugin.debug.params.Instrument;

import java.util.List;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class JavaRBInst implements InstExecutor {
    @Override
    public ExecuteResult execute(Instrument inst, Context context) {
        int lineNumber = Integer.parseInt(inst.getParam());
        List<BreakpointRequest> breakpointRequests = context.getBreakpointRequests();
        for (BreakpointRequest breakpointRequest : breakpointRequests) {
            if (breakpointRequest.location().lineNumber() == lineNumber) {
                breakpointRequest.disable();
                return ExecuteResult.success("break point remove at line " + lineNumber);
            }
        }
        return ExecuteResult.fail("break point at " + lineNumber + " haven't been set!");
    }
}
