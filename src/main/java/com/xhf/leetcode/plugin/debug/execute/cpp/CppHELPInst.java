package com.xhf.leetcode.plugin.debug.execute.cpp;

import com.xhf.leetcode.plugin.debug.execute.ExecuteResult;
import com.xhf.leetcode.plugin.debug.instruction.Instruction;
import com.xhf.leetcode.plugin.utils.Constants;
import org.jetbrains.annotations.NotNull;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class CppHELPInst extends AbstractCppInstExecutor {

    @Override
    protected ExecuteResult doExecute(Instruction inst, CppContext pCtx, String gdbCommand) {
        return ExecuteResult.success(inst.getOperation(), Constants.HELP_INFO);

    }

    @Override
    protected String getGdbCommand(@NotNull Instruction inst, CppContext pCtx) {
        return null;
    }
}
