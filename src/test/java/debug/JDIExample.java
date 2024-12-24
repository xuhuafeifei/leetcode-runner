package debug;

import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequestManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

import com.sun.jdi.request.StepRequest;
import com.xhf.leetcode.plugin.debug.command.operation.Operation;
import com.xhf.leetcode.plugin.debug.execute.*;
import com.xhf.leetcode.plugin.debug.execute.java.Context;
import com.xhf.leetcode.plugin.debug.execute.java.JavaPInst;
import com.xhf.leetcode.plugin.debug.instruction.Instruction;
import com.xhf.leetcode.plugin.debug.reader.ReadType;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;

public class JDIExample {

    static Context context = new Context();
    static StepRequest stepRequest;
    static int b = 16;
    static String methodName = "getKth";

    private static void captureStream(InputStream stream, String streamName) {
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("[" + streamName + "] " + line);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }


    public static void main(String[] args) throws IOException, InterruptedException {
//        Runtime.getRuntime().exec("E:\\jdk8\\bin\\javac -g -encoding utf-8 -cp E:\\java_code\\lc-test\\cache\\debug E:\\java_code\\lc-test\\cache\\debug\\Main.java");
//        Runtime.getRuntime().exec("E:\\java_code\\lc-test\\cache\\debug\\start.cmd");
//        Thread.sleep(1500);
        try {
//            // 创建连接
//            VirtualMachineManager vmm = Bootstrap.virtualMachineManager();
//            AttachingConnector connector = getConnector(vmm);
//
//            // 配置调试连接信息
//            Map<String, Connector.Argument> arguments = connector.defaultArguments();
//            arguments.get("port").setValue("5005");
//
//            // 连接到目标 JVM
//            VirtualMachine vm = connector.attach(arguments);

            // 获取 LaunchingConnector
            LaunchingConnector connector = Bootstrap.virtualMachineManager().defaultConnector();

            // 配置启动参数
            Map<String, Connector.Argument> arguments = connector.defaultArguments();
            arguments.get("main").setValue("Main"); // 替换为你的目标类全路径
            arguments.get("options").setValue("-classpath E:\\java_code\\lc-test\\cache\\debug"); // 指定类路径

            // 启动目标 JVM
            VirtualMachine vm = connector.launch(arguments);


            // 获取当前类的调试信息
            EventRequestManager erm = vm.eventRequestManager();

            // 设置类加载事件监听
            ClassPrepareRequest classPrepareRequest = erm.createClassPrepareRequest();
            classPrepareRequest.enable();

            // 启动事件循环
            EventQueue eventQueue = vm.eventQueue();

            context.setErm(erm);

            // 捕获目标虚拟机的输出
            captureStream(vm.process().getInputStream(), "OUTPUT");
            captureStream(vm.process().getErrorStream(), "ERROR");

            while (true) {
                EventSet eventSet = eventQueue.remove();
                for (Event event : eventSet) {
                    if (event instanceof ClassPrepareEvent) {
                        ClassPrepareEvent classPrepareEvent = (ClassPrepareEvent) event;
                        String className = classPrepareEvent.referenceType().name();
                        if (className.equals("Main")) {
                            // 获取Main类
                            ClassType mainClass = (ClassType) classPrepareEvent.referenceType();
                            Method mainMethod = mainClass.methodsByName("main").get(0);

                            // 设置断点
                            // 在指定行设置断点
                            System.out.println("Breakpoint set at Main.main");
                        }
                        if (className.equals("Solution")) {
                            // 获取Main类
                            ClassType mainClass = (ClassType) classPrepareEvent.referenceType();
                            setBreakpointAtLine(mainClass, b, erm, methodName);
                            System.out.println("Breakpoint set at Main.main");
                        }
                        eventSet.resume();
                    }
                    else if (event instanceof BreakpointEvent) {
                        BreakpointEvent breakpointEvent = (BreakpointEvent) event;
                        System.out.println("Hit breakpoint at: " + breakpointEvent.location());

                        context.setBreakpointEvent(breakpointEvent);

                        context.setThread(breakpointEvent.thread());
                        context.setStepRequest(StepRequest.STEP_LINE, StepRequest.STEP_OVER);

                        eventSet.resume();
                    }
                    else if (event instanceof StepEvent) {
                        StepEvent stepEvent = (StepEvent) event;
                        context.setLocation(stepEvent.location());
                        context.setThread(stepEvent.thread());
                        ExecuteResult execute = new JavaPInst().execute(Instruction.success(ReadType.COMMAND_IN, Operation.R, ""), context);
                        System.err.println(DebugUtils.buildCurrentLineInfoByLocation(execute));
                        if (execute.isSuccess()) {
                            System.out.println(execute.getResult());
                        }
                        eventSet.resume();
                    }
                    else {
                        eventSet.resume();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 在指定行设置断点
    private static void setBreakpointAtLine(ClassType mainClass, int lineNumber, EventRequestManager erm, String methodName) {
        try {
            // 获取方法
            Method mainMethod = mainClass.methodsByName(methodName).get(0);
            // 获取方法的所有位置
            List<Location> locations = mainMethod.allLineLocations();

            // 查找指定行的Location
            for (Location location : locations) {
                if (location.lineNumber() == lineNumber) {
                    // 设置断点
                    BreakpointRequest breakpointRequest = erm.createBreakpointRequest(location);
                    breakpointRequest.enable();
                    System.out.println("Breakpoint set at line " + lineNumber);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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
}
