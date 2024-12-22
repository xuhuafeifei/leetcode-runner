package com.xhf.leetcode.plugin.debug.debugger;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.sun.jdi.*;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.event.*;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.StepRequest;
import com.xhf.leetcode.plugin.debug.env.AbstractDebugEnv;
import com.xhf.leetcode.plugin.debug.env.JavaDebugEnv;
import com.xhf.leetcode.plugin.debug.execute.*;
import com.xhf.leetcode.plugin.debug.output.Output;
import com.xhf.leetcode.plugin.debug.params.Instrument;
import com.xhf.leetcode.plugin.debug.params.Operation;
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

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class JavaDebugger implements Debugger {
    private final Project project;
    private final JavaDebugConfig config;
    private JavaDebugEnv env;
    private EventRequestManager erm;
    private VirtualMachine vm;

    private StepRequest stepRequest;
    private Context context;
    private Output output;
    private InstReader reader;
    private List<BreakpointRequest> breakpointRequests;
    /**
     * 服务启动端口
     */
    private int port;


    public JavaDebugger(Project project, JavaDebugConfig config) {
        this.project = project;
        this.config = config;
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

    private void startDebug() {
        env.startDebug();
        try {
            startVMService();
            connectVM();
        } catch (DebugError ex) {
            ConsoleUtils.getInstance(project).showWaring(ex.toString(), false, true, ex.toString(), "debug异常", ConsoleDialog.ERROR);
            LogUtils.error(ex);
        } catch (Exception e) {
            ConsoleUtils.getInstance(project).showWaring(e.toString(), false, true, e.toString(), "未知异常", ConsoleDialog.ERROR);
            LogUtils.error(e);
        }
        env.stopDebug();
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

        // 获取当前类的调试信息
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

    /**
     * 核心方法, 读取Instrument, 执行debug流程
     */
    private void processEvent() throws InterruptedException {
        this.context = new Context();
        context.setErm(erm);
        context.setVm(vm);
        context.setEnv(env);
        context.setBreakpointRequestList(this.breakpointRequests);
        context.setProject(project);
        // 启动事件循环
        EventQueue eventQueue = vm.eventQueue();
        while (true) {
            EventSet eventSet;
            // vm断开连接, 结束断点
            try {
                eventSet = eventQueue.remove();
            } catch (VMDisconnectedException e) {
                LogUtils.simpleDebug("vm disconnected, done !");
                return;
            }
            context.setEventSet(eventSet);
            for (Event event : eventSet) {
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
    public void doRun() {
        while (AbstractDebugEnv.isDebug()) {
            Instrument inst = reader.readInst();

            if (! inst.isSuccess()) {
                ReadType readType = inst.getReadType();
                switch (readType) {
                    case COMMAND_IN:
                    case STD_IN:
                        DebugUtils.simpleDebug("命令错误", project);
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
                continue;
            }

            InstExecutor instExecutor = JavaInstFactory.getInstance().create(inst);

            ExecuteResult r;
            try {
                r = instExecutor.execute(inst, context);
            } catch (Exception e) {
                DebugUtils.simpleDebug("指令执行异常: " + e, project);
                LogUtils.error(e);
                continue;
            }
            // 设置上下文
            r.setContext(context);
            output.output(r);
            if (! r.isSuccess()) {
                // 错误结果日志记录
                LogUtils.simpleDebug(r.getMsg());
            }

            // 如果是运行类的指令, 则跳出循环, 运行vm
            if (inst.getOperation() == Operation.R ||
                    inst.getOperation() == Operation.N ||
                    inst.getOperation() == Operation.STEP
            ) {
                break;
            }
        }
    }

    private void handleStepEvent(Event event) {
        StepEvent stepEvent = (StepEvent) event;
        context.setStepEvent(stepEvent);
        setContextBasicInfo((LocatableEvent) event);
        doRun();
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
        setContextBasicInfo(breakpointEvent);

        // 设置单步请求
        if (stepRequest == null) {
            stepRequest = erm.createStepRequest(breakpointEvent.thread(), StepRequest.STEP_LINE, StepRequest.STEP_INTO);
            context.setStepRequest(stepRequest);
        }
        try {
            stepRequest.enable();
        } catch (Exception e) {
            // 未知错误, 就很干. 我都不知道为啥出错, fuck
            LogUtils.simpleDebug(e.toString());
            return;
        }

        if (AppSettings.getInstance().isUIOutput()) {
            // 输入UI打印指令. UI总是会在遇到断点时打印局部变量. 因此输入P指令
            InstSource.uiInstInput(Instrument.success(ReadType.UI_IN, Operation.P, ""));
            // 高亮指令
            InstSource.uiInstInput(Instrument.success(ReadType.UI_IN, Operation.W, ""));
        }

        doRun();
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
        new JavaBInst().execute(Instrument.success(ReadType.UI_IN, Operation.B, String.valueOf(location.lineNumber())), context);

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

        List<XBreakpoint<?>> allBreakpoint = DebugUtils.getAllBreakpoint(project);
        for (XBreakpoint<?> breakpoint : allBreakpoint) {
            XSourcePosition position = breakpoint.getSourcePosition();
            if (position == null) {
                continue;
            }
            VirtualFile file = Objects.requireNonNull(position).getFile();
            // 如果file和当前打开的vile一致, 设置断点信息
            if (file.equals(ViewUtils.getCurrentOpenVirtualFile(project))) {
                Instrument instrument = DebugUtils.buildBInst(position);
                // 设置断点
                new JavaBInst().execute(instrument, context);
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

    /**
     * 获取可用端口
     * @return port
     */
    public static int findAvailablePort() {
        // socket使用完后立刻关闭
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new DebugError("Failed to find an available port", e);
        }
    }
    private void startVMService() {
        // 测试
        this.port = findAvailablePort();
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
