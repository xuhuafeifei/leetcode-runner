package com.xhf.leetcode.plugin.debug.debugger;

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
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;
import com.xhf.leetcode.plugin.utils.LogUtils;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
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
    private JavaDebugEnv env;
    private EventRequestManager erm;
    private VirtualMachine vm;

    private StepRequest stepRequest;
    private Context context;
    private Output output;
    private InstReader reader;
    private List<BreakpointRequest> breakpointRequests;


    public JavaDebugger(Project project) {
        this.project = project;
    }

    @Override
    public void start() {
        env = new JavaDebugEnv();
        // 为了测试方便, 暂时不调用Prepare
        // env.prepare();
        // 启动debug
        this.output = new StdOutput();
        this.reader = new StdInReader();
        this.breakpointRequests = new ArrayList<>();
        startDebug();
    }

    private void startDebug() {
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
        arguments.get("port").setValue("5005");

        // 连接到目标 JVM
        VirtualMachine vm = null;
        // 3次连接尝试
        int tryCount = 1;
        do {
            try {
                LogUtils.debug("第 " + tryCount + " 次连接, 尝试中...");
                vm = connector.attach(arguments);
                LogUtils.simpleDebug("连接成功!");
                break;
            } catch (IOException | IllegalConnectorArgumentsException e) {
                LogUtils.simpleDebug(e.toString());
            }
            tryCount++;
        } while (tryCount <= 3);

        if (vm == null) {
            LogUtils.error("vm 连接失败");
            return;
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
            throw new RuntimeException(e);
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
            try {
                eventSet = eventQueue.remove();
            } catch (VMDisconnectedException e) {
                LogUtils.simpleDebug("done !");
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
            // 解析指令, 并执行
            Instrument parse = new InstParserImpl().parse(res);
            if (parse == null) {
                LogUtils.simpleDebug("指令解析失败");
                continue;
            }
            JavaInstFactory instance = JavaInstFactory.getInstance();
            InstExecutor instExecutor = instance.create(parse);

            ExecuteResult r;
            try {
                r = instExecutor.execute(parse, context);
            } catch (Exception e) {
                LogUtils.simpleDebug("指令执行异常: " + e);
                continue;
            }
            if (r.isSuccess()) {
                if (r.isHasResult()) {
                    output.output(r.getResult());
                }
            }else {
                output.output(r.getMsg());
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

        LogUtils.simpleDebug("Hit breakpoint at: " + res);

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
        LogUtils.simpleDebug(className);
        if (className.equals("Solution")) {
            // 获取Main类
            ClassType mainClass = (ClassType) classPrepareEvent.referenceType();
            // todo: 这块写"findMedianSortedArrays"死了, 记得改回来: env.getMethodName()
            String methodName = "findMedianSortedArrays";
            Method mainMethod = mainClass.methodsByName(methodName).get(0);
            Location location = mainMethod.location();

            BreakpointRequest breakpointRequest = erm.createBreakpointRequest(location);
            breakpointRequest.enable();
            this.breakpointRequests.add(breakpointRequest);

            LogUtils.simpleDebug("break point set at Solution." + mainMethod + " line " + location.lineNumber());
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

    private void startVMService() {
        // 测试
        int port = 5005;
        String cdCmd = "cd " + env.getFilePath();
        String startCmd = String.format("%s -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=%d %s",
                env.getJava(), port, "Main");

        String combinedCmd = "cmd /c \"" + cdCmd + " & " + startCmd + "\"";

        LogUtils.simpleDebug(combinedCmd);

        try {
            Process exec = Runtime.getRuntime().exec(combinedCmd);
            getRunInfo(exec);

            // 获取cmd执行输出的结果, 并打印在控制台
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void getRunInfo(Process exec) throws InterruptedException {
        new Thread(() -> {
            try {
                LogUtils.simpleDebug("cmd result = " + IOUtils.toString(exec.getInputStream(), StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
        new Thread(() -> {
            try {
                LogUtils.simpleDebug("cmd error result = " + IOUtils.toString(exec.getErrorStream(), StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
        Thread.sleep(1000);
    }
}
