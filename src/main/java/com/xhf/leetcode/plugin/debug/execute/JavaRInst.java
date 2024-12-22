package com.xhf.leetcode.plugin.debug.execute;

import com.sun.jdi.event.EventSet;
import com.sun.jdi.request.StepRequest;
import com.xhf.leetcode.plugin.debug.params.Instrument;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class JavaRInst implements InstExecutor{

    @Override
    public ExecuteResult execute(Instrument inst, Context context) {
        context.removeStepRequest();
        return ExecuteResult.success(inst.getOperation());
    }
}
