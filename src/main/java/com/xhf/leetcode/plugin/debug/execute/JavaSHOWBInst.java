package com.xhf.leetcode.plugin.debug.execute;

import com.sun.jdi.Location;
import com.sun.jdi.request.BreakpointRequest;
import com.xhf.leetcode.plugin.debug.params.Instrument;

import java.util.List;

/**
 * show b: 显示所有断点
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class JavaSHOWBInst implements InstExecutor{
    @Override
    public ExecuteResult execute(Instrument inst, Context context) {
        List<BreakpointRequest> breakpointRequests = context.getBreakpointRequests();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < breakpointRequests.size(); i++) {
            BreakpointRequest breakpointRequest = breakpointRequests.get(i);
            Location location = breakpointRequest.location();
            String className = location.declaringType().name();
            String methodName = location.method().name();
            int lineNumber = location.lineNumber();
            sb.append("Breakpoint at ").append(className).append(".").append(methodName).append(" line ").append(lineNumber);
            if (i != breakpointRequests.size() - 1) {
                sb.append("\n");
            }
        }
        return ExecuteResult.success(sb.toString());
    }
}
