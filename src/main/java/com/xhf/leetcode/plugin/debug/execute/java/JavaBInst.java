package com.xhf.leetcode.plugin.debug.execute.java;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassType;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequestManager;
import com.xhf.leetcode.plugin.debug.execute.ExecuteResult;
import com.xhf.leetcode.plugin.debug.instruction.Instruction;
import com.xhf.leetcode.plugin.utils.BundleUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * 只允许在Solution文件中设置断点
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class JavaBInst extends AbstractJavaInstExecutor {

    // 在指定行设置断点
    private static ExecuteResult setBreakpointAtLine(ClassType solutionClassType, int lineNumber,
        EventRequestManager erm, Context context, Instruction inst) throws AbsentInformationException {
        List<Method> methods = solutionClassType.methods();
        // 获取methods的所有location
        List<Location> locations = new ArrayList<>();
        for (Method method : methods) {
            locations.addAll(method.allLineLocations());
        }
        // 检查breakpointRequest是否已经存在
        List<BreakpointRequest> breakpointRequests = context.getBreakpointRequests();
        for (BreakpointRequest breakpointRequest : breakpointRequests) {
            if (breakpointRequest.location().lineNumber() == lineNumber) {
                breakpointRequest.enable();
                return ExecuteResult.success(inst.getOperation(),
                    BundleUtils.i18nHelper("断点设置在Solution的行号 = ", "break point set at Solution's line ")
                        + lineNumber);
            }
        }

        // 查找指定行的Location
        for (Location location : locations) {
            if (location.lineNumber() == lineNumber) {
                // 设置断点
                BreakpointRequest breakpointRequest = erm.createBreakpointRequest(location);
                breakpointRequest.enable();
                context.addBreakpointRequest(breakpointRequest);

                return ExecuteResult.success(inst.getOperation(),
                    BundleUtils.i18nHelper("断点设置在Solution的行号 = ", "break point set at Solution's line ")
                        + lineNumber);
            }
        }
        ExecuteResult r = ExecuteResult.fail(inst.getOperation(),
            BundleUtils.i18nHelper("未找到Solution文件", "no valid location found in Solution file"));
        r.setAddLine(lineNumber);
        return r;
    }

    @Override
    protected ExecuteResult doExecute(Instruction inst, Context context) {
        /*
          确保获取Solution的Location. 避免出现执行到系统方法, 获取到系统函数的Location.
          从而导致用户在idea设置断点却找不到的bug
         */
        Location solutionLocation = context.getSolutionLocation();
        if (solutionLocation == null) {
            return ExecuteResult.success(inst.getOperation(),
                BundleUtils.i18nHelper("Solution Class未被加载, 无法添加断点",
                    "Solution Class not loaded, cannot add breakpoint"));
        }

        ReferenceType referenceType = solutionLocation.declaringType();
        ClassType solutionClassType = (ClassType) referenceType;
        int lineNumber = Integer.parseInt(inst.getParam());

        try {
            return setBreakpointAtLine(solutionClassType, lineNumber, context.getErm(), context, inst);
        } catch (AbsentInformationException e) {
            return ExecuteResult.fail(inst.getOperation(), e.toString());
        }
    }
}
