package com.xhf.leetcode.plugin.debug.execute.java;

import com.sun.jdi.Location;
import com.sun.jdi.request.BreakpointRequest;
import com.xhf.leetcode.plugin.debug.execute.ExecuteResult;
import com.xhf.leetcode.plugin.debug.instruction.Instruction;
import com.xhf.leetcode.plugin.utils.BundleUtils;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 * show b: 显示所有断点
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class JavaSHOWBInst extends AbstractJavaInstExecutor {

    @Override
    public ExecuteResult doExecute(Instruction inst, Context context) {
        List<BreakpointRequest> breakpointRequests = context.getBreakpointRequests();
        StringBuilder sb = new StringBuilder();

        // 先排序
        context.getBreakpointRequests().sort((a, b) -> {
            String aName = a.location().declaringType().name();
            String bName = b.location().declaringType().name();
            if (aName.equals(bName)) {
                return a.location().lineNumber() - b.location().lineNumber();
            } else {
                return aName.compareTo(bName);
            }
        });

        for (int i = 0; i < breakpointRequests.size(); i++) {
            BreakpointRequest breakpointRequest = breakpointRequests.get(i);
            Location location = breakpointRequest.location();
            String className = location.declaringType().name();
            String methodName = location.method().name();
            int lineNumber = location.lineNumber();
            // 只有开启的断点才有效
            if (!breakpointRequest.isEnabled()) {
                continue;
            }
            sb.append(BundleUtils.i18nHelper("断点在 ", "Breakpoint at ")).append(className).append(".")
                .append(methodName).append(BundleUtils.i18nHelper(" 第" + lineNumber + "行", " line " + lineNumber));
            if (i != breakpointRequests.size() - 1) {
                sb.append("\n");
            }
        }
        String res = sb.toString();
        if (StringUtils.isBlank(res)) {
            res = BundleUtils.i18nHelper("没有有效的断点", "No enabled breakpoints");
        }
        return ExecuteResult.success(inst.getOperation(), res);
    }
}
