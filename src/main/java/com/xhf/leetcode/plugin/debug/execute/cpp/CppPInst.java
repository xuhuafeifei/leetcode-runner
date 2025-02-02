package com.xhf.leetcode.plugin.debug.execute.cpp;

import com.xhf.leetcode.plugin.debug.command.operation.Operation;
import com.xhf.leetcode.plugin.debug.execute.ExecuteResult;
import com.xhf.leetcode.plugin.debug.instruction.Instruction;
import com.xhf.leetcode.plugin.exception.DebugError;
import com.xhf.leetcode.plugin.utils.Constants;
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
        String res = "";
        res += getWatchPool(inst, pCtx);

        String gdbCommand = "-stack-list-variables  --all-values";
        ExecuteResult r = super.doExecute(inst, pCtx, gdbCommand);

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
        return r;
    }

    private String getWatchPool(Instruction inst, CppContext pCtx) {
        String[] watchPool = pCtx.getWatchPool();
        StringBuilder res = new StringBuilder();
        if (watchPool.length != 0) {
            res = new StringBuilder(Constants.WATCH + ":\n");
            for (String watch : watchPool) {
                inst.setParam(watch);
                res.append(super.doExecute(inst, pCtx, "p " + watch).getResult()).append("\n");
            }
            return res.toString();
        }
        return res.toString();
    }
}
