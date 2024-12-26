package com.xhf.leetcode.plugin.debug.execute.python;

import com.xhf.leetcode.plugin.debug.execute.ExecuteResult;
import com.xhf.leetcode.plugin.debug.instruction.Instruction;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class PythonRBInst extends AbstractPythonInstExecutor {
    @Override
    protected void doPre(Instruction inst, PyContext pCtx) {
        // 纠正break point行号
        super.correctInst(inst, pCtx);
    }

    @Override
    protected void doAfter(ExecuteResult r, PyContext pCtx) {
        correctResult(r, pCtx);
        // 纠正result行号
        StringBuilder sb = new StringBuilder();
        for (String s : r.getResult().split("\n")) {
            sb.append(matchLine(s, -pCtx.getEnv().getOffset())).append("\n");
        }
        r.setResult(sb.toString());
    }
}
