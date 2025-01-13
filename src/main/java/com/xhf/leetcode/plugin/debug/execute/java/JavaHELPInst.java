package com.xhf.leetcode.plugin.debug.execute.java;

import com.xhf.leetcode.plugin.debug.execute.ExecuteResult;
import com.xhf.leetcode.plugin.debug.instruction.Instruction;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import com.xhf.leetcode.plugin.utils.Constants;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class JavaHELPInst extends AbstractJavaInstExecutor {

    @Override
    public ExecuteResult doExecute(Instruction inst, Context context) {
        ExecuteResult success = ExecuteResult.success(inst.getOperation(), Constants.HELP_INFO);
        DebugUtils.fillExecuteResultByLocation(success, context.getLocation());
        return success;
    }
}
