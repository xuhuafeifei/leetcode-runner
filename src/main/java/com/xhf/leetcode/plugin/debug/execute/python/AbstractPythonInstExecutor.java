package com.xhf.leetcode.plugin.debug.execute.python;

import com.xhf.leetcode.plugin.debug.command.operation.Operation;
import com.xhf.leetcode.plugin.debug.env.PythonDebugEnv;
import com.xhf.leetcode.plugin.debug.execute.AbstractInstExecutor;
import com.xhf.leetcode.plugin.debug.execute.ExecuteContext;
import com.xhf.leetcode.plugin.debug.execute.ExecuteResult;
import com.xhf.leetcode.plugin.debug.execute.InstExecutor;
import com.xhf.leetcode.plugin.debug.instruction.Instruction;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import com.xhf.leetcode.plugin.exception.DebugError;
import com.xhf.leetcode.plugin.utils.Constants;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public abstract class AbstractPythonInstExecutor extends AbstractInstExecutor {
    protected static final String PY_SERVER_DISCONNECT = Constants.PY_SERVER_DISCONNECT;

    @Override
    public final ExecuteResult execute(Instruction inst, ExecuteContext context) {
        if (! (context instanceof PyContext) ) {
            throw new DebugError("context must be instance of com.xhf.leetcode.plugin.debug.execute.python.PyContext in python executor");
        }
        PyContext pCtx = (PyContext) context;
        // 前置处理inst
        doPre(inst, pCtx);
        ExecuteResult r = doExecute(inst, pCtx);
        // 后置处理result
        doAfter(r, pCtx);
        return r;
    }

    // 与python服务交互前, 做前置处理. 处理对象为指令
    protected void doPre(Instruction inst, PyContext pCtx) {
    }

    // 后置操作ExecuteResult, 交给子类重写, 默认不做任何操作
    protected void doAfter(ExecuteResult r, PyContext pCtx) {
    }

    // protected abstract ExecuteResult doExecute(Instruction inst, PyContext pCtx);
    /*
        curl -X POST http://localhost:{pyPort}/process -d "{\"operation\": \"R\", \"param\": \"\"}" -H "Content-Type: application/json"
        可以选择让子类重写
     */
    protected ExecuteResult doExecute(Instruction inst, PyContext pCtx) {
        PyClient pyClient = pCtx.getPyClient();
        PyClient.PyResponse pyResponse = pyClient.postRequest(inst);
        if (pyResponse == null) {
            return disConnectedResult(inst.getOperation());
        }
        return pyResponse.getData();
    }

    /**
     * 纠正结果操作, 目前用于处理行号信息. 因为python调试服务对于用户编写的文件做出额外处理, 在debug过程中新增大量的import语句
     * 因此和编写的文件相比, 存在行数上的偏差, 需要进行结果纠正
     *
     * @param result 执行结果
     */
    protected final void correctResult(ExecuteResult result, PyContext pCtx) {
        int addLine = result.getAddLine();
        if (addLine != 0) {
            // 校准
            PythonDebugEnv env = pCtx.getEnv();
            addLine -= env.getOffset();
            result.setAddLine(addLine);
        }
    }

    /**
     * 匹配与更换信息
     * 该方法匹配形如 'breakpoint at line 数字' 的内容. 并且在'数字'累加offset
     * 请注意, 该方法只能匹配单行数据, 也就是说input不能包含换行符, 且必须是 line 数字 这样的形式
     *
     * @param input input
     * @param offset offset
     * @return String
     */
    protected String matchLine(String input, int offset) {
        return DebugUtils.matchLine(input, offset);
    }

    protected void correctInst(Instruction inst, PyContext pCtx) {
        int lineNumber = Integer.parseInt(inst.getParam());
        lineNumber += pCtx.getEnv().getOffset();
        inst.setParam(String.valueOf(lineNumber));
    }

    /**
     * 返回断开连接的result
     * @param op operation
     * @return r
     */
    protected ExecuteResult disConnectedResult(Operation op) {
        ExecuteResult success = ExecuteResult.success(op);
        success.setMoreInfo(PY_SERVER_DISCONNECT);
        success.setHasResult(false);
        return success;
    }
}
