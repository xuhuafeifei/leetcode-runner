package com.xhf.leetcode.plugin.debug.execute.cpp;

import com.xhf.leetcode.plugin.debug.command.operation.Operation;
import com.xhf.leetcode.plugin.debug.execute.ExecuteResult;
import com.xhf.leetcode.plugin.debug.instruction.Instruction;
import com.xhf.leetcode.plugin.debug.reader.InstSource;
import com.xhf.leetcode.plugin.debug.reader.ReadType;
import org.jetbrains.annotations.NotNull;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class CppRInst extends AbstractCppInstExecutor {
    @Override
    protected String getGdbCommand(@NotNull Instruction inst, CppContext pCtx) {
        return "-exec-continue";
    }

    @Override
    protected ExecuteResult doExecute(Instruction inst, CppContext pCtx, String gdbCommand) {
        ReadType readType = pCtx.getReadType();
        switch (readType) {
            case COMMAND_IN:
                InstSource.userCmdInput("w");
            case UI_IN:
                InstSource.uiInstInput(Instruction.success(readType, Operation.W, null));
        }
        return super.doExecute(inst, pCtx, gdbCommand);
        // return new CppWInst().execute(Instruction.success(pCtx.getReadType(), Operation.W, null), pCtx);
    }
}
