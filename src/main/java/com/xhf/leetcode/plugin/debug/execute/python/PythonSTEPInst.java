package com.xhf.leetcode.plugin.debug.execute.python;

import com.sun.jdi.Location;
import com.xhf.leetcode.plugin.debug.command.operation.Operation;
import com.xhf.leetcode.plugin.debug.execute.ExecuteContext;
import com.xhf.leetcode.plugin.debug.execute.ExecuteResult;
import com.xhf.leetcode.plugin.debug.execute.InstExecutor;
import com.xhf.leetcode.plugin.debug.instruction.Instruction;
import com.xhf.leetcode.plugin.debug.reader.InstSource;
import com.xhf.leetcode.plugin.debug.reader.ReadType;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import com.xhf.leetcode.plugin.setting.AppSettings;

/**
 * 参考{@link com.xhf.leetcode.plugin.debug.execute.java.JavaNInst}
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class PythonSTEPInst extends AbstractPythonInstExecutor {
    @Override
    protected void doAfter(ExecuteResult r, PyContext pCtx) {
        // 这么设置的原因可以参考JavaNInst
        if (AppSettings.getInstance().isUIOutput()) {
            InstSource.uiInstInput(Instruction.success(ReadType.UI_IN, Operation.W, ""));
        }
    }
}
