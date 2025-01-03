package debug;

import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

import com.xhf.leetcode.plugin.debug.debugger.JEventHandler;
import com.xhf.leetcode.plugin.debug.execute.*;
import com.xhf.leetcode.plugin.debug.execute.java.Context;
import com.xhf.leetcode.plugin.debug.execute.java.JavaInstFactory;
import com.xhf.leetcode.plugin.debug.instruction.Instruction;
import com.xhf.leetcode.plugin.debug.output.StdOutput;
import com.xhf.leetcode.plugin.debug.reader.StdInReader;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import com.xhf.leetcode.plugin.utils.LogUtils;

public class JDIExample {

    static Context context = new Context();
    static StepRequest stepRequest;
    static int b = 18;
    static String methodName = "test";

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


    static EventRequestManager erm;

    public static void main(String[] args) throws IOException, InterruptedException, IllegalConnectorArgumentsException, VMStartException {
        // 获取 LaunchingConnector
        LaunchingConnector connector = Bootstrap.virtualMachineManager().defaultConnector();

        // 配置启动参数
        String path = "E:\\java_code\\leetcode-runner\\out\\test\\classes";
//        String path = "E:\\java_code\\lc-test\\cache\\debug\\java";
        Map<String, Connector.Argument> arguments = connector.defaultArguments();
        arguments.get("main").setValue("Main");
        arguments.get("options").setValue("-classpath " + path + " -Dfile.encoding=UTF-8"); // 指定类路径

        // 启动目标 JVM
        VirtualMachine vm = connector.launch(arguments);

        // 获取当前类的调试信息
        erm = vm.eventRequestManager();

        // 设置类加载事件监听
        ClassPrepareRequest classPrepareRequest = erm.createClassPrepareRequest();
        classPrepareRequest.enable();

        // 启动事件循环
        EventQueue eventQueue = vm.eventQueue();

        context.setErm(erm);
        context.setVm(vm);

        // 捕获目标虚拟机的输出
//            captureStream(vm.process().getInputStream(), "OUTPUT");
//            captureStream(vm.process().getErrorStream(), "ERROR");
        /*
        new Thread(() -> {
            try {
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

                                List<Location> locations = mainMethod.allLineLocations();
                                for (Location location : locations) {
                                    if (location.lineNumber() == b) {
                                        BreakpointRequest breakpointRequest = erm.createBreakpointRequest(location);
                                        breakpointRequest.setSuspendPolicy(EventRequest.SUSPEND_ALL);
                                        breakpointRequest.enable();
                                    }
                                }
                            }
                            eventSet.resume();
                        } else if (event instanceof BreakpointEvent) {
                            BreakpointEvent breakpointEvent = (BreakpointEvent) event;
                            context.setBreakpointEvent(breakpointEvent);
                            context.setThread(breakpointEvent.thread());
                            context.setStepRequest(StepRequest.STEP_LINE, StepRequest.STEP_OVER);
                            eventSet.resume();
                        } else if (event instanceof StepEvent) {
                            StepEvent stepEvent = (StepEvent) event;
                            context.setLocation(stepEvent.location());
                            context.setThread(stepEvent.thread());
                            // ExecuteResult execute = new JavaPInst().execute(Instruction.success(ReadType.COMMAND_IN, Operation.R, ""), context);
                            new Thread(() -> {
                                doExp doExp = new doExp();
                                System.err.println("-----" + stepEvent.location().lineNumber());
                                String s = null;
                                try {
                                    s = doExp.executeExpression("abab", context);
                                    System.err.println(s);
                                    System.err.println(stepEvent.location().lineNumber());
                                } catch (Exception e) {
                                    return;
                                }
                            }).start();

                            // System.err.println(DebugUtils.buildCurrentLineInfoByLocation(execute));
                            eventSet.resume();
                        } else {
                            eventSet.resume();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
         */

        // 开启后台, 处理targetVM
        new Thread(() -> {
            try {
                while (true) {
                    System.err.println("准备阻塞....");
                    EventSet set = eventQueue.remove();
                    System.err.println("获取新数据, 不再阻塞----------");
                    Iterator<Event> it = set.iterator();
                    // copy JDB源码
                    boolean resumeStoppedApp = false;
                    while (it.hasNext()) {
                        // 只要有一个事件不resume, 就必须resume
                        resumeStoppedApp |= !handleEvent(it.next());
                    }
                    if (resumeStoppedApp) {
                        set.resume();
                    }
                }
            } catch(VMDisconnectedException ignored) {
            } catch (InterruptedException e) {
                LogUtils.error(e);
            } catch (AbsentInformationException e) {
                LogUtils.error(e);
            }
        }).start();

//        JEventHandler jEventHandler = new JEventHandler(context);

        // 前台读取指令
        StdInReader stdInReader = new StdInReader(null);
        StdOutput stdOutput = new StdOutput(null);
        while (true) {
            try {
                Instruction instruction = stdInReader.readInst();
                if (instruction.isExit()) {
                    break;
                }
                // 分析指令
                InstExecutor executor = JavaInstFactory.getInstance().create(instruction);
                // 执行指令
                ExecuteResult r = executor.execute(instruction, context);
                // 查看当前行
                if (context.getLocation() != null) {
                    DebugUtils.fillExecuteResultByLocation(r, context.getLocation());
                }
                // 输出执行结果
                stdOutput.output(r);
            }  catch (VMDisconnectedException e) {
                LogUtils.warn("debug结束, 终止debug..");
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static boolean handleEvent(Event event) throws AbsentInformationException {
        DebugUtils.simpleDebug(event.toString(), null);
        if (event instanceof ClassPrepareEvent) {
            return handleClassPrepareEvent((ClassPrepareEvent) event);
        } else if (event instanceof BreakpointEvent) {
            return handleBreakpointEvent((BreakpointEvent) event);
        } else if (event instanceof StepEvent) {
            return handleStepEvent((StepEvent) event);
        } else if (event instanceof VMStartEvent) {
            return false;
        } else if (event instanceof VMDisconnectEvent) {
            return handleVMDisconnectEvent((VMDisconnectEvent) event);
        } else {
            return true;
        }
    }

    private static boolean handleVMDisconnectEvent(VMDisconnectEvent event) {
        // 通知

        return true;
    }

    private static boolean handleStepEvent(StepEvent event) {
        context.setLocation(event.location());
        context.setThread(event.thread());

        Thread.yield();  // fetch output

        return false;
    }

    private static boolean handleBreakpointEvent(BreakpointEvent event) {
        context.setLocation(event.location());
        context.setThread(event.thread());

        BreakpointEvent breakpointEvent = (BreakpointEvent) event;
        context.setBreakpointEvent(breakpointEvent);
        context.setThread(breakpointEvent.thread());
        context.setStepRequest(StepRequest.STEP_LINE, StepRequest.STEP_OVER);

        Thread.yield();  // fetch output

        return true;
    }

    static Set<String> classNames = new HashSet<>();

    private static boolean handleClassPrepareEvent(ClassPrepareEvent event) throws AbsentInformationException {
        String className = event.referenceType().name();

        classNames.add(className);
        String[] array = classNames.toArray(new String[0]);

        DebugUtils.simpleDebug("ClassPrepareEvent : " + Arrays.toString(array), null);

        if (className.equals("Main")) {
            // 获取Main类
            ClassType mainClass = (ClassType) event.referenceType();
            Method mainMethod = mainClass.methodsByName("main").get(0);

            List<Location> locations = mainMethod.allLineLocations();
            for (Location location : locations) {
                if (location.lineNumber() == b) {
                    BreakpointRequest breakpointRequest = erm.createBreakpointRequest(location);
                    breakpointRequest.setSuspendPolicy(EventRequest.SUSPEND_ALL);
                    breakpointRequest.enable();
                }
            }
        }
        if (className.equals("Solution")) {
            // 获取Main类
            ClassType mainClass = (ClassType) event.referenceType();
            Method mainMethod = mainClass.methodsByName("solveNQueens").get(0);

            List<Location> locations = mainMethod.allLineLocations();
            for (Location location : locations) {
                if (location.lineNumber() == b) {
                    BreakpointRequest breakpointRequest = erm.createBreakpointRequest(location);
                    breakpointRequest.setSuspendPolicy(EventRequest.SUSPEND_ALL);
                    breakpointRequest.enable();
                }
            }
        }
        return false;
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
