package com.xhf.leetcode.plugin.debug.execute;

import com.sun.jdi.*;
import com.sun.jdi.event.BreakpointEvent;
import com.xhf.leetcode.plugin.debug.params.Instrument;
import com.xhf.leetcode.plugin.debug.params.Operation;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import com.xhf.leetcode.plugin.utils.LogUtils;

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
        ThreadReference thread = context.getThread();
        Location location = context.getLocation();

        int lineNumber = location.lineNumber();
        String className = location.declaringType().name();
        String methodName = location.method().name();

        // 执行到solution以外的代码, 不做任何处理
        if (! "Solution".equals(className)) {
            ExecuteResult failed = ExecuteResult.fail(inst.getOperation(), "当前断点执行的是系统函数, 不支持显示局部变量");
            DebugUtils.fillExecuteResultByLocation(failed, location);
            return failed;
        }

        try {
            String res = getVariable(thread);
            // 存储输出结果
            ExecuteResult success = ExecuteResult.success(inst.getOperation(), res);
            DebugUtils.fillExecuteResultByLocation(success, location);
            return success;
        } catch (AbsentInformationException e) {
            ExecuteResult failed = ExecuteResult.fail(inst.getOperation(), "没有局部变量");
            DebugUtils.fillExecuteResultByLocation(failed, location);
            return failed;
        } catch (IncompatibleThreadStateException e) {
            LogUtils.error(e);
            return ExecuteResult.fail(inst.getOperation());
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
