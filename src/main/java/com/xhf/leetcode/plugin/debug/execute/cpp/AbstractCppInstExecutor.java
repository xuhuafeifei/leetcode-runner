package com.xhf.leetcode.plugin.debug.execute.cpp;

import com.xhf.leetcode.plugin.debug.execute.AbstractInstExecutor;
import com.xhf.leetcode.plugin.debug.execute.ExecuteContext;
import com.xhf.leetcode.plugin.debug.execute.ExecuteResult;
import com.xhf.leetcode.plugin.debug.execute.InstExecutor;
import com.xhf.leetcode.plugin.debug.execute.cpp.gdb.CppGdbInfo;
import com.xhf.leetcode.plugin.debug.execute.cpp.gdb.GdbParser;
import com.xhf.leetcode.plugin.debug.instruction.Instruction;
import com.xhf.leetcode.plugin.exception.DebugError;
import com.xhf.leetcode.plugin.utils.GsonUtils;
import org.jetbrains.annotations.NotNull;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public abstract class AbstractCppInstExecutor extends AbstractInstExecutor {

    protected final GdbParser gdbParser;

    public AbstractCppInstExecutor() {
        this.gdbParser = GdbParser.getInstance();
    }

    @Override
    public ExecuteResult execute(@NotNull Instruction inst, @NotNull ExecuteContext context) {
        if (! (context instanceof CppContext) ) {
            throw new DebugError("context must be instance of com.xhf.leetcode.plugin.debug.execute.cpp.CppContext in cpp executor");
        }
        var pCtx = (CppContext) context;
        // 前置处理inst
        doPre(inst, pCtx);
        ExecuteResult r = doExecute(inst, pCtx, getGdbCommand(inst, pCtx));
        // 恢复被转义的\n, 在cpp server端, 处理json数据时, 会将\n转义为\\n, 所以需要恢复
        r.setMoreInfo(r.getMoreInfo() == null ? "" : r.getMoreInfo().replace("\\n", "\n"));
        r.setResult(r.getResult() == null ? "" : r.getResult().replace("\\n", "\n"));
        r.setOperation(inst.getOperation());
        // 后置处理result
        doAfter(r, pCtx);
        return r;
    }

    private void doPre(Instruction inst, CppContext pCtx) {

    }

    protected void doAfter(ExecuteResult r, CppContext pCtx) {

    }

    protected abstract String getGdbCommand(@NotNull Instruction inst, CppContext pCtx);

    /**
     * 执行逻辑, 发送指令给GDB服务
     * @param inst inst
     * @param pCtx pCtx
     * @param gdbCommand gdbCommand
     * @return ExecuteResult
     */
    protected ExecuteResult doExecute(Instruction inst, CppContext pCtx, String gdbCommand) {
        CppClient cppClient = pCtx.getCppClient();
        return cppClient.postRequest(inst.getOperation().getName(), gdbCommand);
    }

    protected CppGdbInfo getCppGdbInfo(ExecuteResult r) {
        String moreInfo = r.getMoreInfo();
        return GsonUtils.fromJson(moreInfo, CppGdbInfo.class);
    }

    /**
     * 如果cppGdbInfo为false, 返回true, 并处理ExecuteResult
     * @param r r
     * @param cppGdbInfo info
     * @return boolean true表示gdb执行发生错误
     */
    protected boolean handleError(ExecuteResult r, CppGdbInfo cppGdbInfo) {
        if (isGdbError(cppGdbInfo)) {
            r.setSuccess(false);
            r.setHasResult(false);
            r.setMsg(cppGdbInfo.getResultRecord());
            return false;
        }
        return true;
    }

    protected boolean isGdbError(CppGdbInfo cppGdbInfo) {
        return "error".equals(cppGdbInfo.getStatus());
    }
}
