package com.xhf.leetcode.plugin.debug.debugger;

import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.debug.DebugManager;
import com.xhf.leetcode.plugin.debug.execute.ExecuteContext;
import com.xhf.leetcode.plugin.debug.execute.ExecuteResult;
import com.xhf.leetcode.plugin.debug.execute.InstExecutor;
import com.xhf.leetcode.plugin.debug.execute.InstructionFactory;
import com.xhf.leetcode.plugin.debug.instruction.Instruction;
import com.xhf.leetcode.plugin.debug.output.Output;
import com.xhf.leetcode.plugin.debug.output.OutputType;
import com.xhf.leetcode.plugin.debug.reader.InstReader;
import com.xhf.leetcode.plugin.debug.reader.ReadType;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;
import com.xhf.leetcode.plugin.setting.AppSettings;
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
        // 清空consoleView
        ConsoleUtils.getInstance(project).clearConsole();
    }

    @Override
    public void doFailed(Instruction inst) {
        String outputTypeName = AppSettings.getInstance().getOutputTypeName();
        OutputType outputType = OutputType.getByName(outputTypeName);
        if (outputType == null) {
            outputType = OutputType.UI_OUT;
        }
        ReadType readType = inst.getReadType();
        // output决定输出形式
        switch (outputType) {
            case CONSOLE_OUT:
            case STD_OUT:
                DebugUtils.simpleDebug(readType.getType() + "命令错误", this.project);
                break;
            case UI_OUT:
                ConsoleUtils.getInstance(project).showWaring(readType.getType() + "指令错误", false, true);
                LogUtils.warn(readType.getType() + "指令错误 inst = " + inst);
                break;
            default:
                ConsoleUtils.getInstance(project).showWaring("outputType未知错误: " + outputType.getType(), false, true);
                LogUtils.warn("outputType未知错误: " + outputType.getType());
                break;
        }
    }

    /**
     * 接受用户输入，处理并执行对应的debug指令，并进行输出
     * @return
     */
    protected ProcessResult processDebugCommand() {
        Instruction inst;
        try {
            inst = reader.readInst();
        } catch (InterruptedException e) {
            // 此时读取到的是无效指令, 因为是阻塞队列消费被打断而返回的null数据, 因此需要continue, 继续读取数据
            return new ProcessResult(true, true, false, null, null);
        }

        // 如果是null, 表示读取InstSource的UI消费阻塞队列被打断, 此时返回null
        if (inst == null) {
            return new ProcessResult(true, true, false, null, null);
        }
        // 允许子类在读取instruction后执行一些操作
        try {
            doAfterReadInstruction(inst);
        } catch (Exception e) {
            LogUtils.warn("error happens in doAfterReadInstruction! current class is " + this.getClass().getName());
        }

        // debug
        LogUtils.simpleDebug(inst.toString());

        // 如果指令是exit, 直接终止运行
        if (inst.isExit()) {
            DebugManager.getInstance(project).stopDebugger();
            return new ProcessResult(true, false, true, inst, null);
        }
        if (! inst.isSuccess()) {
            doFailed(inst);
            return new ProcessResult(false, true, false, inst, null);
        }

        InstExecutor instExecutor = instFactory.create(inst);
        if (instExecutor == null) {
            DebugUtils.simpleDebug("指令执行异常: " + inst + ". 指令对应的执行器InstExecutor创建为null, 请检查!", project);
            return new ProcessResult(false, true, false, inst, null);
        }

        ExecuteResult r;
        try {
            r = instExecutor.execute(inst, this.basicContext);
        } catch (Exception e) {
            DebugUtils.simpleDebug("指令执行异常: " + e, project);
            LogUtils.error(e);
            return new ProcessResult(false, true, false, inst, null);
        }

        try {
            doAfterExecuteInstruction(r);
        } catch (Exception e) {
            LogUtils.warn("error happens in doAfterExecuteInstruction! current class is " + this.getClass().getName());
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

    /**
     * 允许子类在执行完指令后做出额外操作
     */
    protected void doAfterExecuteInstruction(ExecuteResult r) {

    }

    /**
     * 允许子类重写该方法, 执行某些操作
     * @param inst
     */
    protected void doAfterReadInstruction(final Instruction inst) {

    }

    protected static class ProcessResult {
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
