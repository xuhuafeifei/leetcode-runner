package com.xhf.leetcode.plugin.debug.execute.python;

import com.xhf.leetcode.plugin.debug.execute.ExecuteContext;
import com.xhf.leetcode.plugin.debug.execute.ExecuteResult;
import com.xhf.leetcode.plugin.debug.execute.InstExecutor;
import com.xhf.leetcode.plugin.debug.instruction.Instruction;
import com.xhf.leetcode.plugin.exception.DebugError;
import com.xhf.leetcode.plugin.utils.Constants;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public abstract class AbstractPythonInstExecutor implements InstExecutor {
    protected static final String PY_SERVER_DISCONNECT = Constants.PY_SERVER_DISCONNECT;

    @Override
    public final ExecuteResult execute(Instruction inst, ExecuteContext context) {
        if (! (context instanceof PyContext) ) {
            throw new DebugError("context must be instance of com.xhf.leetcode.plugin.debug.execute.python.PyContext in python executor");
        }
        return doExecute(inst, (PyContext) context);
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
            ExecuteResult success = ExecuteResult.success(inst.getOperation());
            success.setMoreInfo(PY_SERVER_DISCONNECT);
            success.setHasResult(false);
            return success;
        }
        ExecuteResult data = pyResponse.getData();
        // data.setHasResult(true);
        return data;
    }
}
