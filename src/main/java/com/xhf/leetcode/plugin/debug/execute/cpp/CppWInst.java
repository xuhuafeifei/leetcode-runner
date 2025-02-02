package com.xhf.leetcode.plugin.debug.execute.cpp;

import com.xhf.leetcode.plugin.debug.execute.ExecuteResult;
import com.xhf.leetcode.plugin.debug.execute.cpp.gdb.CppGdbInfo;
import com.xhf.leetcode.plugin.debug.execute.cpp.gdb.GdbArray;
import com.xhf.leetcode.plugin.debug.execute.cpp.gdb.GdbElement;
import com.xhf.leetcode.plugin.debug.instruction.Instruction;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import org.jetbrains.annotations.NotNull;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class CppWInst extends AbstractCppInstExecutor {
    @Override
    protected void doAfter(ExecuteResult r, CppContext pCtx) {
        CppGdbInfo cppGdbInfo = super.getCppGdbInfo(r);
        // gdb执行没有错误
        if (! super.handleError(r, cppGdbInfo)) {
            GdbElement gdbElement =  this.gdbParser.parse(cppGdbInfo.getResultRecord());
            GdbArray arr = gdbElement.getAsGdbObject().get("stack").getAsGdbArray();
            var frame0 = arr.get(0).getAsGdbObject();
            DebugUtils.fillExecuteResultByLocation(r,
                    frame0.get("fullname").getAsGdbPrimitive().getAsString(),
                    frame0.get("func").getAsGdbPrimitive().getAsString(),
                    frame0.get("line").getAsGdbPrimitive().getAsNumber().intValue()
            );
        }
    }

    @Override
    protected String getGdbCommand(@NotNull Instruction inst, CppContext pCtx) {
        return "-stack-list-frames";
    }
}
