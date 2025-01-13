package com.xhf.leetcode.plugin.debug.execute.python;

import com.xhf.leetcode.plugin.debug.execute.ExecuteResult;
import com.xhf.leetcode.plugin.debug.instruction.Instruction;
import com.xhf.leetcode.plugin.utils.Constants;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class PythonHELPInst extends AbstractPythonInstExecutor {
    @Override
    protected ExecuteResult doExecute(Instruction inst, PyContext context) {
        return ExecuteResult.success(inst.getOperation(), Constants.HELP_INFO);
    }
}
