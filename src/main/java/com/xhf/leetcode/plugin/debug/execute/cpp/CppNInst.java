package com.xhf.leetcode.plugin.debug.execute.cpp;

import com.xhf.leetcode.plugin.debug.command.operation.Operation;
import com.xhf.leetcode.plugin.debug.execute.ExecuteResult;
import com.xhf.leetcode.plugin.debug.instruction.Instruction;
import org.jetbrains.annotations.NotNull;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class CppNInst extends AbstractCppInstExecutor {

    @Override
    protected String getGdbCommand(@NotNull Instruction inst, CppContext pCtx) {
        return "-exec-step";
    }

    @Override
    protected void doAfter(ExecuteResult r, CppContext pCtx) {
        // 这么设置的原因可以参考JavaNInst
        super.doMoreInst(
            new Operation[]{Operation.W, Operation.P},
            new String[]{"w", "p"},
            pCtx.getReadType()
        );
    }
}
