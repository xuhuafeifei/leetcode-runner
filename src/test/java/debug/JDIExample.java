package debug;

import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequestManager;

import java.io.IOException;
import java.util.*;
import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.StepRequest;
import com.xhf.leetcode.plugin.debug.execute.*;
import com.xhf.leetcode.plugin.debug.params.InstParserImpl;
import com.xhf.leetcode.plugin.debug.params.Instrument;
import com.xhf.leetcode.plugin.debug.params.Operation;
import com.xhf.leetcode.plugin.debug.reader.ReadType;
import com.xhf.leetcode.plugin.debug.reader.StdInReader;

import java.util.*;

public class JDIExample {

    static Context context = new Context();
    static StepRequest stepRequest;
    public static void main(String[] args) throws IOException, InterruptedException {
        Runtime.getRuntime().exec("E:\\jdk8\\bin\\javac -g -encoding utf-8 -cp E:\\java_code\\lc-test\\cache\\debug E:\\java_code\\lc-test\\cache\\debug\\Main.java");
        Runtime.getRuntime().exec("E:\\java_code\\lc-test\\cache\\debug\\start.cmd");
        Thread.sleep(1500);
        try {
            // 创建连接
            VirtualMachineManager vmm = Bootstrap.virtualMachineManager();
            AttachingConnector connector = getConnector(vmm);

            // 配置调试连接信息
            Map<String, Connector.Argument> arguments = connector.defaultArguments();
            arguments.get("port").setValue("5005");

            // 连接到目标 JVM
            VirtualMachine vm = connector.attach(arguments);

            // 获取当前类的调试信息
            EventRequestManager erm = vm.eventRequestManager();

            // 设置类加载事件监听
            ClassPrepareRequest classPrepareRequest = erm.createClassPrepareRequest();
            classPrepareRequest.enable();

            // 启动事件循环
            EventQueue eventQueue = vm.eventQueue();

            context.setErm(erm);

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
//                            setBreakpointAtLine(mainClass, 10, erm, "main");
                            System.out.println("Breakpoint set at Main.main");
                        }
                        if (className.equals("Solution")) {
                            // 获取Main类
                            ClassType mainClass = (ClassType) classPrepareEvent.referenceType();
                            Method mainMethod = mainClass.methodsByName("combinationSum").get(0);
//                            Location location = mainMethod.location();
//
//                            BreakpointRequest breakpointRequest = erm.createBreakpointRequest(location);
//                            breakpointRequest.enable();


                            setBreakpointAtLine(mainClass, 16, erm, "combinationSum");
                            System.out.println("Breakpoint set at Main.main");
                        }
                        eventSet.resume();
                    }
                    else if (event instanceof BreakpointEvent) {
                        BreakpointEvent breakpointEvent = (BreakpointEvent) event;
                        System.out.println("Hit breakpoint at: " + breakpointEvent.location());

                        context.setBreakpointEvent(breakpointEvent);

                        // 设置单步请求
                        if (stepRequest == null) {
                            stepRequest = erm.createStepRequest(breakpointEvent.thread(), StepRequest.STEP_LINE, StepRequest.STEP_INTO);
                            context.setStepRequest(stepRequest);
                        }
                        stepRequest.enable();

                        eventSet.resume();
                    }
                    else if (event instanceof StepEvent) {
                        StepEvent stepEvent = (StepEvent) event;
                        context.setLocation(stepEvent.location());
                        context.setThread(stepEvent.thread());
                        ExecuteResult execute = new JavaPInst().execute(Instrument.success(ReadType.COMMAND_IN, Operation.R, ""), context);
                        if (execute.isSuccess()) {
                            System.out.println(execute.getResult());
                        }
//                        StdInReader reader = new StdInReader();
//                        String res;
//                        context.setStepEvent(stepEvent);
//                        while (true) {
//                            res = reader.readInst();
//                            Instrument parse = new InstParserImpl().parse(res);
//                            JavaInstFactory instance = JavaInstFactory.getInstance();
//                            InstExecutor instExecutor = instance.create(parse);
//
//                            context.setEventSet(eventSet);
//
//                            ExecuteResult r = instExecutor.execute(parse, context);
//                            if (r.isHasResult()) {
//                                System.out.println(r.getResult());
//                            }
//
//                            if (parse.getOperation() == Operation.R || parse.getOperation() == Operation.N) {
//                                break;
//                            }
//                        }
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
    public static void doRead() {

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

    // 打印当前线程的调用栈
    private static void printStackTrace(ThreadReference thread) {
        try {
            // 获取当前线程的堆栈帧
            List<StackFrame> frames = thread.frames();
            System.out.println("Stack trace:");
            for (StackFrame frame : frames) {
                // 打印每一帧的方法名称和类名
                System.out.println("  at " + frame.location().declaringType().name() + "." + frame.location().method().name() +
                        " (" + frame.location().sourceName() + ":" + frame.location().lineNumber() + ")");
            }

            // 获取当前线程的堆栈帧
            StackFrame frame = thread.frame(0);  // 获取栈中的第一个帧（即当前执行的函数）
            Location location = frame.location();  // 获取当前执行的位置

            // 获取方法名和类名
            String className = location.declaringType().name();
            String methodName = location.method().name();
            System.out.println("Currently executing in: " + className + "." + methodName);

            // 获取局部变量
            List<LocalVariable> localVariables = frame.visibleVariables();  // 获取所有可见的局部变量
            for (LocalVariable localVar : localVariables) {
                // 获取每个局部变量的值
                Value varValue = frame.getValue(localVar);

                ((ArrayReference) varValue).getValues();
                System.out.println("Variable: " + localVar.name() + " = " + varValue);
            }

            // 获取字段值（例如，如果当前堆栈帧与对象相关）
            if (location.declaringType() instanceof ClassType) {
                ClassType classType = (ClassType) location.declaringType();
                List<Field> fields = classType.fields();
                for (Field field : fields) {
                    System.out.println(field.typeName());
//                    Value fieldValue = thread.frame(0).getValue(field);
//                    System.out.println("Field: " + field.name() + " = " + fieldValue);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
