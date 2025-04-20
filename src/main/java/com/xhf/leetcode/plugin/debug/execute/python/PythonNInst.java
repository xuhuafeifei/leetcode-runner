package com.xhf.leetcode.plugin.debug.execute.python;

import com.xhf.leetcode.plugin.debug.command.operation.Operation;
import com.xhf.leetcode.plugin.debug.execute.ExecuteResult;
import com.xhf.leetcode.plugin.debug.instruction.Instruction;
import com.xhf.leetcode.plugin.debug.reader.InstSource;
import com.xhf.leetcode.plugin.debug.reader.ReadType;
import com.xhf.leetcode.plugin.setting.AppSettings;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class PythonNInst extends AbstractPythonInstExecutor {
    @Override
    protected void doAfter(ExecuteResult r, PyContext pCtx) {
        super.doMoreInst(
            new Operation[]{Operation.W, Operation.P},
            new String[]{"w", "p"},
            pCtx.getReadType()
        );
    }
}
