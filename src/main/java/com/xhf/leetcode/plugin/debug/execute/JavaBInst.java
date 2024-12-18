package com.xhf.leetcode.plugin.debug.execute;

import com.sun.jdi.*;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequestManager;
import com.xhf.leetcode.plugin.debug.params.Instrument;
import com.xhf.leetcode.plugin.utils.LogUtils;

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
            return setBreakpointAtLine(classType, lineNumber, context.getErm(), context);
        } catch (AbsentInformationException e) {
            return ExecuteResult.fail(e.toString());
        }
    }

    // 在指定行设置断点
    private static ExecuteResult setBreakpointAtLine(ClassType currentClass, int lineNumber, EventRequestManager erm, Context context) throws AbsentInformationException {
        List<Method> methods = currentClass.methods();
        // 获取methods的所有location
        List<Location> locations = new ArrayList<>();
        for (Method method : methods) {
            locations.addAll(method.allLineLocations());
        }

        // 查找指定行的Location
        for (Location location : locations) {
            if (location.lineNumber() == lineNumber) {
                // 设置断点
                BreakpointRequest breakpointRequest = erm.createBreakpointRequest(location);
                breakpointRequest.enable();
                context.addBreakpointRequest(breakpointRequest);

                LogUtils.simpleDebug("Breakpoint set at line " + lineNumber);
                return ExecuteResult.success();
            }
        }
        return ExecuteResult.fail("no valid location found");
    }
}
