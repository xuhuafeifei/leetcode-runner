package com.xhf.leetcode.plugin.debug.execute.python;

import com.xhf.leetcode.plugin.debug.execute.ExecuteResult;
import org.apache.commons.lang3.StringUtils;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class PythonSHOWBInst extends AbstractPythonInstExecutor {
    @Override
    protected void doAfter(ExecuteResult r, PyContext pCtx) {
        correctResult(r, pCtx);
        String input = r.getResult();
        if (StringUtils.isBlank(input)) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (String s : input.split("\n")) {
            String newLine = matchLine(s, -pCtx.getEnv().getOffset());
            sb.append(newLine).append("\n");
        }
        r.setResult(sb.toString());
    }
}
