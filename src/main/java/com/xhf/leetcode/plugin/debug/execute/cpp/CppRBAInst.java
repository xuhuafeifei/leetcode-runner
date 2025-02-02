package com.xhf.leetcode.plugin.debug.execute.cpp;

import com.xhf.leetcode.plugin.debug.instruction.Instruction;
import org.jetbrains.annotations.NotNull;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class CppRBAInst extends AbstractCppInstExecutor {
    @Override
    protected String getGdbCommand(@NotNull Instruction inst, CppContext pCtx) {
        return "-break-delete";
    }
}
