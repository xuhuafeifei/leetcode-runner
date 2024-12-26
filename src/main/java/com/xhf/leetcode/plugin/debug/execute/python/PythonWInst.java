package com.xhf.leetcode.plugin.debug.execute.python;

import com.xhf.leetcode.plugin.debug.execute.ExecuteResult;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class PythonWInst extends AbstractPythonInstExecutor {

    @Override
    protected void doAfter(ExecuteResult r, PyContext pCtx) {
        // 处理result
        String result = r.getResult();
        if (result != null) {
            correctResult(r, pCtx);
            result = r.getClassName() + " " + r.getMethodName() + " " + r.getAddLine() + " : " + result;
            r.setResult(result);
        }
    }
}
