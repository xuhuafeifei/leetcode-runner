package com.xhf.leetcode.plugin.debug.execute;

import com.sun.jdi.*;
import com.xhf.leetcode.plugin.debug.params.Instrument;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import com.xhf.leetcode.plugin.utils.Constants;
import com.xhf.leetcode.plugin.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 执行P操作. 操作具体信息详见{@link com.xhf.leetcode.plugin.debug.params.Operation}
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class JavaPInst implements InstExecutor{

    private final JavaValueInspector inspector;
    private StackFrame frame;

    public JavaPInst() {
        inspector = JavaValueInspector.getInstance();
    }

    @Override
    public ExecuteResult execute(Instrument inst, Context context) {
        ThreadReference thread = context.getThread();
        Location location = context.getLocation();

        String className = location.declaringType().name();

        // 执行到solution以外的代码, 不做任何处理
        if (! "Solution".equals(className)) {
            ExecuteResult failed = ExecuteResult.fail(inst.getOperation(), "当前断点执行的是系统函数, 不支持显示局部变量");
            DebugUtils.fillExecuteResultByLocation(failed, location);
            return failed;
        }

        try {
            if (location.lineNumber() == 18) {
                System.out.println("31...");
            }
            System.err.println(DebugUtils.buildCurrentLineInfoByLocation(location));
            String res = getVariable(thread, location);
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

    private String getVariable(ThreadReference thread, Location location) throws IncompatibleThreadStateException, AbsentInformationException {
        // 获取当前栈帧
        StackFrame frame = thread.frame(0);
        // 获取本地变量
        List<LocalVariable> localVariables = frame.visibleVariables();
        StringBuilder sb = new StringBuilder(Constants.LOCAL_VARIABLE + ":\n");
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
        // 获取静态变量
        ReferenceType referenceType = location.declaringType();
        List<Field> fields = referenceType.fields();

        List<String> statics = new ArrayList<>();
        List<String> members = new ArrayList<>();

        for (Field field : fields) {
            // 处理静态变量
            if (field.isStatic()) {
                Value value = referenceType.getValue(field);  // 获取静态字段的值
                statics.add(field.name() + " = " + inspector.inspectValue(value));
            }
            // 处理成员变量
            else {
                ObjectReference objectRef = frame.thisObject();
                if (objectRef != null) {
                    // 获取实例字段的值
                    Value value = objectRef.getValue(field);
                    members.add(field.name() + " = " + inspector.inspectValue(value));
                }
            }
        }
        if (!statics.isEmpty()) {
            sb.append("\n" + Constants.STATIC_VARIABLE + ":\n");
            for (int i = 0; i < statics.size(); i++) {
                sb.append(statics.get(i));
                if (i != statics.size() - 1) {
                    sb.append("\n");
                }
            }
        }
        if (!members.isEmpty()) {
            sb.append("\n" + Constants.MEMBER_VARIABLE + ":\n");
            for (int i = 0; i < members.size(); i++) {
                sb.append(members.get(i));
                if (i != members.size() - 1) {
                    sb.append("\n");
                }
            }
        }
        return sb.toString();
    }

}
