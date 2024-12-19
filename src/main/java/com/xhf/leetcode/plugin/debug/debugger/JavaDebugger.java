package com.xhf.leetcode.plugin.debug.debugger;

import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;
import com.sun.jdi.*;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.event.*;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.StepRequest;
import com.xhf.leetcode.plugin.debug.env.JavaDebugEnv;
import com.xhf.leetcode.plugin.debug.execute.Context;
import com.xhf.leetcode.plugin.debug.execute.ExecuteResult;
import com.xhf.leetcode.plugin.debug.execute.InstExecutor;
import com.xhf.leetcode.plugin.debug.execute.JavaInstFactory;
import com.xhf.leetcode.plugin.debug.output.Output;
import com.xhf.leetcode.plugin.debug.output.StdOutput;
import com.xhf.leetcode.plugin.debug.params.InstParserImpl;
import com.xhf.leetcode.plugin.debug.params.Instrument;
import com.xhf.leetcode.plugin.debug.params.Operation;
import com.xhf.leetcode.plugin.debug.reader.InstReader;
import com.xhf.leetcode.plugin.debug.reader.StdInReader;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import com.xhf.leetcode.plugin.exception.DebugError;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;
import com.xhf.leetcode.plugin.io.console.utils.ConsoleDialog;
import com.xhf.leetcode.plugin.utils.LogUtils;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
//        env = new JavaDebugEnv();
        // 为了测试方便, 暂时不调用Prepare
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
        // 需要开启新线程, 否则会阻塞idea渲染UI的主线程
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
        String res;

        while (true) {
            res = reader.readInst();
            DebugUtils.simpleDebug("command = " + res, project, ConsoleViewContentType.USER_INPUT);
            if (res == null || res.equals("bk") || res.equals("exit")) {
                return;
            }
            // 解析指令, 并执行
            Instrument parse = new InstParserImpl().parse(res);
            if (parse == null) {
                DebugUtils.simpleDebug("指令解析失败", project);
                continue;
            }
            JavaInstFactory instance = JavaInstFactory.getInstance();
            InstExecutor instExecutor = instance.create(parse);

            ExecuteResult r;
            try {
                r = instExecutor.execute(parse, context);
            } catch (Exception e) {
                DebugUtils.simpleDebug("指令执行异常: " + e, project);
                continue;
            }
            if (r.isSuccess()) {
                if (r.isHasResult()) {
                    output.output(r.getResult());
                }
            }else {
                output.output(r.getMsg());
                // 错误结果日志记录
                LogUtils.simpleDebug(r.getMsg());
            }

            // 如果是运行类的指令, 则跳出循环, 运行vm
            if (parse.getOperation() == Operation.R || parse.getOperation() == Operation.N) {
                break;
            }
        }
    }

    private void handleStepEvent(Event event) {
        StepEvent stepEvent = (StepEvent) event;
        context.setStepEvent(stepEvent);
        context.setLocation(stepEvent.location());
        doRun();
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
        context.setLocation(location);

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

        doRun();
    }

    private void InitBreakPoint(Event event) {
        ClassPrepareEvent classPrepareEvent = (ClassPrepareEvent) event;
        String className = classPrepareEvent.referenceType().name();
        if (className.equals("Solution")) {
            // 获取Main类
            ClassType mainClass = (ClassType) classPrepareEvent.referenceType();
            // todo: 这块写"findMedianSortedArrays"死了, 记得改回来: env.getMethodName()
            String methodName = env.getMethodName();
            Method mainMethod = mainClass.methodsByName(methodName).get(0);
            Location location = mainMethod.location();

            BreakpointRequest breakpointRequest = erm.createBreakpointRequest(location);
            breakpointRequest.enable();
            this.breakpointRequests.add(breakpointRequest);

            DebugUtils.simpleDebug("break point set at Solution." + mainMethod + " line " + location.lineNumber(), project);
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
        String startCmd = String.format("%s -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=%d %s",
                env.getJava(), port, "Main");

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
        DebugUtils.printProcess(exec, true);
    }
}
