package com.xhf.leetcode.plugin.debug.execute.java;

import com.sun.jdi.request.BreakpointRequest;
import com.xhf.leetcode.plugin.debug.execute.ExecuteResult;
import com.xhf.leetcode.plugin.debug.instruction.Instruction;
import com.xhf.leetcode.plugin.utils.BundleUtils;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class JavaRBAInst extends AbstractJavaInstExecutor {

    @Override
    public ExecuteResult doExecute(Instruction inst, Context context) {
        for (BreakpointRequest breakpointRequest : context.getBreakpointRequests()) {
            breakpointRequest.disable();
        }
        return ExecuteResult.success(inst.getOperation(),
            BundleUtils.i18nHelper("所有断点已移除", "All breakpoint removed !"));
    }
}
