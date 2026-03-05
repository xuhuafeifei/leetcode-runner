package com.xhf.leetcode.plugin.debug.execute.java.p;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.Field;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.Location;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import com.xhf.leetcode.plugin.debug.command.operation.Operation;
import com.xhf.leetcode.plugin.debug.execute.ExecuteResult;
import com.xhf.leetcode.plugin.debug.execute.java.AbstractJavaInstExecutor;
import com.xhf.leetcode.plugin.debug.execute.java.Context;
import com.xhf.leetcode.plugin.debug.instruction.Instruction;
import com.xhf.leetcode.plugin.debug.output.Output;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import com.xhf.leetcode.plugin.exception.ComputeError;
import com.xhf.leetcode.plugin.exception.DebugError;
import com.xhf.leetcode.plugin.utils.BundleUtils;
import com.xhf.leetcode.plugin.utils.Constants;
import com.xhf.leetcode.plugin.utils.LogUtils;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 * 执行P操作. 操作具体信息详见{@link Operation}
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class JavaPInst extends AbstractJavaInstExecutor {

    private final JavaValueInspector inspector;

    public JavaPInst() {
        inspector = JavaValueInspector.getInstance();
    }

    @Override
    public ExecuteResult doExecute(Instruction inst, Context context) {
        // 等待event handler
        context.waitForJEventHandler("JavaPInst");

        Location location = context.getLocation();
        String className = location.declaringType().name();

        // 执行到solution以外的代码, 不做任何处理
        if (!"Solution".equals(className)) {
            ExecuteResult failed = ExecuteResult.fail(inst.getOperation(),
                BundleUtils.i18nHelper("当前断点执行的是系统函数, 不支持显示局部变量",
                    "current breakpoint is in system function, not support for displaying local variables"));
            DebugUtils.fillExecuteResultByLocation(failed, location);
            return failed;
        }

        try {
            String res = checkAndGetValue(inst, context);
            // 存储输出结果
            ExecuteResult success = ExecuteResult.success(inst.getOperation(), res);
            DebugUtils.fillExecuteResultByLocation(success, location);
            return success;
        } catch (AbsentInformationException e) {
            ExecuteResult failed = ExecuteResult.fail(inst.getOperation(),
                BundleUtils.i18nHelper("没有局部变量", "no global variables"));
            DebugUtils.fillExecuteResultByLocation(failed, location);
            return failed;
        } catch (Exception e) {
            LogUtils.error(e);
            return ExecuteResult.fail(inst.getOperation(), e.getMessage());
        }
    }

    /**
     * 检查并获取value.
     * 如果是P指令, 如果param有值, 则计算表达式. 否则打印当前局部变量
     * 如果是WATCH指令, 将表达式存入watchPool, 然后执行P指令
     *
     * @param inst 指令
     * @param context 上下文
     * @return res
     * @throws ClassNotLoadedException ClassNotLoadedException
     * @throws IncompatibleThreadStateException IncompatibleThreadStateException
     * @throws AbsentInformationException AbsentInformationException
     */
    private String checkAndGetValue(Instruction inst, Context context)
        throws ClassNotLoadedException, IncompatibleThreadStateException, AbsentInformationException {
        if (inst.getOperation() == Operation.P) {
            return doP(inst, context);
        } else if (inst.getOperation() == Operation.WATCH) {
            return doWATCH(inst, context);
        }
        throw new DebugError(BundleUtils.i18nHelper("JavaPInst不支持的操作 ", "JavaPInst not support this operation ")
            + inst.getOperation());
    }

    /**
     * 执行watch指令
     *
     * @param inst inst
     * @param context context
     * @return string
     * @throws ClassNotLoadedException ClassNotLoadedException
     * @throws IncompatibleThreadStateException IncompatibleThreadStateException
     * @throws AbsentInformationException AbsentInformationException
     */
    private String doWATCH(Instruction inst, Context context)
        throws ClassNotLoadedException, IncompatibleThreadStateException, AbsentInformationException {
        String exp = inst.getParam();
        context.addToWatchPool(exp);
        return doP(inst, context);
    }

    /**
     * 执行P指令
     *
     * @param inst inst
     * @param context context
     * @return string
     * @throws ClassNotLoadedException ClassNotLoadedException
     * @throws IncompatibleThreadStateException IncompatibleThreadStateException
     * @throws AbsentInformationException AbsentInformationException
     */
    private String doP(Instruction inst, Context context)
        throws ClassNotLoadedException, IncompatibleThreadStateException, AbsentInformationException {
        StringBuilder res = new StringBuilder();
        String exp = inst.getParam();
        // 计算表达式(doP可能被doWATCH调用, 这里二次判断)
        if (inst.getOperation() == Operation.P && StringUtils.isNotBlank(exp)) {
            res.append(computeExpression(exp, context)).append("\n");
        }
        // 检查监视池
        res.append(getWatchPool(context));
        // 获取当前执行环境存在的变量
        res.append(getVariable(context.getThread(), context.getLocation(), context));
        return res.toString();
    }

    /**
     * 检查watch pool, 如果存在内容, 执行计算逻辑
     *
     * @param context context
     * @return string
     */
    private String getWatchPool(Context context) {
        StringBuilder res = new StringBuilder();
        String[] watchPool = context.getWatchPool();
        if (watchPool.length != 0) {
            res.append(Constants.WATCH).append(":\n");
            for (String watch : watchPool) {
                if (StringUtils.isBlank(watch)) {
                    continue;
                }
                res.append(computeExpression(watch, context)).append("\n");
            }
        }
        return res.toString();
    }

    /**
     * 计算表达式
     *
     * @param exp exp
     * @param context context
     * @return string
     */
    private String computeExpression(String exp, Context context) {
        StringBuilder res = new StringBuilder();
        // 设置loading data
        Output output = context.getOutput();
        output.output(
            ExecuteResult.success(Operation.NULL, BundleUtils.i18nHelper("exp = 计算中...", "exp = calculating...")));

        try {
            res.append(exp).append(" = ").append(new JavaEvaluatorImpl().executeExpression(exp, context));
        } catch (ComputeError e) {
            res.append(e.getMessage());
        }
        return res.toString();
    }

    /**
     * 获取当前执行环境存在的变量
     *
     * @param thread thread
     * @param location location
     * @param context context
     * @return string
     * @throws ClassNotLoadedException ClassNotLoadedException
     * @throws IncompatibleThreadStateException IncompatibleThreadStateException
     * @throws AbsentInformationException AbsentInformationException
     */
    private String getVariable(ThreadReference thread, Location location, Context context)
        throws IncompatibleThreadStateException, AbsentInformationException, ClassNotLoadedException {
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
            return BundleUtils.i18nHelper("变量类型不支持显示", "variable type not support display");
        }
    }

}
