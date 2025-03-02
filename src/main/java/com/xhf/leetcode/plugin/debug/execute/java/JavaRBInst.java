package com.xhf.leetcode.plugin.debug.execute.java;

import com.sun.jdi.request.BreakpointRequest;
import com.xhf.leetcode.plugin.debug.execute.ExecuteResult;
import com.xhf.leetcode.plugin.debug.instruction.Instruction;
import com.xhf.leetcode.plugin.utils.BundleUtils;

import java.util.List;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class JavaRBInst extends AbstractJavaInstExecutor {
    @Override
    public ExecuteResult doExecute(Instruction inst, Context context) {
        int lineNumber = Integer.parseInt(inst.getParam());
        List<BreakpointRequest> breakpointRequests = context.getBreakpointRequests();
        for (BreakpointRequest breakpointRequest : breakpointRequests) {
            if (breakpointRequest.location().lineNumber() == lineNumber) {
                breakpointRequest.disable();
                ExecuteResult success = ExecuteResult.success(inst.getOperation(), BundleUtils.i18nHelper("断点已在" + lineNumber + "行移除!", "break point remove at line " + lineNumber));
                success.setAddLine(lineNumber);
                return success;
            }
        }
        return ExecuteResult.fail(inst.getOperation(), BundleUtils.i18nHelper("断点在" + lineNumber + "行不存在!", "break point at " + lineNumber + " haven't been set!"));
    }
}
