package com.xhf.leetcode.plugin.debug.debugger;

import com.intellij.openapi.project.Project;
import com.sun.jdi.Bootstrap;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VirtualMachineManager;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.LaunchingConnector;
import com.sun.jdi.connect.VMStartException;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequestManager;
import com.xhf.leetcode.plugin.debug.DebugManager;
import com.xhf.leetcode.plugin.debug.command.operation.Operation;
import com.xhf.leetcode.plugin.debug.env.DebugEnv;
import com.xhf.leetcode.plugin.debug.env.JavaDebugEnv;
import com.xhf.leetcode.plugin.debug.execute.ExecuteResult;
import com.xhf.leetcode.plugin.debug.execute.cpp.KillPortProcess;
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
import com.xhf.leetcode.plugin.io.file.utils.FileUtils;
import com.xhf.leetcode.plugin.utils.BundleUtils;
import com.xhf.leetcode.plugin.utils.LogUtils;
import com.xhf.leetcode.plugin.utils.OSHandler;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class JavaDebugger extends AbstractDebugger {

    private final JavaDebugConfig config;
    private final Context context;
    /**
     * 用于输出debug过程中, 代码的std out/ std error
     */
    private final OutputHelper outputHelper;
    private JavaDebugEnv env;
    private VirtualMachine vm;
    private Output output;
    private List<BreakpointRequest> breakpointRequests;
    /**
     * 服务启动端口
     */
    private int port = -1;
    private String stdLogPath;
    private String stdErrPath;


    public JavaDebugger(Project project, JavaDebugConfig config) {
        super(project, new Context(project), config, JavaInstFactory.getInstance());
        this.context = (Context) super.basicContext;
        this.config = config;
        this.outputHelper = new OutputHelper(project);
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

    @Override
    public void start() {
        env = new JavaDebugEnv(project);
        boolean flag = super.envPrepare(env);
        if (!flag) {
            return;
        }
        // 启动debug
        this.output = config.getOutput();
        InstReader reader = config.getReader();
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
        DebugUtils.simpleDebug(BundleUtils.i18n("debug.leetcode.debug.server.stopsoon"), project);
        env.stopDebug();
        try {
            vm.dispose();
            if (port != -1) {
                // 强制关停端口
                if (DebugUtils.isPortAvailable2("localhost", port)) {
                    LogUtils.info(BundleUtils.i18n("debug.leetcode.debug.server.stop.force") + " port = " + port);
                    KillPortProcess.killProcess(port);
                }
            }
        } catch (VMDisconnectedException ignored) {
        }
        DebugUtils.simpleDebug(BundleUtils.i18n("debug.leetcode.debug.server.stop"), project);
    }

    @Override
    public DebugEnv getEnv() {
        return this.env;
    }

    private void startDebug() {
        env.startDebug();
        try {
            // debugLocally();
            debugRemotely();
        } catch (DebugError ex) {
            ConsoleUtils.getInstance(project)
                .showError(ex.getMessage(), false, true, ex.getMessage(), "debug异常", ConsoleDialog.ERROR);
            LogUtils.error(ex);
        } catch (VMDisconnectedException e) {
            DebugUtils.simpleDebug(BundleUtils.i18n("debug.leetcode.vm.connect.stop"), project, true);
        } catch (Exception e) {
            ConsoleUtils.getInstance(project)
                .showError(e.getMessage(), false, true, e.getMessage(), "未知异常", ConsoleDialog.ERROR);
            LogUtils.error(e);
        }
        if (DebugManager.getInstance(project).isDebug()) {
            DebugManager.getInstance(project).stopDebugger();
        }
    }

    /**
     * 本地断点启动
     */
    @Deprecated
    private void debugLocally() {
        // 获取 LaunchingConnector
        LaunchingConnector connector = Bootstrap.virtualMachineManager().defaultConnector();

        /*
          clion 2024.3.1.1版本, 使用jdk8启动vm时的指令, 该指令会导致报错
          {home=home=E:\jdk8, options=options=-classpath E:\java_code\lc-test\cache\debug\java -Dencoding=utf-8, main=main=Main, suspend=suspend=true, quote=quote=", vmexec=vmexec=java.exe, includevirtualthreads=includevirtualthreads=n}
         */
        // 配置启动参数
        Map<String, Connector.Argument> arguments = connector.defaultArguments();

        // 在clion 2024.3.1.1版本中, defaultArguments会包含 includevirtualthreads, 该参数在使用低版本jdk时会报错
        // 此外, defaultArguments的启动参数决定于jetbrains产品编写使用的jdk版本, 具体参数内容可参考{@link SunCommandLineLauncher.launch}方法
        // 如果jetbrains产品版本过高, 那他将无法兼容低版本jdk的debug启动, 最终抛出异常. 该异常无法被避免, 因此废弃debugLocally方法
        arguments.get("main").setValue("Main"); // 替换为你的目标类全路径
        arguments.get("options").setValue("-classpath " + env.getFilePath() + " -Dencoding=utf-8"); // 指定类路径
        // fix: 编译jdk和运行jdk不一致问题
        arguments.get("home").setValue(env.getJAVA_HOME());
        arguments.get("vmexec").setValue("java.exe");

        List<String> list = Arrays.asList("main", "options", "home", "vmexec", "suspend", "quote", "vmexec");
        for (String key : arguments.keySet()) {
            if (!list.contains(key)) {
                arguments.remove(key);
            }
        }

        DebugUtils.simpleDebug(arguments.toString(), project);
        // 启动目标 JVM
        VirtualMachine vm;
        try {
            vm = connector.launch(arguments);
        } catch (IOException | IllegalConnectorArgumentsException | VMStartException e) {
            LogUtils.warn(DebugUtils.getStackTraceAsString(e));
            throw new DebugError(BundleUtils.i18n("debug.leetcode.vm.start.failed"), e);
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
     */
    private void startProcessEvent(VirtualMachine vm) {
        EventRequestManager erm = vm.eventRequestManager();

        // 设置类加载事件监听
        ClassPrepareRequest classPrepareRequest = erm.createClassPrepareRequest();
        classPrepareRequest.enable();

        this.vm = vm;

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
        /**
         * java debug event handler
         */
        JEventHandler handler = new JEventHandler(context);

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
                throw new DebugError(BundleUtils.i18n("action.leetcode.unknown.error"));
            }
        }
    }

    private void debugRemotely() {
        startVMService();
        connectVM();
    }

    /**
     * 连接VM, 开始debug
     */
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
                DebugUtils.simpleDebug(BundleUtils.i18n("debug.leetcode.connect.try") + tryCount, project);
                vm = connector.attach(arguments);
                DebugUtils.simpleDebug(BundleUtils.i18n("debug.leetcode.server.connect.succuess"), project);
                break;
            } catch (IOException | IllegalConnectorArgumentsException e) {
                DebugUtils.simpleDebug(BundleUtils.i18n("debug.leetcode.server.connect.failed") + " " + e, project);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }
            }
            tryCount++;
        } while (tryCount <= 3);

        if (vm == null) {
            LogUtils.warn("vm 连接失败");
            throw new DebugError(BundleUtils.i18n("debug.leetcode.vm.connect.failed"));
        }

        // 捕获目标虚拟机的输出
        // captureStream(vm.process().getInputStream(), OutputHelper.STD_OUT);
        // captureStream(vm.process().getErrorStream(), OutputHelper.STD_ERROR);

        // 监听stdout/stderr
        captureStd(OutputHelper.STD_OUT, 0, this.stdLogPath, outputHelper);
        captureStd(OutputHelper.STD_ERROR, 1, this.stdErrPath, outputHelper);

        startProcessEvent(vm);
    }

    private void startVMService() {
        // 创建检测标准输出, 标准错误文件
        this.stdLogPath = new FileUtils.PathBuilder(env.getFilePath()).append("javaLog").append("std_log.log").build();
        this.stdErrPath = new FileUtils.PathBuilder(env.getFilePath()).append("javaLog").append("std_err.log").build();
        try {
            FileUtils.removeFile(this.stdLogPath);
            FileUtils.removeFile(this.stdErrPath);
            FileUtils.createAndWriteFile(stdLogPath, "");
            FileUtils.createAndWriteFile(stdErrPath, "");
        } catch (Exception e) {
            String message = BundleUtils.i18n("debug.leetcode.java.log.create.failed") + "\n"
                + "std_log.log = " + stdLogPath + "\n"
                + "std_err_log = " + stdErrPath + "\n";
            LogUtils.simpleDebug(message);
            ConsoleUtils.getInstance(project).showError(message, false);
        }

        this.port = DebugUtils.findAvailablePort();
        LogUtils.simpleDebug("get available port : " + this.port);

        String cdCmd = "cd " + env.getFilePath();
        String startCmd = String.format(
            "%s -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=%d -cp %s %s %s",
            env.getJava(), port, env.getFilePath(), "Main", "> " + stdLogPath + " 2> " + stdErrPath);

        String combinedCmd = cdCmd + " & " + startCmd;

        LogUtils.simpleDebug(combinedCmd);

        try {
            var exec = OSHandler.buildProcess(combinedCmd);
            // DebugUtils.buildProcess("cmd.exe", "/c", combinedCmd);
            getRunInfo(exec);
        } catch (InterruptedException ignored) {
        } catch (Exception e) {
            throw new DebugError(e.toString(), e);
        }
    }

    private void getRunInfo(Process exec) throws InterruptedException {
        DebugUtils.printProcess(exec, true, project);
    }
}
