package com.xhf.leetcode.plugin.debug.debugger;

import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.debug.execute.ExecuteContext;
import com.xhf.leetcode.plugin.debug.execute.ExecuteResult;
import com.xhf.leetcode.plugin.debug.execute.InstExecutor;
import com.xhf.leetcode.plugin.debug.execute.InstructionFactory;
import com.xhf.leetcode.plugin.debug.execute.java.JavaInstFactory;
import com.xhf.leetcode.plugin.debug.instruction.Instruction;
import com.xhf.leetcode.plugin.debug.output.AbstractOutput;
import com.xhf.leetcode.plugin.debug.output.Output;
import com.xhf.leetcode.plugin.debug.reader.CommandReader;
import com.xhf.leetcode.plugin.debug.reader.InstReader;
import com.xhf.leetcode.plugin.debug.reader.ReadType;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;
import com.xhf.leetcode.plugin.utils.LogUtils;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public abstract class AbstractDebugger implements Debugger{
    protected final Project project;
    protected InstReader reader;
    protected ExecuteContext basicContext;
    protected Output output;
    protected InstructionFactory instFactory;

    public AbstractDebugger(Project project, ExecuteContext context, DebugConfig config, InstructionFactory instFactory) {
        this.project = project;
        this.basicContext = context;
        this.reader = config.getReader();
        this.output = config.getOutput();
        this.instFactory = instFactory;
    }

    @Override
    public void doFailed(Instruction inst) {
        ReadType readType = inst.getReadType();
        switch (readType) {
            case COMMAND_IN:
            case STD_IN:
                DebugUtils.simpleDebug("命令错误", this.project);
                break;
            case UI_IN:
                ConsoleUtils.getInstance(project).showWaring("UI指令错误", false, true);
                LogUtils.warn("UI指令错误 inst = " + inst);
                break;
            default:
                ConsoleUtils.getInstance(project).showWaring("readType未知错误: " + readType.getType(), false, true);
                LogUtils.warn("readType未知错误: " + readType.getType());
                break;
        }
    }

    /**
     * 接受用户输入，处理并执行对应的debug指令，并进行输出
     * @return
     */
    protected ProcessResult processDebugCommand() {
        Instruction inst = reader.readInst();

        // 如果是null, 表示读取InstSource的UI消费阻塞队列被打断, 此时返回null
        if (inst == null) {
            return new ProcessResult(true, true, false, null, null);
        }
        // 如果指令是exit, 直接终止运行
        if (inst.isExit()) {
            this.stop();
            return new ProcessResult(true, false, true, inst, null);
        }
        if (! inst.isSuccess()) {
            doFailed(inst);
            return new ProcessResult(false, true, false, inst, null);
        }

        InstExecutor instExecutor = instFactory.create(inst);

        ExecuteResult r;
        try {
            r = instExecutor.execute(inst, this.basicContext);
        } catch (Exception e) {
            DebugUtils.simpleDebug("指令执行异常: " + e, project);
            LogUtils.error(e);
            return new ProcessResult(false, true, false, inst, null);
        }

        // 执行结果为null
        if (r == null) {
            return new ProcessResult(false, false, false, inst, null);
        }
        // 设置上下文
        r.setContext(this.basicContext);
        this.output.output(r);
        if (! r.isSuccess()) {
            // 错误结果日志记录
            LogUtils.simpleDebug(r.getMsg());
        }
        return new ProcessResult(true, false, false, inst, r);
    }

    protected class ProcessResult {
        boolean isSuccess;
        boolean isContinue;
        boolean isReturn;
        Instruction inst;
        ExecuteResult r;

        public ProcessResult(boolean isSuccess, boolean isContinue, boolean isReturn, Instruction inst, ExecuteResult r) {
            this.isSuccess = isSuccess;
            this.isContinue = isContinue;
            this.isReturn = isReturn;
            this.inst = inst;
            this.r = r;
        }
    }
}
