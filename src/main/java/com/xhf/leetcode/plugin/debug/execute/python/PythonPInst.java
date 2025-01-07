package com.xhf.leetcode.plugin.debug.execute.python;

import com.xhf.leetcode.plugin.debug.command.operation.Operation;
import com.xhf.leetcode.plugin.debug.execute.ExecuteResult;
import com.xhf.leetcode.plugin.debug.instruction.Instruction;
import com.xhf.leetcode.plugin.exception.DebugError;
import com.xhf.leetcode.plugin.utils.Constants;
import org.apache.commons.lang.StringUtils;

import java.util.Deque;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class PythonPInst extends AbstractPythonInstExecutor {
    @Override
    protected ExecuteResult doExecute(Instruction inst, PyContext pCtx) {
        if (inst.getOperation() == Operation.P) {
            return doP(inst, pCtx);
        }else if (inst.getOperation() == Operation.WATCH) {
            return doWATCH(inst, pCtx);
        }
        throw new DebugError("PythonPInst不支持的操作 " + inst.getOperation());
    }

    private ExecuteResult doWATCH(Instruction inst, PyContext pCtx) {
        pCtx.getWatchPool().addFirst(inst.getParam());
        return doP(inst, pCtx);
    }

    private ExecuteResult doP(Instruction inst, PyContext pCtx) {
        String exp = inst.getParam();
        String res = "";
        // 修改为P指令
        inst.setOperation(Operation.P);
        // 计算表达式
        if (StringUtils.isNotBlank(exp)) {
            ExecuteResult computeR = super.doExecute(inst, pCtx);
            res = computeR.getResult() + "\n";
        }
        // 检查监视池
        res += getWatchPool(inst, pCtx);
        inst.setParam("");
        // 打印变量
        ExecuteResult printR = super.doExecute(inst, pCtx);
        res += printR.getResult();
        return ExecuteResult.success(inst.getOperation(), res);
    }

    private String getWatchPool(Instruction inst, PyContext pCtx) {
        Deque<String> watchPool = pCtx.getWatchPool();
        StringBuilder res = new StringBuilder();
        if (! watchPool.isEmpty()) {
            res = new StringBuilder(Constants.WATCH + ":\n");
            for (String watch : watchPool) {
                inst.setParam(watch);
                res.append(super.doExecute(inst, pCtx).getResult()).append("\n");
            }
            return res.toString();
        }
        return res.toString();
    }
}
