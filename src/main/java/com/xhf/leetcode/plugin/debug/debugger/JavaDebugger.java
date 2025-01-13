package com.xhf.leetcode.plugin.debug.debugger;

import com.intellij.openapi.project.Project;
import com.sun.jdi.Bootstrap;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VirtualMachineManager;
import com.sun.jdi.connect.*;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequestManager;
import com.xhf.leetcode.plugin.debug.DebugManager;
import com.xhf.leetcode.plugin.debug.command.operation.Operation;
import com.xhf.leetcode.plugin.debug.env.DebugEnv;
import com.xhf.leetcode.plugin.debug.env.JavaDebugEnv;
import com.xhf.leetcode.plugin.debug.execute.ExecuteResult;
import com.xhf.leetcode.plugin.debug.execute.java.Context;
import com.xhf.leetcode.plugin.debug.execute.java.JavaInstFactory;
import com.xhf.leetcode.plugin.debug.instruction.Instruction;
import com.xhf.leetcode.plugin.debug.output.Output;
import com.xhf.leetcode.plugin.debug.output.OutputHelper;
import com.xhf.leetcode.plugin.debug.reader.InstReader;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import com.xhf.leetcode.plugin.exception.DebugError;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;
import com.xhf.leetcode.plugin.io.console.utils.ConsoleDialog;
import com.xhf.leetcode.plugin.utils.LogUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class JavaDebugger extends AbstractDebugger {
    private final JavaDebugConfig config;
    private JavaDebugEnv env;
    private EventRequestManager erm;
    private VirtualMachine vm;
    private Context context;
    private Output output;
    private InstReader reader;
    private List<BreakpointRequest> breakpointRequests;
    /**
     * 服务启动端口
     */
    @Deprecated // 不适用远程debug方式
    private int port;
    /**
     * 用于输出debug过程中, 代码的std out/ std error
     */
    private final OutputHelper outputHelper;
    /**
     * java debug event handler
     */
    private JEventHandler handler;


    public JavaDebugger(Project project, JavaDebugConfig config) {
        super(project, new Context(), config, JavaInstFactory.getInstance());
        this.context = (Context) super.basicContext;
        this.config = config;
        this.outputHelper = new OutputHelper(project);
    }

    @Override
    public void start() {
        env = new JavaDebugEnv(project);
        try {
            if (!env.prepare()) {
                env.stopDebug();
                return;
            }
        } catch (DebugError e) {
            ConsoleUtils.getInstance(project).showError(e.toString(), false, true);
            LogUtils.warn(DebugUtils.getStackTraceAsString(e));
            return;
        } catch (Exception e) {
            ConsoleUtils.getInstance(project).showError(e.toString(), false, true);
            LogUtils.error(e);
            return;
        }
        // 启动debug
        this.output = config.getOutput();
        this.reader = config.getReader();
        this.breakpointRequests = new ArrayList<>();
        // 需要开启新线程, 否则指令读取操作会阻塞idea渲染UI的主线程
        new Thread(this::startDebug).start();
    }

    @Override
    public void stop() {
        // 已经停止了, 无需再次停止
        if (!DebugManager.getInstance(project).isDebug()) {
            return;
        }
        DebugUtils.simpleDebug("JavaDebugger即将停止!", project);
        env.stopDebug();
        try {
            vm.dispose();
        } catch (VMDisconnectedException ignored) {
        }
        DebugUtils.simpleDebug("JavaDebugger停止!", project);
    }

    @Override
    public DebugEnv getEnv() {
        return this.env;
    }

    private void startDebug() {
        env.startDebug();
        try {
            debugLocally();
            // debugRemotely(); // 废弃远程debug方式
        } catch (DebugError ex) {
            ConsoleUtils.getInstance(project).showWaring(ex.getMessage(), false, true, ex.getMessage(), "debug异常", ConsoleDialog.ERROR);
            LogUtils.error(ex);
        } catch (Exception e) {
            ConsoleUtils.getInstance(project).showWaring(e.getMessage(), false, true, e.getMessage(), "未知异常", ConsoleDialog.ERROR);
            LogUtils.error(e);
        }
        DebugManager.getInstance(project).stopDebugger();
    }


    /**
     * 本地断点启动
     */
    private void debugLocally() {
        // 获取 LaunchingConnector
        LaunchingConnector connector = Bootstrap.virtualMachineManager().defaultConnector();

        // 配置启动参数
        Map<String, Connector.Argument> arguments = connector.defaultArguments();
        arguments.get("main").setValue("Main"); // 替换为你的目标类全路径
        arguments.get("options").setValue("-classpath " + env.getFilePath() + " -Dencoding=utf-8"); // 指定类路径
        // fix: 编译jdk和运行jdk不一致问题
        arguments.get("home").setValue(env.getJAVA_HOME());
        arguments.get("vmexec").setValue("java.exe");

        // 启动目标 JVM
        VirtualMachine vm;
        try {
            vm = connector.launch(arguments);
        } catch (IOException | IllegalConnectorArgumentsException | VMStartException e) {
            throw new DebugError("vm启动失败!", e);
        }

        // 捕获目标虚拟机的输出
        captureStream(vm.process().getInputStream(), OutputHelper.STD_OUT);
        captureStream(vm.process().getErrorStream(), OutputHelper.STD_ERROR);

        // 获取当前类的调试信息
        startProcessEvent(vm);
    }

    private void captureStream(InputStream stream, String streamName) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "GBK"))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        ExecuteResult success = ExecuteResult.success(null, line + "\n");
                        success.setMoreInfo(streamName);
                        outputHelper.output(success);
                    }
                } catch (Exception e) {
                    LogUtils.error(e);
                }
            }
        }).start();

    }

    /**
     * 开始处理debug流程
     * @param vm
     */
    private void startProcessEvent(VirtualMachine vm) {
        EventRequestManager erm = vm.eventRequestManager();

        // 设置类加载事件监听
        ClassPrepareRequest classPrepareRequest = erm.createClassPrepareRequest();
        classPrepareRequest.enable();

        this.vm = vm;
        this.erm = erm;

        context.addOutput(output);
        context.setErm(erm);
        context.setVm(vm);
        context.setEnv(env);
        context.setBreakpointRequestList(this.breakpointRequests);
        context.setProject(project);

        doRun();
    }

    @Override
    protected void doAfterReadInstruction(Instruction inst) {
        // 判断是否是运行类操作
        Operation op = inst.getOperation();
        if (op == Operation.N || op == Operation.R || op == Operation.STEP) {
            // 需要等待JEventHandler
            context.waitFor();
        }
    }

    /**
     * 核心运行方法, 负责读取指令, 并执行
     * 同时启动事件处理器, 处理VM debug过程中遇到的event
     */
    public void doRun() {
        this.handler = new JEventHandler(context);

        while (DebugManager.getInstance(project).isDebug()) {
            // 确保执行指令时, JEventHandler已经处理好必要逻辑
            context.waitForJEventHandler("doRun");

            ProcessResult pR;
            try {
                pR = processDebugCommand();
            } catch (VMDisconnectedException e) {
                return;
            }
            if (pR.isContinue) {
                continue;
            } else if (pR.isReturn) {
                return;
            }
            if (!pR.isSuccess) {
                throw new DebugError("未知异常! debug 指令执行错误!");
            }
        }
    }

    // 采用本地debug方式, 废弃远程连接
    @Deprecated
    private void debugRemotely() {
        startVMService();
        connectVM();
    }

    /**
     * 连接VM, 开始debug
     */
    @Deprecated // 取消远程连接的debug方式
    private void connectVM() {
        // 创建连接
        VirtualMachineManager vmm = Bootstrap.virtualMachineManager();
        AttachingConnector connector = getConnector(vmm);

        // 配置调试连接信息
        Map<String, Connector.Argument> arguments = connector.defaultArguments();
        arguments.get("port").setValue(String.valueOf(port));

        // 连接到目标 JVM
        VirtualMachine vm = null;
        // 3次连接尝试
        int tryCount = 1;
        do {
            try {
                DebugUtils.simpleDebug("第 " + tryCount + " 次连接, 尝试中...", project);
                vm = connector.attach(arguments);
                DebugUtils.simpleDebug("连接成功", project);
                break;
            } catch (IOException | IllegalConnectorArgumentsException e) {
                DebugUtils.simpleDebug("连接失败: " + e, project);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }
            }
            tryCount++;
        } while (tryCount <= 3);

        if (vm == null) {
            LogUtils.warn("vm 连接失败");
            throw new DebugError("vm 连接失败");
        }

        startProcessEvent(vm);
    }


    // 获取 JDWP 连接器
    private static AttachingConnector getConnector(VirtualMachineManager vmm) {
        for (AttachingConnector connector : vmm.attachingConnectors()) {
            if (connector.transport().name().equals("dt_socket")) {
                return connector;
            }
        }
        throw new RuntimeException("No suitable connector found.");
    }

    // 取消用远程debug的方式
    @Deprecated
    private void startVMService() {
        // 测试
        this.port = DebugUtils.findAvailablePort();
        LogUtils.simpleDebug("get available port : " + this.port);

        String cdCmd = "cd " + env.getFilePath();
        String startCmd = String.format("%s -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=%d -cp %s %s",
                env.getJava(), port, env.getFilePath(), "Main");

        String combinedCmd = "cmd /c " + cdCmd + " & " + startCmd;

        LogUtils.simpleDebug(combinedCmd);

        try {
            Process exec = Runtime.getRuntime().exec(combinedCmd);
            getRunInfo(exec);
        } catch(InterruptedException ignored) {
        } catch (Exception e) {
            throw new DebugError(e.toString(), e);
        }
    }

    private void getRunInfo(Process exec) throws InterruptedException {
        DebugUtils.printProcess(exec, true, project);
    }
}
