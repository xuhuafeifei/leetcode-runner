package com.xhf.leetcode.plugin.debug.execute;

import com.sun.jdi.*;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequestManager;
import com.xhf.leetcode.plugin.debug.params.Instrument;

import java.util.ArrayList;
import java.util.List;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class JavaBInst implements InstExecutor{
    @Override
    public ExecuteResult execute(Instrument inst, Context context) {
        Location location = context.getLocation();

        ReferenceType referenceType = location.declaringType();
        ClassType classType = (ClassType) referenceType;
        int lineNumber = Integer.parseInt(inst.getParam());

        try {
            return setBreakpointAtLine(classType, lineNumber, context.getErm(), context, inst);
        } catch (AbsentInformationException e) {
            return ExecuteResult.fail(inst.getOperation(), e.toString());
        }
    }

    // 在指定行设置断点
    private static ExecuteResult setBreakpointAtLine(ClassType currentClass, int lineNumber, EventRequestManager erm, Context context, Instrument inst) throws AbsentInformationException {
        List<Method> methods = currentClass.methods();
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
                return ExecuteResult.success(inst.getOperation(), "break point set at line " + lineNumber);
            }
        }

        // 查找指定行的Location
        for (Location location : locations) {
            if (location.lineNumber() == lineNumber) {
                // 设置断点
                BreakpointRequest breakpointRequest = erm.createBreakpointRequest(location);
                breakpointRequest.enable();
                context.addBreakpointRequest(breakpointRequest);

                return ExecuteResult.success(inst.getOperation(), "Breakpoint set at line " + lineNumber);
            }
        }
        ExecuteResult r = ExecuteResult.fail(inst.getOperation(), "no valid location found");
        r.setAddLine(lineNumber);
        return r;
    }
}
