package com.xhf.leetcode.plugin.debug.execute.cpp;

import com.xhf.leetcode.plugin.debug.execute.ExecuteResult;
import com.xhf.leetcode.plugin.debug.execute.cpp.gdb.GdbArray;
import com.xhf.leetcode.plugin.debug.execute.cpp.gdb.GdbElement;
import com.xhf.leetcode.plugin.debug.instruction.Instruction;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class CppSHOWBInst extends AbstractCppInstExecutor {

    @Override
    protected String getGdbCommand(@NotNull Instruction inst, CppContext pCtx) {
        return "-break-list";
    }

    @Override
    protected void doAfter(ExecuteResult r, CppContext pCtx) {
        var cppGdbInfo = super.getCppGdbInfo(r);
        // gdb执行没有错误
        if (super.handleError(r, cppGdbInfo)) {
            GdbElement gdbElement = this.gdbParser.parse(cppGdbInfo.getResultRecord());
            GdbArray body = gdbElement.getAsGdbObject().get("BreakpointTable").getAsGdbObject().get("body")
                .getAsGdbArray();
            StringBuilder sb = new StringBuilder();
            if (body.size() == 0) {
                sb.append("No breakpoints");
            } else {
                // 遍历所有断点
                List<String[]> points = new ArrayList<>();
                for (GdbElement element : body) {
                    GdbElement breakpoint = element.getAsGdbObject().get("bkpt");
                    String[] point = new String[2];
                    point[0] = breakpoint.getAsGdbObject().get("type").getAsGdbPrimitive().getAsString();
                    // point[1] = breakpoint.getAsGdbObject().get("func").getAsGdbPrimitive().getAsString();
                    point[1] = breakpoint.getAsGdbObject().get("line").getAsGdbPrimitive().getAsString();
                    points.add(point);
                }
                // sort
                points.sort((o1, o2) -> CharSequence.compare(o1[1], o2[1]));
                for (String[] point : points) {
                    sb.append(point[0]).append(" ").append(point[1]).append("\n");
                }
            }
            r.setResult(sb.toString());
        }
    }
}
