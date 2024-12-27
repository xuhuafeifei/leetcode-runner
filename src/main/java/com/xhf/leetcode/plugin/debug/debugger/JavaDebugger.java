package com.xhf.leetcode.plugin.debug.debugger;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.StepRequest;
import com.xhf.leetcode.plugin.debug.DebugManager;
import com.xhf.leetcode.plugin.debug.env.AbstractDebugEnv;
import com.xhf.leetcode.plugin.debug.env.DebugEnv;
import com.xhf.leetcode.plugin.debug.env.JavaDebugEnv;
import com.xhf.leetcode.plugin.debug.execute.*;
import com.xhf.leetcode.plugin.debug.execute.java.Context;
import com.xhf.leetcode.plugin.debug.execute.java.JavaBInst;
import com.xhf.leetcode.plugin.debug.execute.java.JavaInstFactory;
import com.xhf.leetcode.plugin.debug.instruction.Instruction;
import com.xhf.leetcode.plugin.debug.output.Output;
import com.xhf.leetcode.plugin.debug.command.operation.Operation;
import com.xhf.leetcode.plugin.debug.output.OutputHelper;
import com.xhf.leetcode.plugin.debug.reader.InstReader;
import com.xhf.leetcode.plugin.debug.reader.InstSource;
import com.xhf.leetcode.plugin.debug.reader.ReadType;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import com.xhf.leetcode.plugin.exception.DebugError;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;
import com.xhf.leetcode.plugin.io.console.utils.ConsoleDialog;
import com.xhf.leetcode.plugin.setting.AppSettings;
import com.xhf.leetcode.plugin.utils.LogUtils;
import com.xhf.leetcode.plugin.utils.ViewUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

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
        vm.dispose();
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
            ConsoleUtils.getInstance(project).showWaring(ex.toString(), false, true, ex.toString(), "debug异常", ConsoleDialog.ERROR);
            LogUtils.error(ex);
        } catch (Exception e) {
            ConsoleUtils.getInstance(project).showWaring(e.toString(), false, true, e.toString(), "未知异常", ConsoleDialog.ERROR);
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
        arguments.get("options").setValue("-classpath " + env.getFilePath()); // 指定类路径

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
     * 开始处理event
     * @param vm
     */
    private void startProcessEvent(VirtualMachine vm) {
        EventRequestManager erm = vm.eventRequestManager();

        this.vm = vm;
        this.erm = erm;

        // 设置类加载事件监听
        ClassPrepareRequest classPrepareRequest = erm.createClassPrepareRequest();
        classPrepareRequest.enable();

        try {
            processEvent();
        } catch (InterruptedException e) {
            throw new DebugError("事件处理失败", e);
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

    /**
     * 核心方法, 读取instruction, 执行debug流程
     */
    private void processEvent() throws InterruptedException {
        context.setErm(erm);
        context.setVm(vm);
        context.setEnv(env);
        context.setBreakpointRequestList(this.breakpointRequests);
        context.setProject(project);
        // 启动事件循环
        EventQueue eventQueue = vm.eventQueue();
        while (DebugManager.getInstance(project).isDebug()) {
            EventSet eventSet;
            // vm断开连接, 结束断点
            try {
                eventSet = eventQueue.remove();
            } catch (VMDisconnectedException e) {
                LogUtils.simpleDebug("vm disconnected, done !");
                return;
            }
            Iterator<Event> itr = eventSet.iterator();
            context.setEventSet(eventSet);
            context.setItrEvent(itr);

            while (itr.hasNext()) {
                Event event = itr.next();
                if (! DebugManager.getInstance(project).isDebug()) {
                    return;
                }
                if (event instanceof ClassPrepareEvent) {
                    InitBreakPoint(event);
                } else if (event instanceof BreakpointEvent) {
                    handleBreakpoint(event);
                } else if (event instanceof StepEvent) {
                    handleStepEvent(event);
                }
                eventSet.resume();
            }
        }
    }

    /**
     * 核心运行方法, 负责读取指令, 并执行
     */
    public void doRun(Event event) {
        // event类型检测
        if (! (event instanceof LocatableEvent)) {
            throw new DebugError("event类型错误, 不是LocatableEvent");
        }
        setContextBasicInfo((LocatableEvent) event);

        while (DebugManager.getInstance(project).isDebug()) {
            ProcessResult pR = processDebugCommand();
            if (pR.isContinue) {
                continue;
            } else if (pR.isReturn) {
                return;
            }
            if (!pR.isSuccess) {
                throw new DebugError("未知异常! debug 指令执行错误!");
            }
            Instruction inst = pR.inst;
            // 如果是运行类的指令, 则跳出循环, 运行vm
            if (inst.getOperation() == Operation.R ||
                    inst.getOperation() == Operation.N ||
                    inst.getOperation() == Operation.STEP
            ) {
                // 消费所有事件
                context.consumeAllEvent();
                break;
            }
        }
    }


    private void handleStepEvent(Event event) {
        doRun(event);
    }

    /**
     * 设置基础信息, 比如location和thread
     * @param event
     */
    private void setContextBasicInfo(LocatableEvent event) {
        context.setLocation(event.location());
        context.setThread(event.thread());
    }

    private void handleBreakpoint(Event event) {
        BreakpointEvent breakpointEvent = (BreakpointEvent) event;

        Location location = breakpointEvent.location();
        String className = location.declaringType().name(); // 类名
        String methodName = location.method().name(); // 方法名
        int lineNumber = location.lineNumber(); // 行号

        String res = className + "." + methodName + ":" + lineNumber;

        DebugUtils.simpleDebug("Hit breakpoint at: " + res, project);

        context.setBreakpointEvent(breakpointEvent);

        // 设置单步请求
        setContextBasicInfo((LocatableEvent) event); // 这是初始化方法, 不能删除...
        context.setStepRequest(StepRequest.STEP_LINE, StepRequest.STEP_INTO);

        if (AppSettings.getInstance().isUIOutput()) {
            // 输入UI打印指令. UI总是会在遇到断点时打印局部变量. 因此输入P指令
            InstSource.uiInstInput(Instruction.success(ReadType.UI_IN, Operation.P, ""));
            // 高亮指令
            InstSource.uiInstInput(Instruction.success(ReadType.UI_IN, Operation.W, ""));
        }

        doRun(event);
    }

    private void InitBreakPoint(Event event) {
        ClassPrepareEvent classPrepareEvent = (ClassPrepareEvent) event;
        String className = classPrepareEvent.referenceType().name();

        if (className.equals("Solution")) {
            // ui读取模式下, 初始化断点
            if (AppSettings.getInstance().isUIReader()) {
                uiBreakpointInit(classPrepareEvent);
            }else {
                commandBreakpointInit(classPrepareEvent);
            }
        }
    }

    private void commandBreakpointInit(ClassPrepareEvent classPrepareEvent) {
        // 获取solutionClass
        ClassType solutionClass = (ClassType) classPrepareEvent.referenceType();
        // 获取solution类的核心方法
        String methodName = env.getMethodName();
        Method mainMethod = solutionClass.methodsByName(methodName).get(0);
        Location location = mainMethod.location();

        context.setLocation(location);
        context.setSolutionLocation(location);

        // 设置断点
        new JavaBInst().execute(Instruction.success(ReadType.UI_IN, Operation.B, String.valueOf(location.lineNumber())), context);

        DebugUtils.simpleDebug("break point set at Solution." + mainMethod + " line " + location.lineNumber(), project);
    }

    private void uiBreakpointInit(ClassPrepareEvent classPrepareEvent) {
        // 获取solutionClass
        ClassType solutionClass = (ClassType) classPrepareEvent.referenceType();
        // 获取solution类的核心方法
        String methodName = env.getMethodName();
        Method mainMethod = solutionClass.methodsByName(methodName).get(0);
        // 获取location
        Location location = mainMethod.location();

        // 存储solution的location
        context.setSolutionLocation(location);
        context.setLocation(location);

        // 获取所有断点
        List<XBreakpoint<?>> allBreakpoint = DebugUtils.getAllBreakpoint(project);
        for (XBreakpoint<?> breakpoint : allBreakpoint) {
            XSourcePosition position = breakpoint.getSourcePosition();
            if (position == null) {
                continue;
            }
            VirtualFile file = Objects.requireNonNull(position).getFile();
            // 如果file和当前打开的vile一致, 设置断点信息
            if (file.equals(ViewUtils.getCurrentOpenVirtualFile(project))) {
                Instruction instruction = DebugUtils.buildBInst(position);
                // 设置断点
                new JavaBInst().execute(instruction, context);
            }
        }
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
