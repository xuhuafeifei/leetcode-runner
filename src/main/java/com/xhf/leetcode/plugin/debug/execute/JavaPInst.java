package com.xhf.leetcode.plugin.debug.execute;

import com.sun.jdi.*;
import com.sun.jdi.event.BreakpointEvent;
import com.xhf.leetcode.plugin.debug.params.Instrument;

import java.util.List;

/**
 * 执行P操作. 操作具体信息详见{@link com.xhf.leetcode.plugin.debug.params.Operation}
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class JavaPInst implements InstExecutor{

    private final JavaValueInspector inspector;

    public JavaPInst() {
        inspector = JavaValueInspector.getInstance();
    }

    @Override
    public ExecuteResult execute(Instrument inst, Context context) {
        BreakpointEvent breakpointEvent = context.getBreakpointEvent();
        ThreadReference thread = breakpointEvent.thread();
        try {
            String res = getVariable(thread);
            // 存储输出结果
            return ExecuteResult.success(res);
        } catch (IncompatibleThreadStateException | AbsentInformationException e) {
            e.printStackTrace();
            return ExecuteResult.fail();
        }
    }

    private String getVariable(ThreadReference thread) throws IncompatibleThreadStateException, AbsentInformationException {
        // 获取当前栈帧
        StackFrame frame = thread.frame(0);
        // 获取本地变量
        List<LocalVariable> localVariables = frame.visibleVariables();
        StringBuilder sb = new StringBuilder();
        // 遍历
        for (int i = 0; i < localVariables.size(); i++) {
            LocalVariable localVar = localVariables.get(i);
            Value varValue = frame.getValue(localVar);
            // 强转获取value
            sb.append(localVar.name()).append(" = ").append(inspector.inspectValue(varValue));
            if (i != localVariables.size() - 1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

}
