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
            String fileName = subFileName(frame0.get("frame").getAsGdbObject().get("fullname").getAsGdbPrimitive().getAsString());
            String methodName = frame0.get("frame").getAsGdbObject().get("func").getAsGdbPrimitive().getAsString();
            int lineNumber = Integer.parseInt(frame0.get("frame").getAsGdbObject().get("line").getAsGdbPrimitive().getAsString());
            // 设置行信息
            DebugUtils.fillExecuteResultByLocation(r, fileName, methodName, lineNumber);
            r.setResult("at " + fileName + " " + lineNumber);
        }
    }

    /**
     * 截取文件名字
     * @param fullname 全路径名
     * @return str
     */
    private String subFileName(String fullname) {
        String[] split = fullname.split("\\\\");
        return split[split.length - 1];
    }

    @Override
    protected String getGdbCommand(@NotNull Instruction inst, CppContext pCtx) {
        return "-stack-list-frames";
    }
}
