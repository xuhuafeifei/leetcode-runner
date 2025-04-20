package com.xhf.leetcode.plugin.debug.execute.python;

import com.xhf.leetcode.plugin.debug.command.operation.Operation;
import com.xhf.leetcode.plugin.debug.execute.ExecuteResult;
import com.xhf.leetcode.plugin.debug.instruction.Instruction;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class PythonRInst extends AbstractPythonInstExecutor {
    @Override
    protected ExecuteResult doExecute(Instruction inst, PyContext pCtx) {
        // python debug执行完r指令后, 还需要执行一次w指令.
        // 一方面可以查看当前当前代码执行到哪一行, 另一方面可以触发python debugger的检测机制, 自动检测py server是否执行完毕
        // 此处不同于JavaDebugger. JavaDebugger在remove event时, 会自动判断vm连接情况
        // 但pythonDebugger一切行为都需要我手动判断, 比较操蛋. 除非我每次循环都判断一次连接情况, 否则只能通过执行指令的方式判断
        // python server是否关闭
        super.doExecute(inst, pCtx);
        super.doMoreInst(
            new Operation[]{Operation.P},
            new String[]{"p"},
            pCtx.getReadType()
        );
        return new PythonWInst().execute(Instruction.success(pCtx.getReadType(), Operation.W, null), pCtx);
    }
}
