package com.xhf.leetcode.plugin.debug.execute.java.p;

import com.sun.jdi.*;
import com.xhf.leetcode.plugin.debug.execute.ExecuteResult;
import com.xhf.leetcode.plugin.debug.execute.java.AbstractJavaInstExecutor;
import com.xhf.leetcode.plugin.debug.execute.java.Context;
import com.xhf.leetcode.plugin.debug.instruction.Instruction;
import com.xhf.leetcode.plugin.debug.command.operation.Operation;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import com.xhf.leetcode.plugin.utils.Constants;
import com.xhf.leetcode.plugin.utils.LogUtils;

import java.util.*;

/**
 * 执行P操作. 操作具体信息详见{@link Operation}
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class JavaPInst extends AbstractJavaInstExecutor {

    private final JavaValueInspector inspector;
    private StackFrame frame;

    public JavaPInst() {
        inspector = JavaValueInspector.getInstance();
    }

    @Override
    public ExecuteResult doExecute(Instruction inst, Context context) {
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
            String res = getVariable(thread, location, context);
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
        } catch (ClassNotLoadedException e) {
            throw new RuntimeException(e);
        }
    }

    private String getVariable(ThreadReference thread, Location location, Context context) throws IncompatibleThreadStateException, AbsentInformationException, ClassNotLoadedException {
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
                statics.add(field.name() + " = " + handleValue(value, context));
            }
            // 处理成员变量
            else {
                ObjectReference objectRef = frame.thisObject();
                if (objectRef != null) {
                    // 获取实例字段的值
                    Value value = objectRef.getValue(field);
                    members.add(field.name() + " = " + handleValue(value, context));
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


    private String handleValue(Value value, Context ctx) {
        if (value != null) {
            if (value.toString().contains("java.lang.ThreadGroup")) {
                System.out.println("abab");
            }
            System.out.println(value);
        }
        try {
            return this.inspector.inspectValue(value);
        } catch (Exception e) {
            DebugUtils.simpleDebug(e.getMessage(), ctx.getProject());
            LogUtils.error(e);
            return "变量类型不支持显示";
        }
    }

}
