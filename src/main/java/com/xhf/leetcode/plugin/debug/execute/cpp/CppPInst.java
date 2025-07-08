package com.xhf.leetcode.plugin.debug.execute.cpp;

import com.xhf.leetcode.plugin.debug.command.operation.Operation;
import com.xhf.leetcode.plugin.debug.execute.ExecuteResult;
import com.xhf.leetcode.plugin.debug.execute.cpp.gdb.CppGdbInfo;
import com.xhf.leetcode.plugin.debug.execute.cpp.gdb.GdbObject;
import com.xhf.leetcode.plugin.debug.instruction.Instruction;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import com.xhf.leetcode.plugin.exception.DebugError;
import com.xhf.leetcode.plugin.utils.Constants;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class CppPInst extends AbstractCppInstExecutor {

    @Override
    protected String getGdbCommand(@NotNull Instruction inst, CppContext pCtx) {
        return null;
    }

    @Override
    protected ExecuteResult doExecute(Instruction inst, CppContext pCtx, String gdbCommand) {
        if (inst.getOperation() == Operation.P) {
            return doP(inst, pCtx);
        } else if (inst.getOperation() == Operation.WATCH) {
            return doWATCH(inst, pCtx);
        }
        throw new DebugError("CppPInst不支持的操作 " + inst.getOperation());
    }

    private ExecuteResult doWATCH(Instruction inst, CppContext pCtx) {
        pCtx.addToWatchPool(inst.getParam());
        return doP(inst, pCtx);
    }

    private ExecuteResult doP(Instruction inst, CppContext pCtx) {
        String exp = inst.getParam();
        String res = "";
        // 计算表达式
        if (StringUtils.isNotBlank(exp)) {
            res = doCompute(exp, inst, pCtx);
        }
        // 检查监视池
        res += getWatchPool(inst, pCtx);

        res += Constants.LOCAL_VARIABLE + ":\n";
        ExecuteResult r = super.doExecute(inst, pCtx, "info locals");

        res += r.getResult();
        return ExecuteResult.success(inst.getOperation(), res);

        //        String gdbCommand = "-stack-list-variables  --all-values";
        //        CppGdbInfo cppGdbInfo = super.getCppGdbInfo(r);
        //        // 没有错误
        //        if (handleError(r, cppGdbInfo)) {
        //            GdbElement gdbElement =  super.gdbParser.parse(cppGdbInfo.getResultRecord());
        //            GdbArray variables = gdbElement.getAsGdbObject().get("variables").getAsGdbArray();
        //            StringBuilder sb = new StringBuilder();
        //            if (variables.size() == 0) {
        //                sb.append("No variables");
        //            } else {
        //                for (GdbElement variable : variables) {
        //                    GdbElement name = variable.getAsGdbObject().get("name");
        //                    GdbElement value = variable.getAsGdbObject().get("value");
        //                    sb.append(name.getAsGdbPrimitive().getAsString()).append("=").append(value.getAsGdbPrimitive().getAsString()).append("\n");
        //                }
        //            }
        //            r.setResult(sb.toString());
        //        }
        //        return r;
    }

    /**
     * 表达式计算
     *
     * @param exp 需要计算的表达式
     * @param inst inst
     * @param pCtx pCtx
     * @return ans
     */
    private String doCompute(String exp, Instruction inst, CppContext pCtx) {
        // exp 需要添加双引号
        ExecuteResult computeR = super.doExecute(inst, pCtx, "-data-evaluate-expression " + DebugUtils.addQuotes(exp));
        CppGdbInfo cppGdbInfo = super.getCppGdbInfo(computeR);
        GdbObject obj = this.gdbParser.parse(this.gdbParser.preHandle(cppGdbInfo.getResultRecord())).getAsGdbObject();

        String res = "";

        if (super.isGdbError(cppGdbInfo)) {
            res += exp + " = " + obj.get("msg").getAsGdbPrimitive().getAsString().replace("\\", "");
        } else {
            res += exp + " = " + obj.get("value").getAsGdbPrimitive().getAsString().replace("\\", "");
        }
        if (res.charAt(res.length() - 1) != '\n') {
            res += '\n';
        }
        return res;
    }

    private String getWatchPool(Instruction inst, CppContext pCtx) {
        String[] watchPool = pCtx.getWatchPool();
        StringBuilder res = new StringBuilder();
        if (watchPool.length != 0) {
            res = new StringBuilder(Constants.WATCH + ":\n");
            for (String watch : watchPool) {
                if (StringUtils.isBlank(watch)) {
                    continue;
                }
                inst.setParam(watch);
                res.append(doCompute(watch, inst, pCtx));
            }
            return res.toString();
        }
        return res.toString();
    }
}
