package com.xhf.leetcode.plugin.debug.execute.python;

import com.xhf.leetcode.plugin.debug.env.PythonDebugEnv;
import com.xhf.leetcode.plugin.debug.execute.ExecuteResult;
import com.xhf.leetcode.plugin.debug.instruction.Instruction;
import com.xhf.leetcode.plugin.io.http.utils.HttpClient;
import com.xhf.leetcode.plugin.model.HttpRequest;
import com.xhf.leetcode.plugin.model.HttpResponse;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class PythonNInst extends AbstractPythonInstExecutor {

    /*
        curl -X POST http://localhost:{pyPort}/process -d "{\"operation\": \"R\", \"param\": \"\"}" -H "Content-Type: application/json"
     */
    @Override
    protected ExecuteResult doExecute(Instruction inst, PyContext pCtx) {
        PyClient pyClient = pCtx.getPyClient();
        PyClient.PyResponse pyResponse = pyClient.executeN(inst);
        return null;
    }
}
