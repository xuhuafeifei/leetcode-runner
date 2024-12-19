package com.xhf.leetcode.plugin.debug.execute;

import com.sun.jdi.request.BreakpointRequest;
import com.xhf.leetcode.plugin.debug.params.Instrument;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class JavaRBAInst implements InstExecutor{
    @Override
    public ExecuteResult execute(Instrument inst, Context context) {
        for (BreakpointRequest breakpointRequest : context.getBreakpointRequests()) {
            breakpointRequest.disable();
        }
        return ExecuteResult.success("All breakpoint removed !");
    }
}
