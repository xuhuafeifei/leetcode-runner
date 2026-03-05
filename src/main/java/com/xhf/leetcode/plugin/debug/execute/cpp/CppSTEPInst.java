package com.xhf.leetcode.plugin.debug.execute.cpp;

import com.xhf.leetcode.plugin.debug.command.operation.Operation;
import com.xhf.leetcode.plugin.debug.instruction.Instruction;
import com.xhf.leetcode.plugin.debug.reader.InstSource;
import com.xhf.leetcode.plugin.debug.reader.ReadType;
import com.xhf.leetcode.plugin.exception.DebugError;
import com.xhf.leetcode.plugin.setting.AppSettings;
import org.jetbrains.annotations.NotNull;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class CppSTEPInst extends AbstractCppInstExecutor {

    @Override
    protected String getGdbCommand(@NotNull Instruction inst, CppContext pCtx) {
        // 这里设置原因和N Inst一致
        if (AppSettings.getInstance().isUIOutput()) {
            InstSource.uiInstInput(Instruction.success(ReadType.UI_IN, Operation.W, ""));
        }
        String gdbCommand;
        switch (inst.getParam()) {
            case "over":
                gdbCommand = "-exec-next";
                break;
            case "out":
                gdbCommand = "-exec-finish";
                break;
            default:
                throw new DebugError("未知的STEP指令参数: " + inst.getParam());
        }
        return gdbCommand;
    }
}